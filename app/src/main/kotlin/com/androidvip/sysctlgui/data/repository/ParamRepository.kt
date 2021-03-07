package com.androidvip.sysctlgui.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.IntDef
import com.androidvip.sysctlgui.data.ParamDao
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import com.androidvip.sysctlgui.utils.ApplyResult
import com.androidvip.sysctlgui.utils.KernelParamUtils
import com.androidvip.sysctlgui.utils.RootUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.IllegalArgumentException

class ParamRepository(
    private val paramDao: ParamDao,
    private val prefs: SharedPreferences
) {
    /**
     * Retrieves all params from a given `source`.
     * Source must be [SOURCE_RUNTIME], [SOURCE_ROOM] or [SOURCE_JSON].
     *
     * @param source Source from where we'll retrieve params
     * @param context Only used if [SOURCE_JSON] source is chosen
     * @param jsonFileName Name of the JSON file containing the params of the [SOURCE_JSON] source
     *
     * @return a List of [KernelParam] from the given `source`
     */
    suspend fun getParams(
        @Source source: Int,
        context: Context? = null,
        jsonFileName: String? = null
    ): List<KernelParam> {
        return when (source) {
            SOURCE_RUNTIME -> getRuntimeParams()
            SOURCE_ROOM -> paramDao.getAll().orEmpty()
            SOURCE_JSON -> getJsonParams(context, jsonFileName)

            else -> throw IllegalArgumentException("Source must be RUNTIME, ROOM or JSON")
        }
    }

    /**
     * Transforms a list of children files of the `/proc/sys/` dir into a list of [KernelParam]s.
     *
     * @param files List of files in a particular `/proc/sys` dir. May be the dir itself.
     * @return a List of [KernelParam] from the given files
     */
    suspend fun getParamsFromFiles(files: List<File>): MutableList<KernelParam> {
        val roomParams = getParams(SOURCE_ROOM)

        return files.map {
            it.absolutePath
        }.mapIndexed { index, path ->
            KernelParam(
                id = index + 1,
                path = path,
            ).apply {
                setNameFromPath(path)
                favorite = roomParams.firstOrNull {
                    it.name == name && it.favorite
                } != null
                taskerParam = roomParams.firstOrNull {
                    it.name == name && it.taskerParam
                } != null
                value = RootUtils.executeWithOutput("cat $path", "")
            }
        }.toMutableList()
    }

    suspend fun update(param: KernelParam, @Target target: Int): ApplyResult {
        if (!allowBlankValues && param.value.isEmpty()) {
            return ApplyResult.Failure(
                IllegalArgumentException("Blank values are currently not allowed")
            )
        }

        return when (target) {
            SOURCE_RUNTIME -> {
                val commitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")
                val commitResult = KernelParamUtils.commitChanges(param, prefs)

                if (commitMode == "sysctl") {
                    if (commitResult == "error" || !commitResult.contains(param.name)) {
                        ApplyResult.Failure(Exception("Value refused to apply. Try using 'echo' mode."))
                    } else {
                        ApplyResult.Success
                    }
                } else {
                    if (commitResult == "error") {
                        ApplyResult.Failure(Exception("Value refused to apply"))
                    } else {
                        ApplyResult.Success
                    }
                }
            }
            SOURCE_ROOM -> {
                if (getParams(SOURCE_ROOM).contains(param)) {
                    paramDao.update(param)
                } else {
                    paramDao.insert(param)
                }
                ApplyResult.Success
            }
            else -> {
                ApplyResult.Failure(
                    UnsupportedOperationException("Updating json params is not supported")
                )
            }
        }
    }

    suspend fun delete(param: KernelParam, @Target target: Int = SOURCE_ROOM) {
        when (target) {
            SOURCE_ROOM -> paramDao.delete(param)
            else -> throw UnsupportedOperationException(
                "Deleting params is only supported in room database"
            )
        }
    }

    suspend fun clear(
        @Target target: Int,
        context: Context? = null,
        jsonFileName: String? = null
    ) {
        when (target) {
            SOURCE_ROOM -> paramDao.clearTable()
            SOURCE_JSON -> {
                requireNotNull(jsonFileName)
                JsonRepository(context, jsonFileName).removeAllParams()
            }
            else -> throw UnsupportedOperationException(
                "Clearing params is only supported in Room or JSON database"
            )
        }
    }

    suspend fun addParam(param: KernelParam, @Target target: Int = SOURCE_ROOM) {
        if (!allowBlankValues && param.value.isEmpty()) return

        when (target) {
            SOURCE_ROOM -> {
                if (getParams(SOURCE_ROOM).contains(param)) {
                    update(param, SOURCE_ROOM)
                } else {
                    paramDao.insert(param)
                }
            }
            else -> throw UnsupportedOperationException(
                "Adding params is only supported in room database"
            )
        }
    }

    suspend fun addParams(params: List<KernelParam>, @Target target: Int = SOURCE_ROOM) {
        val filteredParams = if (allowBlankValues) {
            params
        } else params.filterNot {
            it.value.isEmpty()
        }

        when (target) {
            SOURCE_ROOM -> paramDao.insert(*filteredParams.toTypedArray())
            else -> throw UnsupportedOperationException(
                "Adding params is only supported in room database"
            )
        }
    }

    /**
     * Retrieves a list of _all_ runtime kernel params by running the `sysctl -a` command.
     *
     * @return a [KernelParam] list with current runtime values
     */
    private suspend fun getRuntimeParams(): MutableList<KernelParam> {
        val command = if (RootUtils.isBusyboxAvailable()) "busybox sysctl -a" else "sysctl -a"
        val lines = mutableListOf<String>()
        RootUtils.executeWithOutput(command, "") { lines += it }

        val roomParams = getParams(SOURCE_ROOM)

        return lines.filter {
            it.isValidSysctlOutput()
        }.map {
            // Expected output: grandparent.parent.name = value
            val split = it.split("=")
            split.first().trim() to split.last().trim()
        }.mapIndexed { index, paramPair ->
            KernelParam(
                id = index + 1,
                name = paramPair.first,
                value = paramPair.second,
            ).apply {
                setPathFromName(paramPair.first)
                favorite = roomParams.firstOrNull {
                    it.name == name && it.favorite
                } != null
                taskerParam = roomParams.firstOrNull {
                    it.name == name && it.taskerParam
                } != null
            }
        }.toMutableList()
    }

    private suspend fun getJsonParams(
        context: Context?,
        filename: String? = null
    ) = withContext(Dispatchers.IO) {
        requireNotNull(filename)

        return@withContext JsonRepository(context, filename).getUserParamsSet().map {
            it.apply {
                if (filename == "favorites-params") {
                    favorite = true
                }
                if ("tasker-params" in filename) {
                    taskerParam = true
                }
            }
        }
    }

    private fun String.isValidSysctlOutput(): Boolean {
        return !contains("denied") && !startsWith("sysctl") && contains("=")
    }

    suspend fun performDatabaseMigration(context: Context?) {
        requireNotNull(context)
        val oldFavorites = getJsonParams(context, "favorites-params")
        val oldParams = getJsonParams(context, "user-params")
        val oldTasker = getJsonParams(
            context, "tasker-params-${TaskerReceiver.LIST_NUMBER_PRIMARY_TASKER}"
        ) + getJsonParams(
            context, "tasker-params-${TaskerReceiver.LIST_NUMBER_SECONDARY_TASKER}"
        ) + getJsonParams(
            context, "tasker-params-${TaskerReceiver.LIST_NUMBER_FAVORITES}"
        ) + getJsonParams(
            context, "tasker-params-${TaskerReceiver.LIST_NUMBER_APPLY_ON_BOOT}"
        )

        val mergedList = (oldFavorites + oldParams + oldTasker).distinct()
        addParams(mergedList, SOURCE_ROOM)
    }

    private val allowBlankValues
        get() = prefs.getBoolean(Prefs.ALLOW_BLANK, false)

    companion object {
        const val SOURCE_RUNTIME = 0
        const val SOURCE_ROOM = 1

        @Deprecated("Use room database instead")
        const val SOURCE_JSON = 2

        @IntDef(value = [SOURCE_RUNTIME, SOURCE_ROOM, SOURCE_JSON])
        @Retention(AnnotationRetention.SOURCE)
        annotation class Source
    }

}

typealias Target = ParamRepository.Companion.Source

package com.androidvip.sysctlgui.data.repository

import android.util.Log
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import com.androidvip.sysctlgui.utils.isValidSysctlLine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import java.io.File

class ParamsRepositoryImpl(
    private val rootUtils: RootUtils,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ParamsRepository {
    override fun getRuntimeParams(
        useBusybox: Boolean,
        userParams: List<KernelParam>
    ): Flow<List<KernelParam>> = flow {
        val command = if (useBusybox) BUSYBOX_SYSCTL_GET_ALL_COMMAND else SYSCTL_GET_ALL_COMMAND
        val paramsList = rootUtils.executeCommandAndStreamOutput(command)
            .filter { line -> line.isValidSysctlOutput() }
            .mapNotNull { line ->
                // Expected output: "grandparent.parent.name = value"
                val parts = line.split("=", limit = 2)
                val paramName = parts.first().trim()
                val paramValue = if (parts.size > 1) parts.last().trim() else ""
                runCatching {
                    KernelParam.createFromName(
                        name = paramName,
                        value = paramValue,
                        isFavorite = userParams.any { it.name == paramName }
                    )
                }.getOrNull()
            }
            .toList()

        emit(paramsList)
    }.flowOn(ioDispatcher)

    override suspend fun getRuntimeParam(paramName: String, useBusybox: Boolean): KernelParam? {
        val command = String.format(
            SYSCTL_GET_PARAM_COMMAND_FORMAT,
            if (useBusybox) BUSYBOX_PREFIX else "",
            paramName
        )

        val paramValue = runCatching {
            rootUtils.executeCommandAndStreamOutput(command).single()
        }.getOrNull() ?: return null

        return KernelParam.createFromName(
            name = paramName,
            value = paramValue
        )
    }

    override suspend fun setRuntimeParam(
        param: KernelParam,
        commitMode: CommitMode,
        useBusybox: Boolean
    ): String {
        val command = when (commitMode) {
            CommitMode.SYSCTL -> String.format(
                SYSCTL_SET_PARAM_COMMAND_FORMAT,
                if (useBusybox) BUSYBOX_PREFIX else "",
                param.name,
                param.value
            )

            CommitMode.ECHO -> String.format(ECHO_SET_PARAM_COMMAND_FORMAT, param.value, param.path)
        }

        val output = rootUtils.executeCommandAndStreamOutput(command).toList()
        return output.joinToString("\n")
    }

    /**
     * Reads kernel parameters from a list of files.
     * The parameter name is derived from the file path.
     *
     * @param files A list of [File] objects representing the kernel parameter files.
     * @return A [Flow] emitting a list of [KernelParam] objects.
     *         Returns an empty list if no files are provided or if errors occur during processing.
     *         Emits null for files that could not be processed.
     */
    override fun getParamsFromFiles(files: List<File>): Flow<List<KernelParam>> = flow {
        val params = files.mapNotNull { file ->
            try {
                val path = file.absolutePath
                if (file.isDirectory) {
                    KernelParam.createFromPath(path, "")
                } else {
                    val fileContent = runCatching { file.readText() }.getOrNull()
                    if (fileContent != null) {
                        KernelParam.createFromPath(path, fileContent)
                    } else {
                        val command = String.format(CAT_COMMAND_FORMAT, path)
                        val paramValue = rootUtils.executeCommandAndStreamOutput(command)
                            .toList()
                            .joinToString("\n")
                        KernelParam.createFromPath(path, paramValue)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process file: ${file.path}", e)
                null
            }
        }
        emit(params)
    }.flowOn(ioDispatcher)

    override fun getParamsFromPath(path: String): Flow<List<KernelParam>> {
        val files = File(path).listFiles()?.toList() ?: emptyList()
        return getParamsFromFiles(files)
    }

    private fun String.isValidSysctlOutput(): Boolean {
        return isValidSysctlLine() &&
                !this.contains("denied", ignoreCase = true) &&
                !this.startsWith("sysctl")
    }

    interface ChangeListener {
        fun onChange()
    }

    companion object {
        private const val BUSYBOX_PREFIX = "busybox "
        private const val SYSCTL_GET_ALL_COMMAND = "sysctl -a"
        private const val BUSYBOX_SYSCTL_GET_ALL_COMMAND = "$BUSYBOX_PREFIX$SYSCTL_GET_ALL_COMMAND"
        private const val SYSCTL_GET_PARAM_COMMAND_FORMAT = "%ssysctl -n %s" // prefix, name
        private const val SYSCTL_SET_PARAM_COMMAND_FORMAT =
            "%ssysctl -w %s=%s" // prefix, name, value
        private const val ECHO_SET_PARAM_COMMAND_FORMAT = "echo '%s' > %s" // value, path
        private const val CAT_COMMAND_FORMAT = "cat %s" // path
        private const val TAG = "ParamsRepositoryImpl"
    }
}

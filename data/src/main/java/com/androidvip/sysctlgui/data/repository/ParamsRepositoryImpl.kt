package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.datasource.JsonParamDataSource
import com.androidvip.sysctlgui.data.datasource.RoomParamDataSource
import com.androidvip.sysctlgui.data.datasource.RuntimeParamDataSource
import com.androidvip.sysctlgui.domain.exceptions.EmptyFileException
import com.androidvip.sysctlgui.domain.exceptions.MalformedLineException
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type
import kotlin.coroutines.CoroutineContext

class ParamsRepositoryImpl(
    private val jsonParamDataSource: JsonParamDataSource,
    private val roomParamDataSource: RoomParamDataSource,
    private val runtimeParamDataSource: RuntimeParamDataSource,
    private val changeListener: ChangeListener?,
    private val ioContext: CoroutineContext = Dispatchers.IO,
    private val workerContext: CoroutineContext = Dispatchers.Default
) : ParamsRepository {

    override suspend fun getUserParams(): Result<List<DomainKernelParam>> = withContext(ioContext) {
        return@withContext roomParamDataSource.getData()
    }

    override suspend fun getJsonParams(): Result<List<DomainKernelParam>> = withContext(ioContext) {
        return@withContext jsonParamDataSource.getData()
    }

    override suspend fun getRuntimeParams(
        useBusybox: Boolean
    ): Result<List<DomainKernelParam>> = withContext(workerContext) {
        val localResult = getUserParams()
        if (localResult.isFailure) return@withContext localResult

        val runtimeResult = runtimeParamDataSource.getData(useBusybox)
        if (runtimeResult.isFailure) return@withContext runtimeResult

        val localParams = localResult.getOrNull().orEmpty()
        val runtimeParams = runtimeResult.getOrNull().orEmpty()

        return@withContext runCatching {
            runtimeParams.onEach { runtimeParam ->
                runtimeParam.updateParamWithLocalData(localParams)
            }
        }
    }

    override suspend fun getParamsFromFiles(
        files: List<File>
    ): Result<List<DomainKernelParam>> = withContext(ioContext) {
        val result = runtimeParamDataSource.getParamsFromFiles(files)
        if (result.isFailure) return@withContext result

        val localResult = getUserParams()
        if (localResult.isFailure) return@withContext localResult

        val localParams = localResult.getOrNull().orEmpty()
        val fileParams = result.getOrNull().orEmpty()

        return@withContext runCatching {
            fileParams.onEach { runtimeParam ->
                runtimeParam.updateParamWithLocalData(localParams)
            }
        }
    }

    override suspend fun applyParam(
        param: DomainKernelParam,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    ): Result<Unit> = withContext(workerContext) {
        return@withContext runtimeParamDataSource.edit(
            param, commitMode, useBusybox, allowBlank
        ).also {
            changeListener?.onChange()
        }
    }

    override suspend fun updateUserParam(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> = withContext(ioContext) {
        val storedParam = getUserParams().getOrNull().orEmpty().find {
            it.name == param.name
        } ?: return@withContext addUserParam(param, allowBlank)

        param.id = storedParam.id
        return@withContext roomParamDataSource.edit(param, allowBlank)
            .also { changeListener?.onChange() }
    }

    override suspend fun addUserParam(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> = withContext(ioContext) {
        return@withContext roomParamDataSource.add(param, allowBlank)
            .also { changeListener?.onChange() }
    }

    override suspend fun addUserParams(
        params: List<DomainKernelParam>,
        allowBlank: Boolean
    ): Result<Unit> = withContext(ioContext) {
        return@withContext roomParamDataSource.addAll(params, allowBlank)
            .also { changeListener?.onChange() }
    }

    override suspend fun removeUserParam(param: DomainKernelParam): Result<Unit> =
        withContext(ioContext) {
            return@withContext roomParamDataSource.remove(param).also { changeListener?.onChange() }
        }

    override suspend fun clearUserParams(): Result<Unit> = withContext(ioContext) {
        return@withContext roomParamDataSource.clear().also {
            jsonParamDataSource.clear()
            changeListener?.onChange()
        }
    }

    override suspend fun performDatabaseMigration(): Result<Unit> = withContext(ioContext) {
        val jsonParams = getJsonParams().getOrNull().orEmpty()

        return@withContext roomParamDataSource.addAll(jsonParams, true).also {
            changeListener?.onChange()
        }
    }

    override suspend fun importParamsFromJson(
        stream: InputStream
    ): Result<List<DomainKernelParam>> = withContext(ioContext) {
        return@withContext runCatching {
            if (stream.available() == 0) throw EmptyFileException()

            val rawText = buildString {
                stream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        append(line)
                    }
                }
            }
            val type: Type = object : TypeToken<List<DomainKernelParam>>() {}.type
            return@runCatching Gson().fromJson(rawText, type)
        }
    }

    override suspend fun importParamsFromConf(
        stream: InputStream
    ): Result<List<DomainKernelParam>> = withContext(ioContext) {
        fun String.validConfLine() = !startsWith("#") && !startsWith(";") && isNotEmpty()
        val readParams = mutableListOf<DomainKernelParam>()
        return@withContext runCatching {
            if (stream.available() == 0) throw EmptyFileException()

            var cont = 0
            stream.bufferedReader().forEachLine { line ->
                if (line.validConfLine()) runCatching {
                    readParams.add(
                        DomainKernelParam(
                            id = ++cont,
                            name = line.split("=").first().trim(),
                            value = line.split("=")[1].trim()
                        ).apply {
                            setPathFromName(this.name)
                        }
                    )
                }.onFailure {
                    throw MalformedLineException()
                }
            }
            readParams
        }
    }

    private fun DomainKernelParam.updateParamWithLocalData(
        localParams: List<DomainKernelParam>
    ): DomainKernelParam {
        return apply {
            favorite = localParams.firstOrNull { roomParam ->
                (roomParam.name == name) && roomParam.favorite
            } != null
            taskerParam = localParams.firstOrNull { roomParam ->
                (roomParam.name == name) && roomParam.taskerParam
            } != null
        }
    }

    interface ChangeListener {
        fun onChange()
    }
}

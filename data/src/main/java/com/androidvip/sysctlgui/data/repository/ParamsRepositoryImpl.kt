package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.datasource.JsonParamDataSource
import com.androidvip.sysctlgui.data.datasource.RoomParamDataSource
import com.androidvip.sysctlgui.data.datasource.RuntimeParamDataSource
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.File

class ParamsRepositoryImpl(
    private val jsonParamDataSource: JsonParamDataSource,
    private val roomParamDataSource: RoomParamDataSource,
    private val runtimeParamDataSource: RuntimeParamDataSource,
    private val changeListener: ChangeListener?,
) : ParamsRepository {

    override suspend fun getUserParams(): Result<List<DomainKernelParam>> {
        return roomParamDataSource.getData()
    }

    override suspend fun getJsonParams(): Result<List<DomainKernelParam>> {
        return jsonParamDataSource.getData()
    }

    override suspend fun getRuntimeParams(useBusybox: Boolean): Result<List<DomainKernelParam>> {
        val localResult = getUserParams()
        if (localResult.isFailure) return localResult

        val runtimeResult = runtimeParamDataSource.getData(useBusybox)
        if (runtimeResult.isFailure) return runtimeResult

        val localParams = localResult.getOrNull().orEmpty()
        val runtimeParams = runtimeResult.getOrNull().orEmpty()

        return runCatching {
            runtimeParams.onEach { runtimeParam ->
                runtimeParam.updateParamWithLocalData(localParams)
            }
        }
    }

    override suspend fun getParamsFromFiles(files: List<File>): Result<List<DomainKernelParam>> {
        val result = runtimeParamDataSource.getParamsFromFiles(files)
        if (result.isFailure) return result

        val localResult = getUserParams()
        if (localResult.isFailure) return localResult

        val localParams = localResult.getOrNull().orEmpty()
        val fileParams = result.getOrNull().orEmpty()

        return runCatching {
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
    ): Result<Unit> {
        return runtimeParamDataSource.edit(param, commitMode, useBusybox, allowBlank).also {
            changeListener?.onChange()
        }
    }

    override suspend fun updateUserParam(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> {
        val storedParam = getUserParams().getOrNull().orEmpty().find {
            it.name == param.name
        } ?: return addUserParam(param, allowBlank)

        param.id = storedParam.id
        return roomParamDataSource.edit(param, allowBlank).also { changeListener?.onChange() }
    }

    override suspend fun addUserParam(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> {
        return roomParamDataSource.add(param, allowBlank).also { changeListener?.onChange() }
    }

    override suspend fun addUserParams(
        params: List<DomainKernelParam>,
        allowBlank: Boolean
    ): Result<Unit> {
        return roomParamDataSource.addAll(params, allowBlank).also { changeListener?.onChange() }
    }

    override suspend fun removeUserParam(param: DomainKernelParam): Result<Unit> {
        return roomParamDataSource.remove(param).also { changeListener?.onChange() }
    }

    override suspend fun clearUserParams(): Result<Unit> {
        return roomParamDataSource.clear().also {
            jsonParamDataSource.clear()
            changeListener?.onChange()
        }
    }

    override suspend fun performDatabaseMigration(): Result<Unit> {
        val jsonParams = getJsonParams().getOrNull().orEmpty()

        return roomParamDataSource.addAll(jsonParams, true).also {
            changeListener?.onChange()
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

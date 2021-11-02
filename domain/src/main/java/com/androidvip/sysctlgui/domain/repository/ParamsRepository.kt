package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import java.io.File

interface ParamsRepository {
    suspend fun getUserParams(): Result<List<DomainKernelParam>>
    suspend fun getJsonParams(): Result<List<DomainKernelParam>>
    suspend fun getRuntimeParams(useBusybox: Boolean): Result<List<DomainKernelParam>>
    suspend fun getParamsFromFiles(files: List<File>): Result<List<DomainKernelParam>>

    suspend fun applyParam(
        param: DomainKernelParam,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    ): Result<Unit>
    suspend fun updateUserParam(param: DomainKernelParam, allowBlank: Boolean): Result<Unit>

    suspend fun addUserParam(param: DomainKernelParam, allowBlank: Boolean): Result<Unit>
    suspend fun addUserParams(params: List<DomainKernelParam>, allowBlank: Boolean): Result<Unit>
    suspend fun removeUserParam(param: DomainKernelParam): Result<Unit>
    suspend fun clearUserParams(): Result<Unit>

    suspend fun performDatabaseMigration(): Result<Unit>
}

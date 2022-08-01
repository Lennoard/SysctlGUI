package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import java.io.File
import java.io.FileDescriptor
import java.io.InputStream

interface ParamsRepository {
    suspend fun getUserParams(): List<DomainKernelParam>
    suspend fun getJsonParams(): List<DomainKernelParam>
    suspend fun getRuntimeParams(useBusybox: Boolean): List<DomainKernelParam>
    suspend fun getParamsFromFiles(files: List<File>): List<DomainKernelParam>

    suspend fun applyParam(
        param: DomainKernelParam,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    )
    suspend fun updateUserParam(param: DomainKernelParam, allowBlank: Boolean)

    suspend fun addUserParam(param: DomainKernelParam, allowBlank: Boolean)
    suspend fun addUserParams(params: List<DomainKernelParam>, allowBlank: Boolean)
    suspend fun removeUserParam(param: DomainKernelParam)
    suspend fun clearUserParams()

    suspend fun performDatabaseMigration()

    suspend fun importParamsFromJson(stream: InputStream): List<DomainKernelParam>
    suspend fun importParamsFromConf(stream: InputStream): List<DomainKernelParam>
    suspend fun exportParams(params: List<DomainKernelParam>, fileDescriptor: FileDescriptor)
    suspend fun backupParams(params: List<DomainKernelParam>, fileDescriptor: FileDescriptor)
}

package com.androidvip.sysctlgui.domain.datasource

import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import java.io.File

interface RuntimeDataSourceContract<T> {
    suspend fun edit(
        param: DomainKernelParam,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    ): Result<Unit>

    suspend fun getData(useBusybox: Boolean): Result<List<DomainKernelParam>>
    suspend fun getParamsFromFiles(files: List<File>): Result<List<DomainKernelParam>>
}

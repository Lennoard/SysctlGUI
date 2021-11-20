package com.androidvip.sysctlgui.domain.datasource

import java.io.File

interface RuntimeDataSourceContract<T> {
    suspend fun edit(
        param: T,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    ): Result<Unit>

    suspend fun getData(useBusybox: Boolean): Result<List<T>>
    suspend fun getParamsFromFiles(files: List<File>): Result<List<T>>
}

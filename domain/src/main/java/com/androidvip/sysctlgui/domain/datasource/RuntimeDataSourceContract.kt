package com.androidvip.sysctlgui.domain.datasource

import java.io.File

interface RuntimeDataSourceContract<T> {
    suspend fun edit(param: T, commitMode: String, useBusybox: Boolean, allowBlank: Boolean)
    suspend fun getData(useBusybox: Boolean): List<T>
    suspend fun getParamsFromFiles(files: List<File>): List<T>
}

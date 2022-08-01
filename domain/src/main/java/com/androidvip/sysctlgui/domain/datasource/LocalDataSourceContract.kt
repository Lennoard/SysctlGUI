package com.androidvip.sysctlgui.domain.datasource

interface LocalDataSourceContract<T> {
    suspend fun add(param: T, allowBlank: Boolean)
    suspend fun addAll(params: List<T>, allowBlank: Boolean)
    suspend fun remove(param: T)
    suspend fun edit(param: T, allowBlank: Boolean)
    suspend fun clear()
    suspend fun getData(): List<T>
}

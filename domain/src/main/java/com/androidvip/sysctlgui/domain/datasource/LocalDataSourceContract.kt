package com.androidvip.sysctlgui.domain.datasource

interface LocalDataSourceContract<T> {
    suspend fun add(param: T, allowBlank: Boolean): Result<Unit>
    suspend fun addAll(params: List<T>, allowBlank: Boolean): Result<Unit>
    suspend fun remove(param: T): Result<Unit>
    suspend fun edit(param: T, allowBlank: Boolean): Result<Unit>
    suspend fun clear(): Result<Unit>
    suspend fun getData(): Result<List<T>>
}

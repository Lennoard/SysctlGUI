package com.androidvip.sysctlgui.domain.datasource

import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam

interface LocalDataSourceContract<T> {
    suspend fun add(param: DomainKernelParam, allowBlank: Boolean): Result<Unit>
    suspend fun addAll(params: List<DomainKernelParam>, allowBlank: Boolean): Result<Unit>
    suspend fun remove(param: DomainKernelParam): Result<Unit>
    suspend fun edit(param: DomainKernelParam, allowBlank: Boolean): Result<Unit>
    suspend fun clear(): Result<Unit>
    suspend fun getData(): Result<List<T>>
}

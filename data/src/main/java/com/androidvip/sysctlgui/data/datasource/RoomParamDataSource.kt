package com.androidvip.sysctlgui.data.datasource

import com.androidvip.sysctlgui.data.db.ParamDao
import com.androidvip.sysctlgui.data.mapper.RoomParamMapper
import com.androidvip.sysctlgui.domain.datasource.LocalDataSourceContract
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomParamDataSource(
    private val paramDao: ParamDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalDataSourceContract<DomainKernelParam> {
    override suspend fun add(
        param: DomainKernelParam,
        allowBlank: Boolean
    ) = withContext(dispatcher) {
        if (!allowBlank) require(param.value.isNotBlank()) {
            "Param contains blank value while ALLOW_BLANK is not active"
        }
        paramDao.insert(RoomParamMapper.unmap(param))
    }

    override suspend fun addAll(
        params: List<DomainKernelParam>,
        allowBlank: Boolean
    ) = withContext(dispatcher) {
        val filteredParams = if (allowBlank) {
            params
        } else params.filter {
            it.value.isNotEmpty()
        }

        paramDao.insert(*filteredParams.map { RoomParamMapper.unmap(it) }.toTypedArray())
    }

    override suspend fun remove(param: DomainKernelParam) = withContext(dispatcher) {
        paramDao.delete(RoomParamMapper.unmap(param))
    }

    override suspend fun edit(
        param: DomainKernelParam,
        allowBlank: Boolean
    ) = withContext(dispatcher) {
        if (!allowBlank) require(param.value.isNotBlank()) {
            "Param contains blank value while ALLOW_BLANK is not active"
        }
        paramDao.update(RoomParamMapper.unmap(param))
    }

    override suspend fun clear() = withContext(dispatcher) {
        paramDao.clearTable()
    }

    override suspend fun getData(): List<DomainKernelParam> = withContext(dispatcher) {
        paramDao.getAll()?.map {
            RoomParamMapper.map(it)
        } ?: throw Exception("Failed to get params from the local database")
    }
}

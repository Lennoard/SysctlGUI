package com.androidvip.sysctlgui.data.datasource

import com.androidvip.sysctlgui.data.db.ParamDao
import com.androidvip.sysctlgui.data.mapper.RoomParamMapper
import com.androidvip.sysctlgui.domain.datasource.LocalDataSourceContract
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import java.lang.Exception
import java.lang.IllegalArgumentException

class RoomParamDataSource(
    private val paramDao: ParamDao
) : LocalDataSourceContract<DomainKernelParam> {
    override suspend fun add(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> = runCatching {
        if (!allowBlank && param.value.isBlank()) throw IllegalArgumentException(
            "Param contains blank value while ALLOW_BLANK is not active"
        )
        paramDao.insert(RoomParamMapper.unmap(param))
    }

    override suspend fun addAll(
        params: List<DomainKernelParam>,
        allowBlank: Boolean
    ): Result<Unit> = runCatching {
        val filteredParams = if (allowBlank) {
            params
        } else params.filter {
            it.value.isNotEmpty()
        }

        paramDao.insert(*filteredParams.map { RoomParamMapper.unmap(it) }.toTypedArray())
    }

    override suspend fun remove(param: DomainKernelParam): Result<Unit> = runCatching {
        paramDao.delete(RoomParamMapper.unmap(param))
    }

    override suspend fun edit(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> = runCatching {
        if (!allowBlank && param.value.isBlank()) throw IllegalArgumentException(
            "Param contains blank value while ALLOW_BLANK is not active"
        )
        paramDao.update(RoomParamMapper.unmap(param))
    }

    override suspend fun clear(): Result<Unit> = runCatching {
        paramDao.clearTable()
    }

    override suspend fun getData(): Result<List<DomainKernelParam>> = runCatching {
        paramDao.getAll()?.map {
            RoomParamMapper.map(it)
        } ?: throw Exception("Failed to get params from the local database")
    }
}

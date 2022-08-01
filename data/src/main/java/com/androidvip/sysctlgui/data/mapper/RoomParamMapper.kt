package com.androidvip.sysctlgui.data.mapper

import com.androidvip.sysctlgui.data.models.RoomKernelParam
import com.androidvip.sysctlgui.domain.models.DomainKernelParam

object RoomParamMapper : Mapper<RoomKernelParam, DomainKernelParam> {
    override fun map(from: RoomKernelParam): DomainKernelParam = DomainKernelParam().apply {
        id = from.id
        name = from.name
        path = from.path
        value = from.value
        favorite = from.favorite
        taskerParam = from.taskerParam
        taskerList = from.taskerList
    }

    override fun unmap(from: DomainKernelParam): RoomKernelParam = RoomKernelParam().apply {
        id = from.id
        name = from.name
        path = from.path
        value = from.value
        favorite = from.favorite
        taskerParam = from.taskerParam
        taskerList = from.taskerList
    }
}

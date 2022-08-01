package com.androidvip.sysctlgui.data.mapper

import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.models.DomainKernelParam

object DomainParamMapper : Mapper<DomainKernelParam, KernelParam> {
    override fun map(from: DomainKernelParam): KernelParam = KernelParam().apply {
        id = from.id
        name = from.name
        path = from.path
        value = from.value
        favorite = from.favorite
        taskerParam = from.taskerParam
        taskerList = from.taskerList
    }

    override fun unmap(from: KernelParam): DomainKernelParam = DomainKernelParam().apply {
        id = from.id
        name = from.name
        path = from.path
        value = from.value
        favorite = from.favorite
        taskerParam = from.taskerParam
        taskerList = from.taskerList
    }
}

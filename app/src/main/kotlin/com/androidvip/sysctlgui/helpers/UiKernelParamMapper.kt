package com.androidvip.sysctlgui.helpers

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.models.UiKernelParam

object UiKernelParamMapper {
    fun map(param: KernelParam): UiKernelParam {
        return UiKernelParam(
            name = param.name,
            path = param.path,
            value = param.value,
            isFavorite = param.isFavorite,
            isTaskerParam = param.isTaskerParam,
            taskerList = param.taskerList
        )
    }
}

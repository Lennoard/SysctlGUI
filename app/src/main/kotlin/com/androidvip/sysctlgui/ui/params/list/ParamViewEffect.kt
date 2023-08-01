package com.androidvip.sysctlgui.ui.params.list

import com.androidvip.sysctlgui.data.models.KernelParam

sealed interface ParamViewEffect {
    class NavigateToParamDetails(val param: KernelParam) : ParamViewEffect
}

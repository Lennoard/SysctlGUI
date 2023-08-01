package com.androidvip.sysctlgui.ui.params.list

import com.androidvip.sysctlgui.domain.models.DomainKernelParam

sealed interface ParamViewEvent {
    class OnSearchExpressionChanged(val data: String) : ParamViewEvent
    class OnParamClicked(val param: DomainKernelParam) : ParamViewEvent
    object OnRefreshRequested : ParamViewEvent
}

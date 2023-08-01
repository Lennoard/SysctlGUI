package com.androidvip.sysctlgui.ui.params.list

import com.androidvip.sysctlgui.domain.models.DomainKernelParam

sealed interface ParamViewEvent {
    object RefreshRequested : ParamViewEvent
    class SearchExpressionChanged(val data: String) : ParamViewEvent
    class ParamClicked(val param: DomainKernelParam) : ParamViewEvent
}

package com.androidvip.sysctlgui.ui.user

import com.androidvip.sysctlgui.models.UiKernelParam

data class UserParamsViewState(
    val userParams: List<UiKernelParam> = emptyList(),
    val loading: Boolean = true
)

sealed interface UserParamsViewEvent {
    data class ScreenLoaded(val filterPredicate: (UiKernelParam) -> Boolean) : UserParamsViewEvent
    data class ParamClicked(val param: UiKernelParam) : UserParamsViewEvent
    data class ParamDeleteRequested(val param: UiKernelParam) : UserParamsViewEvent
    data object ParamRestoreRequested : UserParamsViewEvent
}

sealed interface UserParamsViewEffect {
    data class ShowParamDetails(val param: UiKernelParam) : UserParamsViewEffect
    data class ShowUndoSnackBar(val param: UiKernelParam) : UserParamsViewEffect
}

package com.androidvip.sysctlgui.ui.params.user

import com.androidvip.sysctlgui.data.models.KernelParam

sealed interface UserParamsViewEvent {
    object ParamsRequested : UserParamsViewEvent
    object SearchViewPressed : UserParamsViewEvent
    object SearchPressed : UserParamsViewEvent
    object CloseSearchPressed : UserParamsViewEvent
    class DeleteSwipe(val param: KernelParam) : UserParamsViewEvent
    class SearchQueryChanged(val query: String) : UserParamsViewEvent
}

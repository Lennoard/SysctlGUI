package com.androidvip.sysctlgui.ui.params.browse

import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.models.UiKernelParam

data class ParamBrowseState(
    val params: List<UiKernelParam> = emptyList(),
    val currentPath: String = "",
    val backEnabled: Boolean = false,
    val documentation: ParamDocumentation? = null
)

sealed interface ParamBrowseViewEffect {
    data class OpenBrowser(val url: String) : ParamBrowseViewEffect
    data class EditKernelParam(val param: UiKernelParam) : ParamBrowseViewEffect
    data class ShowError(val errorMessage: String) : ParamBrowseViewEffect
}

sealed interface ParamBrowseViewEvent {
    data class ParamClicked(val param: UiKernelParam) : ParamBrowseViewEvent
    data class DocumentationClicked(val docs: ParamDocumentation) : ParamBrowseViewEvent
    object BackRequested : ParamBrowseViewEvent
}

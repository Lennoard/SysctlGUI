package com.androidvip.sysctlgui.ui.params.browse

import androidx.compose.runtime.Immutable
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.models.UiParamDocumentation

@Immutable
data class ParamBrowseState(
    val loading: Boolean = false,
    val params: List<UiKernelParam> = emptyList(),
    val currentPath: String = "",
    val backEnabled: Boolean = false,
    val documentation: UiParamDocumentation? = null
)

sealed interface ParamBrowseViewEffect {
    data class OpenBrowser(val url: String) : ParamBrowseViewEffect
    data class EditKernelParam(val param: UiKernelParam) : ParamBrowseViewEffect
    data class ShowError(val errorMessage: String) : ParamBrowseViewEffect
}

sealed interface ParamBrowseViewEvent {
    data class ParamClicked(val param: UiKernelParam) : ParamBrowseViewEvent
    data class DocumentationClicked(val docs: UiParamDocumentation) : ParamBrowseViewEvent
    object BackRequested : ParamBrowseViewEvent
    object RefreshRequested : ParamBrowseViewEvent
}

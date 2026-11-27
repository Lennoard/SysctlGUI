package com.androidvip.sysctlgui.ui.params.browse

import androidx.compose.runtime.Immutable
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.models.UiParamDocumentation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ParamBrowseState(
    val loading: Boolean = false,
    val params: ImmutableList<UiKernelParam> = persistentListOf(),
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

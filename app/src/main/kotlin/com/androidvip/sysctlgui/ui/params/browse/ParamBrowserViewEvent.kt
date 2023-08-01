package com.androidvip.sysctlgui.ui.params.browse

import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import java.io.File

sealed interface ParamBrowserViewEvent {
    object RefreshRequested : ParamBrowserViewEvent
    class SearchExpressionChanged(val data: String) : ParamBrowserViewEvent
    class ParamClicked(val param: DomainKernelParam) : ParamBrowserViewEvent
    class DirectoryChanged(val dir: File) : ParamBrowserViewEvent
    object DocumentationMenuClicked : ParamBrowserViewEvent
    object FavoritesMenuClicked : ParamBrowserViewEvent
}

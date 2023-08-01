package com.androidvip.sysctlgui.ui.params.browse

import androidx.annotation.StringRes
import com.androidvip.sysctlgui.data.models.KernelParam

sealed class ParamBrowserViewEffect {
    object NavigateToFavorite : ParamBrowserViewEffect()
    class NavigateToParamDetails(val param: KernelParam) : ParamBrowserViewEffect()
    class OpenDocumentationUrl(val url: String) : ParamBrowserViewEffect()
    class ShowToast(@StringRes val stringRes: Int) : ParamBrowserViewEffect()
}

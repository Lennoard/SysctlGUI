package com.androidvip.sysctlgui.ui.params.browse

import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.utils.Consts

data class ParamBrowserViewState(
    var data: List<KernelParam> = listOf(),
    var isLoading: Boolean = true,
    var showEmptyState: Boolean = false,
    var currentPath: String = Consts.PROC_SYS,
    var showDocumentationMenu: Boolean = false,
    var docUrl: String = "https://www.kernel.org/doc/Documentation"
)

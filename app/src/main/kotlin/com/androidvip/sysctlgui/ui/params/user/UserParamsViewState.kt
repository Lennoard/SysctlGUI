package com.androidvip.sysctlgui.ui.params.user

import com.androidvip.sysctlgui.data.models.KernelParam

data class UserParamsViewState(
    val searchViewVisible: Boolean = false,
    val params: List<KernelParam> = emptyList(),
)

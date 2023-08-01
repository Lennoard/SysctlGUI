package com.androidvip.sysctlgui.ui.params.list

import com.androidvip.sysctlgui.data.models.KernelParam

data class ParamViewState(
    var data: List<KernelParam> = listOf(),
    var isLoading: Boolean = true,
    var showEmptyState: Boolean = false
)

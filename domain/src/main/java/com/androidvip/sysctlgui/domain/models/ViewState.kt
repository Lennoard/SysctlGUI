package com.androidvip.sysctlgui.domain.models

data class ViewState<T>(
    var data: List<T> = listOf(),
    var isLoading: Boolean = true,
    var showEmptyState: Boolean = false
)

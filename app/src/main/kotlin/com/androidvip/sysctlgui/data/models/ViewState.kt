package com.androidvip.sysctlgui.data.models

data class ViewState<T>(
    var data: List<T> = listOf(),
    var isLoading: Boolean = false
)

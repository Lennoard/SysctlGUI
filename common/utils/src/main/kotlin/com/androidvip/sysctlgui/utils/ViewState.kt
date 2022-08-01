package com.androidvip.sysctlgui.utils

open class ViewState<T>(
    var data: List<T> = listOf(),
    var isLoading: Boolean = true,
    var showEmptyState: Boolean = false,
    var searchExpression: String = "",
) {
    fun copyState(
        data: List<T> = this.data,
        isLoading: Boolean = this.isLoading,
        showEmptyState: Boolean = this.showEmptyState
    ): ViewState<T> {
        return ViewState(data, isLoading, showEmptyState)
    }
}

package com.androidvip.sysctlgui.ui.search

import com.androidvip.sysctlgui.models.SearchHint
import com.androidvip.sysctlgui.models.UiKernelParam

data class SearchViewState(
    val loading: Boolean = true,
    val searchHints: List<SearchHint> = emptyList(),
    val searchResults: List<UiKernelParam> = emptyList()
)

sealed interface SearchViewEvent {
    data object BackClicked : SearchViewEvent
    data class SearchQueryChange(val query: String) : SearchViewEvent
    data class HistoryItemRemoveClicked(val hint: SearchHint) : SearchViewEvent
    data class SearchRequested(val query: String) : SearchViewEvent
    data class ParamClicked(val param: UiKernelParam) : SearchViewEvent
}

sealed interface SearchViewEffect {
    data object NavigateBack : SearchViewEffect
    data class EditKernelParam(val param: UiKernelParam) : SearchViewEffect
}

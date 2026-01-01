package com.androidvip.sysctlgui.ui.search

import androidx.compose.runtime.Stable
import com.androidvip.sysctlgui.models.SearchHint
import com.androidvip.sysctlgui.models.UiKernelParam

@Stable
data class SearchViewState(
    val loading: Boolean = false,
    val searchHints: List<SearchHint> = emptyList(),
    val searchResults: List<UiKernelParam> = emptyList()
)

sealed interface SearchViewEvent {
    data class SearchQueryChange(val query: String) : SearchViewEvent
    data class HistoryItemRemoveClicked(val hint: SearchHint) : SearchViewEvent
    data class SearchRequested(val query: String) : SearchViewEvent
    data class ParamClicked(val param: UiKernelParam) : SearchViewEvent
}

sealed interface SearchViewEffect {
    data class EditKernelParam(val param: UiKernelParam) : SearchViewEffect
}

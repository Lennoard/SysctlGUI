package com.androidvip.sysctlgui.ui.search

import androidx.compose.runtime.Stable
import com.androidvip.sysctlgui.models.SearchHint
import com.androidvip.sysctlgui.models.UiKernelParam
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class SearchViewState(
    val loading: Boolean = false,
    val searchHints: ImmutableList<SearchHint> = persistentListOf(),
    val searchResults: ImmutableList<UiKernelParam> = persistentListOf()
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

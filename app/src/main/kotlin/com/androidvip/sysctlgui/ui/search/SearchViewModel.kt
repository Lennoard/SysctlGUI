package com.androidvip.sysctlgui.ui.search

import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.helpers.UiKernelParamMapper
import com.androidvip.sysctlgui.models.SearchHint
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val getUserParams: GetUserParamsUseCase,
    private val getRuntimeParams: GetRuntimeParamsUseCase,
    private val appPrefs: AppPrefs
) : BaseViewModel<SearchViewEvent, SearchViewState, SearchViewEffect>() {
    private var preSearchJob: Job? = null
    private val searchableParams = mutableListOf<KernelParam>()
    private val searchHistory = mutableListOf<String>()

    init {
        viewModelScope.launch {
            val fetchedHistory = appPrefs.searchHistory
            val fetchedParams = fetchParams()

            searchHistory.addAll(fetchedHistory)
            searchableParams.addAll(fetchedParams)

            val uiSearchHints = fetchedHistory
                .map { SearchHint(hint = it, isFromHistory = true) }
                .takeLast(MAX_SEARCH_HINTS)

            setState {
                copy(
                    loading = false,
                    searchHints = uiSearchHints
                )
            }
        }
    }

    override fun createInitialState() = SearchViewState()

    private suspend fun fetchParams(): List<UiKernelParam> {
        val userParams = getUserParams()
        return getRuntimeParams(userParams).map(UiKernelParamMapper::map)
    }

    override fun onEvent(event: SearchViewEvent) {
        when (event) {
            is SearchViewEvent.HistoryItemRemoveClicked -> handleRemoveFromHistory(event.hint)
            is SearchViewEvent.SearchRequested -> handleSearch(event.query)
            is SearchViewEvent.SearchQueryChange -> handleSearchQueryChange(event.query)
            is SearchViewEvent.ParamClicked -> setEffect {
                SearchViewEffect.EditKernelParam(event.param)
            }
        }
    }

    private fun handleSearchQueryChange(query: String) {
        preSearchJob?.cancel()
        if (query.isEmpty()) {
            setState { copy(searchResults = emptyList()) }
        } else if (query.length >= MIN_PRE_SEARCH_QUERY_LENGTH) {
            preSearchJob = viewModelScope.launch {
                delay(300L) // Debounce delay
                handlePreSearch(query)
            }
        }
    }

    private fun handleSearch(query: String) {
        viewModelScope.launch {
            setState { copy(loading = true) }
            searchHistory.add(query)
            appPrefs.addSearchToHistory(query)

            val searchResults = withContext(Dispatchers.IO) {
                searchableParams
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .map(UiKernelParamMapper::map)
            }

            setState {
                copy(loading = false, searchResults = searchResults)
            }
        }
    }

    private fun handlePreSearch(query: String) {
        viewModelScope.launch {
            val hints = withContext(Dispatchers.IO) {
                val historyHints = searchHistory
                    .toList()
                    .take(MAX_SEARCH_HISTORY)
                    .map { SearchHint(hint = it, isFromHistory = true) }

                val paramHints = searchableParams
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .take(MAX_SEARCH_HINTS)
                    .map { SearchHint(it.name) }

                historyHints + paramHints
            }

            setState {
                copy(loading = false, searchHints = hints)
            }
        }
    }

    private fun handleRemoveFromHistory(searchHint: SearchHint) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appPrefs.removeSearchFromHistory(searchHint.hint)
                searchHistory.remove(searchHint.hint)
            }

            setState {
                copy(
                    searchHints = searchHistory
                        .take(MAX_SEARCH_HISTORY)
                        .map { SearchHint(hint = it, isFromHistory = true) }
                )
            }
        }
    }

    companion object {
        private const val MIN_PRE_SEARCH_QUERY_LENGTH = 4
        private const val MAX_SEARCH_HISTORY = 3
        private const val MAX_SEARCH_HINTS = 5
    }
}

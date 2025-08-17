package com.androidvip.sysctlgui.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.design.utils.isLandscape
import com.androidvip.sysctlgui.models.SearchHint
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import com.androidvip.sysctlgui.ui.params.browse.ParamRow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    outerScaffoldPadding: PaddingValues,
    onParamSelected: (UiKernelParam) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    showTopBar = false,
                    showNavBar = false,
                )
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchViewEffect.EditKernelParam -> onParamSelected(effect.param)
            }
        }
    }

    SearchScreenContent(
        outerScaffoldPadding = outerScaffoldPadding,
        searchQuery = searchQuery,
        onSearchQueryChange = {
            searchQuery = it
            viewModel.onEvent(SearchViewEvent.SearchQueryChange(it))
        },
        searchActive = searchActive,
        onSearchActiveChange = { searchActive = it },
        state = state,
        onHistoryItemRemoveClicked = {
            viewModel.onEvent(SearchViewEvent.HistoryItemRemoveClicked(it))
        },
        onParamClicked = {
            viewModel.onEvent(SearchViewEvent.ParamClicked(it))
        },
        onSearch = { query ->
            searchActive = false
            viewModel.onEvent(SearchViewEvent.SearchRequested(query))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    state: SearchViewState,
    outerScaffoldPadding: PaddingValues = PaddingValues(),
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onHistoryItemRemoveClicked: (SearchHint) -> Unit,
    onParamClicked: (UiKernelParam) -> Unit,
    onSearch: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val navHostTopPadding = outerScaffoldPadding.calculateTopPadding()
    val searchBarHorizontalPadding by animateDpAsState(
        targetValue = if (searchActive) 0.dp else 16.dp,
        label = "SearchBarHorizontalPadding",
        animationSpec = tween(durationMillis = 300)
    )
    val searchBarTopPadding by animateDpAsState(
        targetValue = if (searchActive) 0.dp else 8.dp,
        label = "SearchBarTopPadding",
        animationSpec = tween(durationMillis = 300)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = -navHostTopPadding),
        topBar = {
            val onActiveChange: (Boolean) -> Unit = { isActive ->
                if (searchActive != isActive) {
                    onSearchActiveChange(isActive)
                }
                if (!isActive && searchQuery.isNotEmpty()) {
                    onSearchQueryChange("")
                }
            }
            val searchBarColors = SearchBarDefaults.colors()
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearch,
                        expanded = searchActive,
                        onExpandedChange = onActiveChange,
                        placeholder = { Text(stringResource(R.string.search_title)) },
                        leadingIcon = {
                            AnimatedContent(
                                targetState = searchActive,
                                label = "SearchIconsAnimation"
                            ) { targetSearchActive ->
                                if (targetSearchActive) {
                                    IconButton(onClick = {
                                        focusManager.clearFocus()
                                        onSearchActiveChange(false)
                                        onSearchQueryChange("")
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = stringResource(R.string.go_back)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(
                                            R.string.acessibility_search_icon
                                        )
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty() && searchActive,
                                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                            ) {
                                IconButton(onClick = {
                                    onSearchQueryChange("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = stringResource(R.string.clear_search)
                                    )
                                }
                            }
                        },
                        colors = searchBarColors.inputFieldColors,
                    )
                },
                expanded = searchActive,
                onExpandedChange = onActiveChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = searchBarHorizontalPadding)
                    .padding(top = searchBarTopPadding),
                shape = SearchBarDefaults.inputFieldShape,
                colors = searchBarColors,
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets,
            ) {
                SearchViewContent(
                    searchHints = state.searchHints,
                    onHistoryItemRemoveClicked = onHistoryItemRemoveClicked,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    onSearchActiveChange = onSearchActiveChange,
                    searchQuery = searchQuery
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.loading,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (it) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                SearchResultsContent(
                    searchResults = state.searchResults,
                    searchQuery = searchQuery,
                    onParamClicked = onParamClicked
                )
            }
        }
    }
}

@Composable
private fun SearchViewContent(
    searchHints: List<SearchHint>,
    onHistoryItemRemoveClicked: (SearchHint) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    searchQuery: String
) {
    val historyHints = searchHints.filter { it.isFromHistory }
    val suggestionHints = searchHints.filter { !it.isFromHistory }
    val columnsCount = if (isLandscape()) 2 else 1
    val hintItemColumns = GridCells.Fixed(columnsCount)

    LazyVerticalGrid(
        columns = hintItemColumns,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (historyHints.isNotEmpty()) {
            item(
                key = "history_header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Text(
                    text = stringResource(R.string.recent_searches),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(historyHints, key = { "history:${it.hint}" }) { historyItem ->
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    headlineContent = { Text(historyItem.hint) },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = stringResource(R.string.history_item)
                        )
                    },
                    trailingContent = {
                        IconButton(
                            onClick = { onHistoryItemRemoveClicked(historyItem) },
                            modifier = Modifier.offset(16.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Clear,
                                contentDescription = stringResource(R.string.clear_history_item)
                            )
                        }
                    },
                    modifier = Modifier
                        .clickable {
                            onSearchQueryChange(historyItem.hint)
                            onSearch(historyItem.hint)
                            onSearchActiveChange(false)
                        }
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (suggestionHints.isNotEmpty()) {
                item(
                    key = "history_suggestion_divider",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }

        if (suggestionHints.isNotEmpty()) {
            item(
                key = "suggestions_header",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Text(
                    text = stringResource(R.string.suggestions),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(suggestionHints, key = { "hint:${it.hint}" }) { hintItem ->
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    headlineContent = { Text(hintItem.hint) },
                    modifier = Modifier
                        .clickable {
                            onSearchQueryChange(hintItem.hint)
                            onSearch(hintItem.hint)
                            onSearchActiveChange(false)
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp) // Padding for grid spacing
                )
            }
        }

        if (searchHints.isEmpty() && searchQuery.isBlank()) {
            item(
                key = "empty_search_suggestions",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.search_no_suggestions))
                }
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    searchResults: List<UiKernelParam>,
    searchQuery: String,
    onParamClicked: (UiKernelParam) -> Unit
) {
    val columns = if (isLandscape()) 2 else 1
    if (searchResults.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            itemsIndexed(searchResults) { index, param ->
                ParamRow(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(4.dp),
                    param = param,
                    showFullName = true,
                    onParamClicked = onParamClicked
                )

                if (columns == 1 && index < searchResults.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    } else if (searchQuery.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.search_no_results_format, searchQuery),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.search_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
@Preview
private fun SearchScreenPreview() {
    SysctlGuiTheme {
        var searchQuery by remember { mutableStateOf("") }
        var searchActive by remember { mutableStateOf(false) }
        var searchHints by remember {
            mutableStateOf(
                listOf(
                    SearchHint("vm.swappiness", isFromHistory = false),
                    SearchHint("net.ipv4.tcp_congestion_control", isFromHistory = false),
                    SearchHint("kernel.panic", isFromHistory = true),
                    SearchHint("fs.file-max", isFromHistory = true)
                )
            )
        }
        var searchResults by remember {
            mutableStateOf(
                listOf(
                    UiKernelParam(
                        name = "vm.swappiness",
                        path = "/proc/sys/vm/swappiness",
                        isFavorite = false
                    ),
                    UiKernelParam(
                        name = "vm.overcommit_memory",
                        path = "/proc/sys/vm/overcommit_memory",
                        value = "1",
                        isFavorite = true
                    ),
                    UiKernelParam(
                        name = "net.ipv4.tcp_congestion_control",
                        path = "/proc/sys/net/ipv4/tcp_congestion_control",
                        value = "cubic"
                    )
                )
            )
        }

        SearchScreenContent(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchActive = searchActive,
            onSearchActiveChange = { searchActive = it },
            state = SearchViewState(
                searchHints = searchHints,
                searchResults = searchResults
            ),
            onHistoryItemRemoveClicked = { searchHints = searchHints - it },
            onParamClicked = {},
            onSearch = {
                searchResults = searchResults.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }
            },
        )
    }
}

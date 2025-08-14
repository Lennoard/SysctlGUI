package com.androidvip.sysctlgui.ui.params.browse

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import com.androidvip.sysctlgui.ui.params.DocumentationBottomSheet
import com.androidvip.sysctlgui.utils.browse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ParamBrowseScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    viewModel: ParamBrowseViewModel = koinViewModel(),
    onParamSelected: (KernelParam) -> Unit
) {
    var documentation by remember { mutableStateOf<ParamDocumentation?>(null) }
    val documentationSheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    showTopBar = true,
                    showNavBar = true,
                    showBackButton = state.backEnabled,
                    showSearchAction = true
                )
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ParamBrowseViewEffect.EditKernelParam -> onParamSelected(effect.param)
                is ParamBrowseViewEffect.OpenBrowser -> context.browse(effect.url)
                is ParamBrowseViewEffect.ShowError -> Toast.makeText(
                    context,
                    effect.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    ParamBrowseScreenContent(
        params = state.params,
        currentPath = state.currentPath,
        documentation = state.documentation,
        onParamClicked = {
            viewModel.onEvent(ParamBrowseViewEvent.ParamClicked(it))
        },
        onDocumentationClicked = {
            viewModel.onEvent(ParamBrowseViewEvent.DocumentationClicked(it))
        },
        backEnabled = state.backEnabled,
        onBackPressed = {
            viewModel.onEvent(ParamBrowseViewEvent.BackRequested)
        },
        isRefreshing = state.loading,
        onRefresh = { viewModel.onEvent(ParamBrowseViewEvent.RefreshRequested) }
    )

    documentation?.let {
        DocumentationBottomSheet(
            documentation = it,
            sheetState = documentationSheetState
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ParamBrowseScreenContent(
    params: List<UiKernelParam>,
    currentPath: String,
    documentation: ParamDocumentation?,
    onParamClicked: (UiKernelParam) -> Unit,
    onDocumentationClicked: (ParamDocumentation) -> Unit,
    backEnabled: Boolean = false,
    onBackPressed: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)
    var headerVisible by remember { mutableStateOf(backEnabled) }

    BackHandler(enabled = backEnabled, onBack = onBackPressed)

    LaunchedEffect(listState) {
        var previousOffset = listState.firstVisibleItemScrollOffset
        var previousIndex = listState.firstVisibleItemIndex

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .map { (currentIndex, currentOffset) ->
                when {
                    currentIndex > previousIndex -> false
                    currentIndex < previousIndex -> true
                    currentOffset > previousOffset -> false
                    currentOffset < previousOffset -> true
                    else -> null // No change or unable to determine (keep current state)
                }.also {
                    previousIndex = currentIndex
                    previousOffset = currentOffset
                }
            }
            .filter { it != null }
            .distinctUntilChanged()
            .collect { scrolledUp ->
                headerVisible = scrolledUp ?: headerVisible
            }
    }

    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    val finalHeaderVisible = (headerVisible || isAtTop) && backEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
    ) {
        val spacerHeight by animateDpAsState(if (finalHeaderVisible) 56.dp else 0.dp)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item { Spacer(modifier = Modifier.height(spacerHeight)) }

            items(
                count = params.size,
                key = { index -> params[index].name }
            ) { index ->
                ParamFileRow(
                    modifier = Modifier.animateItem(),
                    param = params[index],
                    onParamClicked = onParamClicked,
                )
            }

            if (documentation != null) {
                item { Spacer(modifier = Modifier.height(56.dp)) }
            }
        }

        AnimatedVisibility(
            visible = finalHeaderVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            InfoItem(
                text = currentPath,
                icon = painterResource(R.drawable.ic_arrow_upward),
                onClicked = onBackPressed
            )
        }

        AnimatedVisibility(
            visible = finalHeaderVisible && documentation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            InfoItem(
                text = "Read documentation for \"${documentation?.title}\"",
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
                ),
                icon = painterResource(R.drawable.ic_documentation),
                onClicked = { onDocumentationClicked(documentation!!) }
            )
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun InfoItem(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    icon: Painter,
    onClicked: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClicked,
                onLongClick = { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() },
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { this.contentDescription = text },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = textStyle.color
        )
        Text(
            text = text,
            style = textStyle,
            maxLines = 2,
            overflow = TextOverflow.MiddleEllipsis
        )
    }
}

@Composable
@PreviewLightDark
internal fun ParamBrowseScreenContentPreview() {
    fun mapFilesToParams(files: Array<File>?): List<UiKernelParam> {
        return files?.map { file ->
            UiKernelParam(
                name = file.name,
                path = file.path,
                value = "",
                isFavorite = (0..5).random() % 2 == 0
            )
        } ?: emptyList()
    }

    val root = File("/")
    var currentPath by remember { mutableStateOf(root.path) }
    var params by remember(currentPath) {
        mutableStateOf(mapFilesToParams(File(currentPath).listFiles()))
    }
    var isRefreshingPreview by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ParamBrowseScreenContent(
                params = params,
                currentPath = currentPath,
                documentation = ParamDocumentation(
                    title = currentPath,
                    documentationText = "Documentation for $currentPath",
                    url = null
                ),
                onParamClicked = {
                    if (it.isDirectory) {
                        currentPath = it.path
                        params = mapFilesToParams(File(it.path).listFiles())
                    }
                },
                onDocumentationClicked = {},
                backEnabled = currentPath != root.path,
                onBackPressed = { currentPath = File(currentPath).parent ?: root.path },
                isRefreshing = isRefreshingPreview,
                onRefresh = {
                    scope.launch {
                        isRefreshingPreview = true
                        delay(2.seconds)
                        isRefreshingPreview = false
                    }
                }
            )
        }
    }
}

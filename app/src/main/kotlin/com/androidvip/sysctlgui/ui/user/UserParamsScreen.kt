package com.androidvip.sysctlgui.ui.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.ui.main.MainViewEffect
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import com.androidvip.sysctlgui.ui.params.browse.ParamFileRow
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

const val ANIMATION_DURATION = 300

@Composable
fun UserParamsScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    viewModel: UserParamsViewModel = koinViewModel(),
    filterPredicate: (UiKernelParam) -> Boolean = { true },
    onParamSelected: (KernelParam) -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(UserParamsViewEvent.ScreenLoaded(filterPredicate))

        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    showTopBar = true,
                    showNavBar = true,
                    showBackButton = false,
                    showSearchAction = true
                )
            )
        )

        mainViewModel.effect.collect { effect ->
            if (effect is MainViewEffect.ActUponSckbarActionPerformed) {
                viewModel.onEvent(UserParamsViewEvent.ParamRestoreRequested)
            }
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserParamsViewEffect.ShowParamDetails -> {
                    onParamSelected(effect.param)
                }

                is UserParamsViewEffect.ShowUndoSnackBar -> {
                    mainViewModel.onEvent(
                        MainViewEvent.ShowSnackbarRequested(
                            message = context.getString(R.string.favorite_param_deleted_format, effect.param.name),
                            actionLabel = context.getString(R.string.undo)
                        )
                    )
                }
            }
        }
    }

    FavoritesScreenContent(
        favoriteParams = state.userParams,
        loading = state.loading,
        onParamClicked = {
            viewModel.onEvent(UserParamsViewEvent.ParamClicked(it))
        },
        onParamDeleteRequested = {
            viewModel.onEvent(UserParamsViewEvent.ParamDeleteRequested(it))
        }
    )
}

@Composable
private fun FavoritesScreenContent(
    favoriteParams: List<UiKernelParam>,
    loading: Boolean,
    onParamClicked: (UiKernelParam) -> Unit,
    onParamDeleteRequested: (UiKernelParam) -> Unit
) {
    val listState = rememberLazyListState()

    AnimatedVisibility(visible = loading, enter = fadeIn(), exit = fadeOut()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (favoriteParams.isEmpty() && !loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_heart_broken),
                contentDescription = "Empty",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(128.dp)
            )
            Text(
                text = stringResource(R.string.empty_favorites_widget),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = favoriteParams.size,
            key = { index -> favoriteParams[index].name }
        ) { index ->
            val param = favoriteParams[index]
            var showParam by remember { mutableStateOf(true) }
            val dismissState = rememberSwipeToDismissBoxState()

            LaunchedEffect(showParam, param) {
                if (!showParam) {
                    delay(ANIMATION_DURATION.milliseconds)
                    onParamDeleteRequested(param)
                }
            }

            AnimatedVisibility(
                visible = showParam,
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = ANIMATION_DURATION),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(durationMillis = ANIMATION_DURATION))
            ) {
                val swipeBackgroundColor = MaterialTheme.colorScheme.errorContainer
                val swipeContentColor = MaterialTheme.colorScheme.onErrorContainer

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    onDismiss = { showParam = false },
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> swipeBackgroundColor
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_sweep),
                                contentDescription = "Delete",
                                tint = swipeContentColor
                            )
                        }
                    }
                ) {
                    ParamFileRow(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        param = param,
                        showFavoriteIcon = false,
                        onParamClicked = onParamClicked
                    )
                }
            }

            if (index < favoriteParams.lastIndex && showParam) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
@Preview
private fun FavoriteScreenContentPreview() {
    val params = listOf(
        UiKernelParam(
            name = "vm.swappiness",
            path = "/proc/sys/vm/swappiness",
            value = "100",
            isFavorite = true
        ),
        UiKernelParam(
            name = "vm.overcommit_memory",
            path = "/proc/sys/vm/overcommit_memory",
            value = "1",
            isFavorite = true
        ),
        UiKernelParam(
            name = "vm.overcommit_ratio",
            path = "/proc/sys/vm/overcommit_ratio",
            value = "2",
            isFavorite = true
        )
    )
    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            FavoritesScreenContent(
                favoriteParams = params,
                loading = false,
                onParamClicked = {},
                onParamDeleteRequested = {}
            )
        }
    }
}

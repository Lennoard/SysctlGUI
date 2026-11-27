package com.androidvip.sysctlgui.ui.main

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    startDestination: UiRoute = UiRoute.BrowseParams
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val showSnackbar: (String, String?, () -> Unit) -> Unit = { message, actionLabel, onAction ->
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onAction()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            if (effect is MainViewEffect.ShowSnackbar) {
                val result = snackbarHostState.showSnackbar(
                    message = effect.message,
                    actionLabel = effect.actionLabel,
                    duration = SnackbarDuration.Long
                )
                viewModel.onEvent(MainViewEvent.OnSnackbarResult(result))
            }
        }
    }

    MainScreenContent(state, navController, snackbarHostState, startDestination, showSnackbar)
}

@Composable
private fun MainScreenContent(
    state: MainViewState,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    startDestination: UiRoute = UiRoute.BrowseParams,
    showSnackbar: (String, String?, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val showNavigationRail = windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    )
    val topLevelRoutes = remember(context) { TopLevelRouteProvider(context) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelDestination = remember(currentDestination) {
        topLevelRoutes.any { topLevel ->
            currentDestination?.hasRoute(topLevel.route::class) == true
        }
    }

    val topLevelProgress by animateFloatAsState(
        targetValue = if (isTopLevelDestination) 1f else 0f,
        animationSpec = if (isTopLevelDestination) tween(200) else tween(250),
        label = "TopLevelProgressAnimation"
    )

    val effectiveProgress = maxOf(topLevelProgress, state.predictiveBackProgress)
    val isNavVisible = effectiveProgress > 0.001f

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AnimatedVisibility(
                visible = state.showTopBar,
                enter = expandVertically() + slideInVertically() + fadeIn(),
                exit = shrinkVertically() + slideOutVertically() + fadeOut(),
                label = "TopBar"
            ) {
                MainTopBar(
                    title = state.topBarTitle,
                    showSearch = state.showSearchAction,
                    showBack = state.showBackButton,
                    onDeleteAllPressed = state.deleteAllHistoryAction,
                    onSearchPressed = { navController.navigate(UiRoute.Search) },
                    onBackPressed = {
                        onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }
                )
            }
        },
        bottomBar = {
            if (!showNavigationRail) {
                if (isTopLevelDestination && state.showNavBar) {
                    MainNavBar(navController = navController)
                } else if (!isTopLevelDestination && isNavVisible) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = effectiveProgress
                            translationY = 100.dp.toPx() * (1f - effectiveProgress)
                        }
                    ) {
                        MainNavBar(navController = navController)
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        content = { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                Row(Modifier.fillMaxSize()) {
                    if (showNavigationRail && isTopLevelDestination && state.showNavBar) {
                        MainNavRail(navController = navController)
                    }

                    AppNavHost(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (showNavigationRail && isTopLevelDestination) Modifier.navigationBarsPadding() else Modifier),
                        innerPadding = innerPadding,
                        navController = navController,
                        startDestination = startDestination,
                        showSnackbar = showSnackbar
                    )
                }

                if (showNavigationRail && !isTopLevelDestination && isNavVisible) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = effectiveProgress
                            translationX = -100.dp.toPx() * (1f - effectiveProgress)
                        }
                    ) {
                        MainNavRail(navController = navController)
                    }
                }
            }
        }
    )
}

@Composable
@PreviewLightDark
@PreviewDynamicColors
private fun MainScreenPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        MainScreenContent(
            state = MainViewState(),
            navController = rememberNavController(),
            snackbarHostState = remember { SnackbarHostState() },
            showSnackbar = { _, _, _ -> }
        )
    }
}

package com.androidvip.sysctlgui.ui.main

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
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

    MainScreenContent(state, navController, snackbarHostState)
}

@Composable
private fun MainScreenContent(
    state: MainViewState,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    Scaffold(
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
                    onSearchPressed = { navController.navigate(UiRoute.Search) },
                    onBackPressed = {
                        onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.showNavBar,
                enter = expandVertically() + slideInVertically { it / 2 } + fadeIn(),
                exit = shrinkVertically() + slideOutVertically { it / 2 } + fadeOut(),
                label = "BottomBar"
            ) {
                MainNavBar(navController = navController)
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        content = { innerPadding ->
            AppNavHost(
                innerPadding = innerPadding,
                navController = navController
            )
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
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

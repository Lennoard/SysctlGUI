package com.androidvip.sysctlgui.ui.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.ui.components.PredictiveBackContainer
import com.androidvip.sysctlgui.ui.history.ParamChangeHistoryScreen
import com.androidvip.sysctlgui.ui.params.browse.ParamBrowseScreen
import com.androidvip.sysctlgui.ui.params.browse.ParamBrowseScreenContentPreview
import com.androidvip.sysctlgui.ui.params.edit.EditParamScreen
import com.androidvip.sysctlgui.ui.presets.ImportPresetScreen
import com.androidvip.sysctlgui.ui.presets.PresetsScreen
import com.androidvip.sysctlgui.ui.search.SearchScreen
import com.androidvip.sysctlgui.ui.settings.SettingsScreen
import com.androidvip.sysctlgui.ui.user.UserParamsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AppNavHost(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues, // Handled in the outer scope, this is for inner scaffolds
    navController: NavHostController,
    mainViewModel: MainViewModel = koinViewModel(),
    startDestination: UiRoute = UiRoute.BrowseParams,
    showSnackbar: (String, String?, () -> Unit) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut() }
    ) {
        composable<UiRoute.BrowseParams> {
            if (LocalView.current.isInEditMode) {
                ParamBrowseScreenContentPreview()
            } else {
                ParamBrowseScreen(
                    onParamSelect = {
                        navController.navigate(UiRoute.EditParam(paramName = it.name))
                    }
                )
            }
        }

        composable<UiRoute.EditParam> {
            PredictiveBackContainer(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            ) {
                EditParamScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToParam = { paramName ->
                        navController.navigate(UiRoute.EditParam(paramName))
                    },
                    showSnackbar = showSnackbar
                )
            }
        }

        composable<UiRoute.Presets> {
            PresetsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImport = { navController.navigate(UiRoute.ImportPresets) }
            )
        }

        composable<UiRoute.ImportPresets> {
            PredictiveBackContainer(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            ) {
                ImportPresetScreen(onNavigateBack = { navController.popBackStack() })
            }
        }

        composable<UiRoute.Favorites> {
            UserParamsScreen(
                filterPredicate = { it.isFavorite },
                onParamSelect = {
                    navController.navigate(UiRoute.EditParam(paramName = it.name))
                }
            )
        }

        composable<UiRoute.Search> {
            PredictiveBackContainer(
                mainViewModel = mainViewModel,
                onBack = { navController.popBackStack() }
            ) {
                SearchScreen(
                    outerScaffoldPadding = innerPadding,
                    onNavigateBack = { navController.popBackStack() },
                    onParamSelect = {
                        navController.navigate(UiRoute.EditParam(paramName = it.name))
                    }
                )
            }
        }

        composable<UiRoute.Settings> {
            SettingsScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable<UiRoute.ParamChangeHistory> {
            ParamChangeHistoryScreen(showSnackbar = showSnackbar)
        }

        composable<UiRoute.UserParams> {
            UserParamsScreen(
                filterPredicate = { true },
                onParamSelect = {
                    navController.navigate(UiRoute.EditParam(paramName = it.name))
                }
            )
        }
    }
}

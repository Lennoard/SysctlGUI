package com.androidvip.sysctlgui.ui.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.ui.params.browse.ParamBrowseScreen
import com.androidvip.sysctlgui.ui.params.browse.ParamBrowseScreenContentPreview
import com.androidvip.sysctlgui.ui.params.edit.EditParamScreen
import com.androidvip.sysctlgui.ui.presets.ImportPresetScreen
import com.androidvip.sysctlgui.ui.presets.PresetsScreen
import com.androidvip.sysctlgui.ui.search.SearchScreen
import com.androidvip.sysctlgui.ui.settings.SettingsScreen
import com.androidvip.sysctlgui.ui.user.UserParamsScreen

@Composable
internal fun AppNavHost(
    innerPadding: PaddingValues,
    navController: NavHostController,
    startDestination: UiRoute = UiRoute.BrowseParams
) {
    NavHost(
        modifier = Modifier.padding(innerPadding),
        navController = navController,
        startDestination = startDestination,
        enterTransition = { scaleIn(initialScale = 0.9f) + fadeIn() },
        exitTransition = { scaleOut(targetScale = 0.9f) + fadeOut() },
        popEnterTransition = { scaleIn(initialScale = 1.1f) + fadeIn() },
        popExitTransition = { scaleOut(targetScale = 1.1f) + fadeOut() }
    ) {
        composable<UiRoute.BrowseParams> {
            if (LocalView.current.isInEditMode) {
                ParamBrowseScreenContentPreview()
            } else {
                ParamBrowseScreen(
                    onParamSelected = {
                        navController.navigate(UiRoute.EditParam(paramName = it.name))
                    }
                )
            }
        }

        composable<UiRoute.EditParam> {
            EditParamScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<UiRoute.Presets> {
            PresetsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImport = { navController.navigate(UiRoute.ImportPresets) }
            )
        }

        composable<UiRoute.ImportPresets> {
            ImportPresetScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<UiRoute.Favorites> {
            UserParamsScreen(
                filterPredicate = { it.isFavorite },
                onParamSelected = {
                    navController.navigate(UiRoute.EditParam(paramName = it.name))
                }
            )
        }

        composable<UiRoute.Search> {
            SearchScreen(
                outerScaffoldPadding = innerPadding,
                onParamSelected = {
                    navController.navigate(UiRoute.EditParam(paramName = it.name))
                }
            )
        }

        composable<UiRoute.Settings> {
            SettingsScreen(
                onNavigateToUserParams = {
                    navController.navigate(UiRoute.UserParams)
                }
            )
        }

        composable<UiRoute.UserParams> {
            UserParamsScreen(
                filterPredicate = { true },
                onParamSelected = {
                    navController.navigate(UiRoute.EditParam(paramName = it.name))
                }
            )
        }
    }
}

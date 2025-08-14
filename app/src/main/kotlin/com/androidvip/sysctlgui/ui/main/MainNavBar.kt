package com.androidvip.sysctlgui.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.core.navigation.TopLevelRoute
import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme

@Composable
internal fun MainNavBar(navController: NavHostController = rememberNavController()) {
    val browseParamsTitle = stringResource(R.string.browse)
    val presetsTitle = stringResource(R.string.presets)
    val favoritesTitle = stringResource(R.string.favorites)
    val settingsTitle = stringResource(R.string.settings)
    val topLevelRoutes = remember {
        listOf(
            TopLevelRoute(
                name = browseParamsTitle,
                route = UiRoute.BrowseParams,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            TopLevelRoute(
                name = presetsTitle,
                route = UiRoute.Presets,
                selectedIcon = Icons.Rounded.Build,
                unselectedIcon = Icons.Outlined.Build
            ),
            TopLevelRoute(
                name = favoritesTitle,
                route = UiRoute.Favorites,
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder
            ),
            TopLevelRoute(
                name = settingsTitle,
                route = UiRoute.Settings,
                selectedIcon = Icons.Rounded.Settings,
                unselectedIcon = Icons.Outlined.Settings
            )
        )
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        topLevelRoutes.forEach { route ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.hasRoute(route.route::class) } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) route.selectedIcon else route.unselectedIcon,
                        contentDescription = route.name,
                    )
                },
                label = {
                    Text(
                        text = route.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(route.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
@PreviewLightDark
@PreviewDynamicColors
private fun MainNavbarPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        MainNavBar()
    }
}

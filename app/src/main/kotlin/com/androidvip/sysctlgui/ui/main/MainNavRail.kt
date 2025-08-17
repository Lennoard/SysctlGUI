package com.androidvip.sysctlgui.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme

@Composable
internal fun MainNavRail(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val topLevelRoutes = remember { TopLevelRouteProvider(context) }

    NavigationRail {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        topLevelRoutes.forEach { route ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.hasRoute(route.route::class) } == true

            NavigationRailItem(
                icon = {
                    AnimatedContent(targetState = selected) { selectedState ->
                        Icon(
                            imageVector = if (selectedState) {
                                route.selectedIcon
                            } else {
                                route.unselectedIcon
                            },
                            contentDescription = route.name,
                        )
                    }
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
private fun MainNavbarPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        MainNavRail()
    }
}

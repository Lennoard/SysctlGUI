package com.androidvip.sysctlgui.ui.main

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
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

            val iconRes = if (selected) route.selectedAnimatedIconRes else route.unselectedAnimatedIconRes
            val imageVector = AnimatedImageVector.animatedVectorResource(id = iconRes)
            val animatedPainter = rememberAnimatedVectorPainter(
                animatedImageVector = imageVector,
                atEnd = selected
            )

            NavigationRailItem(
                icon = { Icon(painter = animatedPainter, contentDescription = route.name) },

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

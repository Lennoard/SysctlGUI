package com.androidvip.sysctlgui.ui.main

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.core.navigation.TopLevelRoute
import com.androidvip.sysctlgui.core.navigation.UiRoute

object TopLevelRouteProvider {
    operator fun invoke(context: Context): List<TopLevelRoute<out UiRoute>> {
        return listOf(
            TopLevelRoute(
                name = context.getString(R.string.browse),
                route = UiRoute.BrowseParams,
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            TopLevelRoute(
                name = context.getString(R.string.presets),
                route = UiRoute.Presets,
                selectedIcon = Icons.Rounded.Build,
                unselectedIcon = Icons.Outlined.Build
            ),
            TopLevelRoute(
                name = context.getString(R.string.favorites),
                route = UiRoute.Favorites,
                selectedIcon = Icons.Rounded.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder
            ),
            TopLevelRoute(
                name = context.getString(R.string.settings),
                route = UiRoute.Settings,
                selectedIcon = Icons.Rounded.Settings,
                unselectedIcon = Icons.Outlined.Settings
            )
        )
    }
}

package com.androidvip.sysctlgui.ui.main

import android.content.Context
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.core.navigation.TopLevelRoute
import com.androidvip.sysctlgui.core.navigation.UiRoute

object TopLevelRouteProvider {
    operator fun invoke(context: Context): List<TopLevelRoute<out UiRoute>> {
        return listOf(
            TopLevelRoute(
                name = context.getString(R.string.browse),
                route = UiRoute.BrowseParams,
                selectedIconRes = R.drawable.ic_home_filled,
                unselectedIconRes = R.drawable.ic_home
            ),
            TopLevelRoute(
                name = context.getString(R.string.presets),
                route = UiRoute.Presets,
                selectedIconRes = R.drawable.ic_build_filled,
                unselectedIconRes = R.drawable.ic_build
            ),
            TopLevelRoute(
                name = context.getString(R.string.favorites),
                route = UiRoute.Favorites,
                selectedIconRes = R.drawable.ic_favorite,
                unselectedIconRes = R.drawable.ic_favorite_outlined
            ),
            TopLevelRoute(
                name = context.getString(R.string.settings),
                route = UiRoute.Settings,
                selectedIconRes = R.drawable.ic_settings_filled,
                unselectedIconRes = R.drawable.ic_settings
            )
        )
    }
}

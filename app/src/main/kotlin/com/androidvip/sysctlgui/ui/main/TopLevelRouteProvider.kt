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
                selectedAnimatedIconRes = R.drawable.avd_home_off,
                unselectedAnimatedIconRes = R.drawable.avd_home_on
            ),
            TopLevelRoute(
                name = context.getString(R.string.presets),
                route = UiRoute.Presets,
                selectedAnimatedIconRes = R.drawable.avd_build_off,
                unselectedAnimatedIconRes = R.drawable.avd_build_on
            ),
            TopLevelRoute(
                name = context.getString(R.string.favorites),
                route = UiRoute.Favorites,
                selectedAnimatedIconRes = R.drawable.avd_favorite_off,
                unselectedAnimatedIconRes = R.drawable.avd_favorite_on
            ),
            TopLevelRoute(
                name = context.getString(R.string.settings),
                route = UiRoute.Settings,
                selectedAnimatedIconRes = R.drawable.avd_settings_off,
                unselectedAnimatedIconRes = R.drawable.avd_settings_on
            )
        )
    }
}

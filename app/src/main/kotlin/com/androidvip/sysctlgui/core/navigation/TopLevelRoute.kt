package com.androidvip.sysctlgui.core.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/**
 * Represents a top-level destination in the application's navigation.
 *
 * @param T The type of [UiRoute] this top-level route represents. This allows for specific
 *          route information to be associated with the top-level destination.
 * @property name The human-readable name of the top-level destination, used for labels.
 * @property route The actual [UiRoute] object that defines the navigation destination.
 * @property selectedIconRes The icon to display when this top-level route is currently selected.
 * @property unselectedIconRes The icon to display when this top-level route is not selected.
 */
@Immutable
data class TopLevelRoute<T : UiRoute>(
    val name: String,
    val route: T,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unselectedIconRes: Int
)

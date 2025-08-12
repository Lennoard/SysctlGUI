package com.androidvip.sysctlgui.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a top-level destination in the application's navigation.
 *
 * @param T The type of [UiRoute] this top-level route represents. This allows for specific
 *          route information to be associated with the top-level destination.
 * @property name The human-readable name of the top-level destination, used for labels.
 * @property route The actual [UiRoute] object that defines the navigation destination.
 * @property selectedIcon The icon to display when this top-level route is currently selected.
 * @property unselectedIcon The icon to display when this top-level route is not selected.
 */
data class TopLevelRoute<T : UiRoute>(
    val name: String,
    val route: T,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

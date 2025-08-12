package com.androidvip.sysctlgui.core.navigation

import kotlinx.serialization.Serializable

/**
 * Represents the different routes in the application's UI.
 * This is used for navigation purposes.
 */
@Serializable
sealed interface UiRoute {
    @Serializable
    data object BrowseParams : UiRoute
    @Serializable
    data class EditParam(val paramName: String) : UiRoute
    @Serializable
    data object Presets : UiRoute
    @Serializable
    data object ImportPresets : UiRoute
    @Serializable
    data object Favorites : UiRoute
    @Serializable
    data object UserParams : UiRoute
    @Serializable
    data object Search : UiRoute
    @Serializable
    data object Settings : UiRoute
}

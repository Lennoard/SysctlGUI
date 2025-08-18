package com.androidvip.sysctlgui.domain.models

import com.androidvip.sysctlgui.domain.enums.SettingItemType


/**
 * Represents an application setting.
 *
 * This data class encapsulates the properties of a single application setting,
 * including its key, current value, enabled state, display information, and type.
 *
 * @param T The type of the setting's value.
 * @property key A unique identifier for the setting.
 * @property value The current value of the setting.
 * @property enabled Indicates whether the setting is currently active or can be modified. Defaults to `true`.
 * @property title A user-friendly name for the setting, displayed in the UI.
 * @property description An optional detailed explanation of what the setting does. Defaults to `null`.
 * @property category The group or section this setting belongs to, used for organization in the UI.
 * @property type Defines how the setting is presented and interacted with in the UI (e.g., switch, list).
 * @property values An optional list of possible values for the setting, typically used for dropdowns or selection lists.
 * @property iconResource An optional resource ID for an icon that can be displayed with the setting.
 */
data class AppSetting<T>(
    val key: String,
    val value: T,
    val enabled: Boolean = true,
    val title: String,
    val description: String? = null,
    val category: String,
    val type: SettingItemType,
    val values: List<T>? = null,
    val iconResource: Int? = null
)

const val KEY_MANAGE_PARAMS = "manageParams"
const val KEY_DELETE_HISTORY = "deleteHistory"
const val KEY_SOURCE_CODE = "sauce"
const val KEY_CONTRIBUTORS = "contributors"
const val KEY_TRANSLATIONS = "translations"
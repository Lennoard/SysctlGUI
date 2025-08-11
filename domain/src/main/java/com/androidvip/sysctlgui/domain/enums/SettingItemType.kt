package com.androidvip.sysctlgui.domain.enums

/**
 * Represents the different types of settings component that can be displayed in the UI.
 * Each type corresponds to a specific UI element used to interact with the setting.
 */
enum class SettingItemType {
    /**
     * Simple setting header with no behavior
     */
    Text,

    /**
     * Represents a switch setting component that can be toggled on or off.
     */
    Switch,

    /**
     * Represents a list of options that can be selected from.
     */
    List,

    /**
     * Represents a slider component that allows the user to select a value within a range.
     */
    Slider
}
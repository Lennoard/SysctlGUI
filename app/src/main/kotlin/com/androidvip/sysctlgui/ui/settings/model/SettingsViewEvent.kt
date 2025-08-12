package com.androidvip.sysctlgui.ui.settings.model

import com.androidvip.sysctlgui.domain.models.AppSetting

sealed interface SettingsViewEvent {
    class SettingValueChanged<T>(val appSetting: AppSetting<T>, val newValue: Any) : SettingsViewEvent
}

sealed interface SettingsViewEffect {
    object RequestNotificationPermission : SettingsViewEffect
}

data class SettingsViewState(
    val settings: List<AppSetting<*>> = emptyList()
)

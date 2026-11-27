package com.androidvip.sysctlgui.ui.settings.model

import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.domain.models.AppSetting
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface SettingsViewEvent {
    class SettingValueChanged<T>(val appSetting: AppSetting<T>, val newValue: Any) : SettingsViewEvent
    class SettingHeaderClicked<T>(val appSetting: AppSetting<T>) : SettingsViewEvent
}

sealed interface SettingsViewEffect {
    object RequestNotificationPermission : SettingsViewEffect
    class OpenBrowser(val url: String) : SettingsViewEffect
    class Navigate(val route: UiRoute) : SettingsViewEffect
    class ShowToast(val message: String) : SettingsViewEffect
    data object ShowRevertDialog : SettingsViewEffect
}

data class SettingsViewState(
    val settings: ImmutableList<AppSetting<*>> = persistentListOf()
)

package com.androidvip.sysctlgui.ui.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.core.navigation.UiRoute
import com.androidvip.sysctlgui.data.Prefs
import com.androidvip.sysctlgui.domain.StringProvider
import com.androidvip.sysctlgui.domain.enums.SettingItemType
import com.androidvip.sysctlgui.domain.models.KEY_CONTRIBUTORS
import com.androidvip.sysctlgui.domain.models.KEY_DELETE_HISTORY
import com.androidvip.sysctlgui.domain.models.KEY_MANAGE_PARAMS
import com.androidvip.sysctlgui.domain.models.KEY_SOURCE_CODE
import com.androidvip.sysctlgui.domain.models.KEY_TRANSLATIONS
import com.androidvip.sysctlgui.domain.usecase.GetAppSettingsUseCase
import com.androidvip.sysctlgui.ui.settings.model.SettingsViewEffect
import com.androidvip.sysctlgui.ui.settings.model.SettingsViewEvent
import com.androidvip.sysctlgui.ui.settings.model.SettingsViewState
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sharedPreferences: SharedPreferences,
    private val getSettings: GetAppSettingsUseCase,
    private val stringProvider: StringProvider
) : BaseViewModel<SettingsViewEvent, SettingsViewState, SettingsViewEffect>() {

    init {
        viewModelScope.launch {
            loadSettings()
        }
    }

    override fun createInitialState() = SettingsViewState()

    override fun onEvent(event: SettingsViewEvent) {
        when (event) {
            is SettingsViewEvent.SettingValueChanged<*> -> {
                if (event.appSetting.type == SettingItemType.Text) return
                if (event.appSetting.key.isEmpty()) return

                when (event.newValue) {
                    is Boolean -> sharedPreferences.edit {
                        putBoolean(event.appSetting.key, event.newValue)
                    }

                    is Int -> sharedPreferences.edit {
                        putInt(event.appSetting.key, event.newValue)
                    }

                    is Long -> sharedPreferences.edit {
                        putLong(event.appSetting.key, event.newValue)
                    }

                    is Float -> sharedPreferences.edit {
                        putFloat(event.appSetting.key, event.newValue)
                    }

                    is String -> sharedPreferences.edit {
                        putString(event.appSetting.key, event.newValue)
                    }

                    is List<*> -> sharedPreferences.edit {
                        val stringValues = event.newValue.filterIsInstance<String>().toSet()
                        putStringSet(event.appSetting.key, stringValues)
                    }
                }

                if (event.appSetting.key == Prefs.RunOnStartup.key) {
                    setEffect { SettingsViewEffect.RequestNotificationPermission }
                }

                viewModelScope.launch {
                    loadSettings()
                }
            }

            is SettingsViewEvent.SettingHeaderClicked<*> -> {
                when (event.appSetting.key) {
                    KEY_MANAGE_PARAMS -> {
                        setEffect { SettingsViewEffect.Navigate(UiRoute.UserParams) }
                    }

                    KEY_DELETE_HISTORY -> {
                        sharedPreferences.edit { remove(Prefs.SearchHistory.key) }
                        setEffect {
                            SettingsViewEffect.ShowToast(stringProvider.getString(R.string.history_deleted))
                        }
                    }

                    KEY_SOURCE_CODE, KEY_CONTRIBUTORS, KEY_TRANSLATIONS -> {
                        setEffect {
                            SettingsViewEffect.OpenBrowser(event.appSetting.value as String)
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadSettings() {
        val settings = getSettings()
        setState { copy(settings = settings) }
    }
}

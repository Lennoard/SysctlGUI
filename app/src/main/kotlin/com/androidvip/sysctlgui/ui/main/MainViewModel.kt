package com.androidvip.sysctlgui.ui.main

import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.data.Prefs
import com.androidvip.sysctlgui.data.repository.CONTRAST_LEVEL_NORMAL
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val appPrefs: AppPrefs
) : BaseViewModel<MainViewEvent, MainViewState, MainViewEffect>() {
    private val themeSettingsFlow: Flow<ThemeSettings> = combine(
        appPrefs.observeKey(Prefs.ForceDarkTheme.key, false),
        appPrefs.observeKey(Prefs.DynamicColors.key, false),
        appPrefs.observeKey(Prefs.ContrastLevel.key, CONTRAST_LEVEL_NORMAL)
    ) { forceDark, dynamicColors, contrastLevel ->
        ThemeSettings(forceDark, dynamicColors, contrastLevel)
    }
    val themeState = themeSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = loadInitialThemeState()
    )

    private fun loadInitialThemeState(): ThemeSettings {
        return ThemeSettings(
            forceDark = appPrefs.forceDark,
            contrastLevel = appPrefs.contrastLevel,
            dynamicColors = appPrefs.dynamicColors
        )
    }

    override fun createInitialState() = MainViewState()

    override fun onEvent(event: MainViewEvent) {
        when (event) {
            is MainViewEvent.OnSateChangeRequested -> {
                setState { event.newState }
            }

            is MainViewEvent.ShowSnackbarRequested -> {
                setEffect { MainViewEffect.ShowSnackbar(event.message, event.actionLabel) }
            }

            is MainViewEvent.OnSnackbarResult -> {
                val snackbarResult = event.result
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    setEffect { MainViewEffect.ActUponSckbarActionPerformed }
                }
            }
        }
    }
}

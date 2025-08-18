package com.androidvip.sysctlgui.ui.main

import androidx.compose.material3.SnackbarResult
import com.androidvip.sysctlgui.data.repository.CONTRAST_LEVEL_NORMAL

data class MainViewState(
    val topBarTitle: String = "Sysctl GUI",
    val showTopBar: Boolean = true,
    val showNavBar: Boolean = true,
    val showBackButton: Boolean = false,
    val showSearchAction: Boolean = true
)

data class ThemeSettings(
    val forceDark: Boolean = false,
    val dynamicColors: Boolean = false,
    val contrastLevel: Int = CONTRAST_LEVEL_NORMAL
)

sealed interface MainViewEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : MainViewEffect
    data object ActUponSckbarActionPerformed : MainViewEffect
}

sealed interface MainViewEvent {
    data class OnSateChangeRequested(val newState: MainViewState) : MainViewEvent
    data class ShowSnackbarRequested(
        val message: String,
        val actionLabel: String? = null
    ) : MainViewEvent

    data class OnSnackbarResult(val result: SnackbarResult) : MainViewEvent
}

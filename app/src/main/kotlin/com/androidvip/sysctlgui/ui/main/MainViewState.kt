package com.androidvip.sysctlgui.ui.main

import androidx.compose.material3.SnackbarResult

data class MainViewState(
    val topBarTitle: String = "SysctlGUI",
    val showTopBar: Boolean = true,
    val showNavBar: Boolean = true,
    val showBackButton: Boolean = false,
    val showSearchAction: Boolean = true
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

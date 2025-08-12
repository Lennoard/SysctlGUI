package com.androidvip.sysctlgui.ui.main

import androidx.compose.material3.SnackbarResult
import com.androidvip.sysctlgui.utils.BaseViewModel

class MainViewModel : BaseViewModel<MainViewEvent, MainViewState, MainViewEffect>() {

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

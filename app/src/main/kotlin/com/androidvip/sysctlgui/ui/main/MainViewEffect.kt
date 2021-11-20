package com.androidvip.sysctlgui.ui.main

sealed interface MainViewEffect {
    object NavigateToKernelList : MainViewEffect
    object NavigateToKernelBrowser : MainViewEffect
    object ExportParams : MainViewEffect
    object NavigateToFavorites : MainViewEffect
    object NavigateToSettings : MainViewEffect
}

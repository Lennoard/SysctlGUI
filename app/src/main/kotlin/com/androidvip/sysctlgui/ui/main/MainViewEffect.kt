package com.androidvip.sysctlgui.ui.main

sealed class MainViewEffect {
    object NavigateToKernelList : MainViewEffect()
    object NavigateToKernelBrowser : MainViewEffect()
    object ImportParamsFromFile : MainViewEffect()
    object NavigateToFavorites : MainViewEffect()
    object NavigateToSettings : MainViewEffect()
}

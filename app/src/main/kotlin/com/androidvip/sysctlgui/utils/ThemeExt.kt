package com.androidvip.sysctlgui.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import org.koin.android.ext.android.get

@Composable
fun Activity.ComposeTheme(content: @Composable () -> Unit) {
    val prefs: AppPrefs = get()
    SysctlGuiTheme(
        forceDark = prefs.forceDark,
        dynamicColor = prefs.dynamicColors,
        content = content
    )
}

@Composable
fun Fragment.ComposeTheme(content: @Composable () -> Unit) {
    val prefs: AppPrefs = get()
    SysctlGuiTheme(
        forceDark = prefs.forceDark,
        dynamicColor = prefs.dynamicColors,
        content = content
    )
}

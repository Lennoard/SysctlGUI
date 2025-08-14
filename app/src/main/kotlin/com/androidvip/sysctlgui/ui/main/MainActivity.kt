package com.androidvip.sysctlgui.ui.main

import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.os.postDelayed
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val prefs: AppPrefs by inject()
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { resources ->
                    prefs.forceDark ||
                        resources.configuration.uiMode and
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                            android.content.res.Configuration.UI_MODE_NIGHT_YES
                }
            )
        )

        setContent {
            SysctlGuiTheme(
                darkTheme = prefs.forceDark || isSystemInDarkTheme(),
                contrastLevel = prefs.contrastLevel,
                dynamicColor = prefs.dynamicColors
            ) {
                MainScreen()
            }
        }

        Handler(mainLooper).postDelayed(1000) {
            checkNotificationPermission()
        }

        navigateFromIntent()
    }

    private fun navigateFromIntent() {
        // TODO: handle intent
        /*val fragmentName = intent.getStringExtra(EXTRA_DESTINATION) ?: return
        when (fragmentName) {
            Actions.BrowseParams.name -> R.id.navigationBrowse
            Actions.ListParams.name -> R.id.navigationList
            Actions.ExportParams.name -> R.id.navigationExport
            Actions.OpenSettings.name -> R.id.navigationSettings
            else -> null
        }?.let { id ->
            navHost.navController.navigate(id)
        }*/
    }

    private fun checkNotificationPermission() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (prefs.askedForNotificationPermission || !prefs.runOnStartUp) return
        if (manager.areNotificationsEnabled()) return

        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        prefs.askedForNotificationPermission = true
    }

    companion object {
        internal const val EXTRA_DESTINATION = "destination"
    }
}

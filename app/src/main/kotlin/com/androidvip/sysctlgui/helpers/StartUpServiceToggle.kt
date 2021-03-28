package com.androidvip.sysctlgui.helpers

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.androidvip.sysctlgui.receivers.BootReceiver

object StartUpServiceToggle {
    fun toggleStartUpService(context: Context, enabled: Boolean) {
        val receiver = ComponentName(context, BootReceiver::class.java)
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        context.packageManager.setComponentEnabledSetting(
            receiver,
            state,
            PackageManager.DONT_KILL_APP
        )
    }
}
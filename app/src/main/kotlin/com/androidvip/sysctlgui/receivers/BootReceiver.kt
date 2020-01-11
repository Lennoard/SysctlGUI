package com.androidvip.sysctlgui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.androidvip.sysctlgui.services.LegacyStartUpService
import com.androidvip.sysctlgui.services.StartUpService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            context?.let {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    LegacyStartUpService.start(context)
                } else {
                    StartUpService.start(context)
                }
            }
        }
    }
}

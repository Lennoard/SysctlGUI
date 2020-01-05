package com.androidvip.sysctlgui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.androidvip.sysctlgui.services.StartUpService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            // TODO: make a regular service for KitKat
            context?.let {
                StartUpService.start(context)
            }
        }
    }
}

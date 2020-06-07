package com.androidvip.sysctlgui.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.androidvip.sysctlgui.services.base.BaseStartUpService
import java.lang.RuntimeException
import java.lang.ref.WeakReference

class LegacyStartUpService : Service(), BaseStartUpService.ServiceHandler {

    override fun onStart(intent: Intent?, startId: Int) {
        BaseStartUpService(WeakReference(this), this).onStart()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartForeground(id: Int, notification: Notification) {
        this.startForeground(id, notification)
    }

    override fun onStopForeground(removeNotification: Boolean) {
        this.stopForeground(removeNotification)
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, LegacyStartUpService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.wtf(context.packageName, "Legacy service called on wrong API level").also {
                    throw RuntimeException("This service is only for legacy systems")
                }
            } else {
                context.startService(intent)
            }
        }
    }
}
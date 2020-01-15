package com.androidvip.sysctlgui.services

import android.app.Notification
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.androidvip.sysctlgui.services.base.BaseStartUpService
import java.lang.ref.WeakReference

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class StartUpService : JobService(), BaseStartUpService.ServiceHandler {

    override fun onStartJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStart(intent: Intent?, startId: Int) {
        BaseStartUpService(WeakReference(this), this).onStart()
    }

    override fun onStartForeground(id: Int, notification: Notification) {
        this.startForeground(id, notification)
    }

    override fun onStopForeground(removeNotification: Boolean) {
        this.stopForeground(true)
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, StartUpService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}

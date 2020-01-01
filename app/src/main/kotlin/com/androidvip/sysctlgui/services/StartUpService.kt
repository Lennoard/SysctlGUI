package com.androidvip.sysctlgui.services

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService


class StartUpService : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        Log.d(StartUpService::class.java.canonicalName, "Start service")
        // call the .conf file and make an notification for android >= O
    }

    companion object {
        private const val JOB_ID: Int = 1

        fun start(context: Context, intent: Intent) {
            enqueueWork(context, StartUpService::class.java, JOB_ID, intent)
        }
    }
}
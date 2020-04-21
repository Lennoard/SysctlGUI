package com.androidvip.sysctlgui.services.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.*
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class BaseStartUpService(
    private var weakContext: WeakReference<Context?>,
    private var handler: ServiceHandler?
) {

    /**
     * important: implement method to check if the device keep crashing on boot and disable start up
     *            maybe add a counter to prefs and if the value is > 3 disable
     */
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(weakContext.get())
    }

    fun onStart() {
        if (weakContext.get() != null) {

            // call the .conf file and make an notification for android >= O
            showNotificationAndThen {
                GlobalScope.launch(Dispatchers.Main) {
                    if (checkRequirements()) {
                        applyConfig()
                    }

                    if (handler != null) {
                        handler?.onStopForeground(true)
                    } else {
                       NotificationManagerCompat.from(weakContext.get()!!).apply {
                           cancel(SERVICE_ID)
                       }
                    }
                    onCleanUp()
                }
            }
        }
    }

    private fun showNotificationAndThen(onShow: () -> Unit) {
        weakContext.get()?.let { context ->
            val resources = context.resources
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_ID)
                .setSmallIcon(R.drawable.app_icon_foreground)
                .setContentTitle(resources.getString(R.string.notification_start_up_title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager: NotificationManager =
                    context.getSystemService(NotificationManager::class.java)

                NotificationChannel(
                    NOTIFICATION_ID,
                    resources.getString(R.string.notification_start_up_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = resources.getString(R.string.notification_start_up_channel_description)
                    notificationManager.createNotificationChannel(this)
                }
            }

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val startupDelay = prefs.getInt(Prefs.START_UP_DELAY, 0)

            if (startupDelay > 0) {
                builder.setContentTitle(context.getString(R.string.notification_start_up_description_delay, startupDelay))
                builder.setProgress(startupDelay, 0, true)

                GlobalScope.launch(Dispatchers.Main) {
                    NotificationManagerCompat.from(context).apply {
                        var delayCount = 0
                        for (i in startupDelay downTo 0) {
                            if (i == 0) {
                                builder.setProgress(0, 0, true)
                                builder.setContentTitle(context.getString(R.string.notification_start_up_title))
                                builder.setContentText(context.getString(R.string.notification_start_up_description))

                                notify(SERVICE_ID, builder.build())
                                handler?.onStartForeground(SERVICE_ID, builder.build())
                                onShow()
                            } else {
                                builder.setContentTitle(context.getString(R.string.notification_start_up_description_delay, i))
                                builder.setProgress(startupDelay, delayCount, false)
                                notify(SERVICE_ID, builder.build())
                                delay(1100)
                                delayCount++
                            }
                        }
                    }
                }
            } else {
                builder.setContentText(context.getString(R.string.notification_start_up_description))
                handler?.onStartForeground(SERVICE_ID, builder.build())
                onShow()
            }
        }
    }

    private suspend fun applyConfig() = withContext(Dispatchers.IO) {
        weakContext.get()?.let { context ->
            val params: List<KernelParameter> = Prefs.getUserParamsSet(context)
            val kernelParamUtils = KernelParamUtils(context)

            params.forEach { kernelParam: KernelParameter ->
                kernelParamUtils.commitChanges(kernelParam)
            }
        }
    }

    private suspend fun checkRequirements() = withContext(Dispatchers.IO) {
        val allowStartUp: Boolean = prefs.getBoolean(RUN_ON_START_UP, false)

        allowStartUp && Shell.rootAccess()
    }

    private fun onCleanUp() {
        // avoid memory leaks
        this.handler = null
        RootUtils.finishProcess()
    }

    private inline fun <T> T.runDelayed(delay: Long, crossinline block: () -> Unit) {
        Handler().postDelayed({ block() }, delay)
    }

    interface ServiceHandler {
        fun onStartForeground(id: Int, notification: Notification)
        fun onStopForeground(removeNotification: Boolean)
    }

    companion object {
        private const val RUN_ON_START_UP: String = "run_on_start_up"
        private const val SERVICE_ID: Int = 2
        private const val NOTIFICATION_ID: String = "2"
    }
}
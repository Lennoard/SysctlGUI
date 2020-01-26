package com.androidvip.sysctlgui.services.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.*
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            GlobalScope.launch(Dispatchers.Main) {
                showNotification()

                if (checkRequirements()) {
                    applyConfig()
                }

                handler?.onStopForeground(true)
                onCleanUp()
            }
        }
    }

    private fun showNotification() {
        weakContext.get()?.let { context ->
            val resources = context.resources
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, NOTIFICATION_ID)
                .setSmallIcon(R.drawable.app_icon_foreground)
                .setContentTitle(resources.getString(R.string.notification_start_up_title))
                .setContentText(resources.getString(R.string.notification_start_up_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notification: Notification = builder.build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val notificationManager: NotificationManager =
                    context.getSystemService(NotificationManager::class.java)

                val notificationChannel = NotificationChannel(
                    NOTIFICATION_ID,
                    resources.getString(R.string.notification_start_up_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.description =
                    resources.getString(R.string.notification_start_up_channel_description)
                notificationManager.createNotificationChannel(notificationChannel)


                handler?.onStartForeground(SERVICE_ID, notification)
            } else {
                handler?.onStartForeground(SERVICE_ID, notification)
            }
        }
    }

    private suspend fun applyConfig() = withContext(Dispatchers.IO) {
        val params: List<KernelParameter> = Prefs.getUserParamsSet(weakContext.get())

        params.forEach { kernelParam: KernelParameter ->
            KernelParamUtils(weakContext.get()!!).commitChanges(kernelParam)
        }
    }

    private suspend fun checkRequirements() = withContext(Dispatchers.IO) {
        val allowStartUp: Boolean = prefs.getBoolean(RUN_ON_START_UP, false)

        allowStartUp && Shell.rootAccess()
    }

    private fun onCleanUp() {
        // avoid memory leaks
        this.handler = null
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
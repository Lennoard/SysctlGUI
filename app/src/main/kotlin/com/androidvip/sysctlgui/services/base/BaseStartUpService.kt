package com.androidvip.sysctlgui.services.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BaseStartUpService(
    private var context: Context?,
    private var resources: Resources?,
    private var handler: ServiceHandler?
) {

    /**
     * important: implement method to check if the device keep crashing on boot and disable start up
     *            maybe add a counter to prefs and if the value is > 3 disable
     */

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun onStart() {
        // call the .conf file and make an notification for android >= O

        GlobalScope.launch(Dispatchers.Main) {
            showNotification()

            if (checkRequirements()) {
                applyConfig()
            }

            handler!!.onStopForeground(true)
            onCleanUp()
        }
    }

    private fun showNotification() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context!!,
            NOTIFICATION_ID
        )
            .setSmallIcon(R.drawable.app_icon_foreground)
            .setContentTitle(resources!!.getString(R.string.notification_start_up_title))
            .setContentText(resources!!.getString(R.string.notification_start_up_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notification: Notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager: NotificationManager =
                context!!.getSystemService(NotificationManager::class.java)

            val notificationChannel = NotificationChannel(
                NOTIFICATION_ID,
                resources!!.getString(R.string.notification_start_up_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description =
                resources!!.getString(R.string.notification_start_up_channel_description)
            notificationManager.createNotificationChannel(notificationChannel)


            handler!!.onStartForeground(SERVICE_ID, notification)
        } else {
            handler!!.onStartForeground(SERVICE_ID, notification)
        }
    }


    private suspend fun applyConfig() = withContext(Dispatchers.IO) {
        val params: List<KernelParameter> = Prefs.getUserParamsSet(context)

        params.forEach { kernelParam: KernelParameter ->
            val commandPrefix = if (prefs.getBoolean(Prefs.USE_BUSYBOX, false)) "busybox " else ""
            val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
                "sysctl" -> "${commandPrefix}sysctl -w ${kernelParam.name}=${kernelParam.value}"
                "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
                else -> "busybox sysctl -w ${kernelParam.name}=${kernelParam.value}"
            }
            RootUtils.executeSync(command)
        }
    }

    private suspend fun checkRequirements() = withContext(Dispatchers.IO) {
        val allowStartUp: Boolean = prefs.getBoolean(RUN_ON_START_UP, false)

        allowStartUp && Shell.rootAccess()
    }

    private fun onCleanUp() {
        // avoid memory leaks and remove context
        this.context = null
        this.resources = null
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
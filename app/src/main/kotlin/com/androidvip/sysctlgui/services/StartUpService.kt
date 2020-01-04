package com.androidvip.sysctlgui.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.topjohnwu.superuser.Shell


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class StartUpService : JobService() {

    /**
     * important: implement method to check if the device keep crashing on boot and disable start up
     *            maybe add a counter to prefs and if the value is > 3 disable
     */

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStart(intent: Intent?, startId: Int) {
        // call the .conf file and make an notification for android >= O

        showNotification()

        if (checkRequirements()) {
            applyConfig()
        }

        this.stopForeground(true)
    }

    private fun showNotification() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, notificationId)
            .setSmallIcon(R.drawable.app_icon_foreground)
            .setContentTitle(resources.getString(R.string.notification_start_up_title))
            .setContentText(resources.getString(R.string.notification_start_up_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notification: Notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)

            val notificationChannel = NotificationChannel(
                notificationId,
                resources.getString(R.string.notification_start_up_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description =
                resources.getString(R.string.notification_start_up_channel_description)
            notificationManager.createNotificationChannel(notificationChannel)


            startForeground(serviceId, notification)
        } else {
            startForeground(serviceId, notification)
        }
    }

    private fun applyConfig() {
        val params: List<KernelParameter> = Prefs.getUserParamsSet(applicationContext)

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

    private fun checkRequirements(): Boolean {
        if (!prefs.getBoolean(RUN_ON_START_UP, false)) {
            return false
        }

        if (!Shell.rootAccess()) {
            this.stopSelf()
            return false
        }

        return true
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

        private const val RUN_ON_START_UP: String = "run_on_start_up"
        private const val serviceId: Int = 2
        private const val notificationId: String = "2"
    }

}

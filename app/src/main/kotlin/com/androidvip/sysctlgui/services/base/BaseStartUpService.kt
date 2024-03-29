package com.androidvip.sysctlgui.services.base

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.topjohnwu.superuser.Shell
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BaseStartUpService(
    private var weakContext: WeakReference<Context?>,
    private var handler: ServiceHandler?,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CoroutineScope, KoinComponent {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()

    /**
     * important: implement method to check if the device keep crashing on boot and disable start up
     *            maybe add a counter to prefs and if the value is > 3 disable
     */
    private val appPrefs: AppPrefs by inject()
    private val rootUtils: RootUtils by inject()
    private val getUserParamsUseCase: GetUserParamsUseCase by inject()
    private val applyParamsUseCase: ApplyParamsUseCase by inject()

    fun onStart() {
        weakContext.get()?.let { context ->
            // call the .conf file and make an notification for android >= O
            showNotificationAndThen {
                launch {
                    if (checkRequirements()) {
                        applyConfig()
                    }

                    if (handler != null) {
                        handler?.onStopForeground(true)
                    } else {
                        NotificationManagerCompat.from(context).cancel(SERVICE_ID)
                    }

                    onCleanUp()
                }
            }
        }
    }

    private inline fun showNotificationAndThen(crossinline onShow: () -> Unit) {
        val context = weakContext.get() ?: return
        val resources = context.resources
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            NOTIFICATION_ID
        )
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
                description = resources.getString(
                    R.string.notification_start_up_channel_description
                )
                notificationManager.createNotificationChannel(this)
            }
        }

        val startupDelay = appPrefs.startUpDelay

        if (startupDelay > 0) {
            builder.setContentTitle(
                context.getString(
                    R.string.notification_start_up_description_delay,
                    startupDelay
                )
            )
            builder.setProgress(startupDelay, 0, true)

            launch {
                NotificationManagerCompat.from(context).apply {
                    var delayCount = 0
                    for (i in startupDelay downTo 0) {
                        if (i == 0) {
                            builder.setProgress(0, 0, true)
                            builder.setContentTitle(
                                context.getString(R.string.notification_start_up_title)
                            )
                            builder.setContentText(
                                context.getString(R.string.notification_start_up_description)
                            )

                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED ||
                                Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU
                            ) {
                                notify(SERVICE_ID, builder.build())
                            }
                            handler?.onStartForeground(SERVICE_ID, builder.build())
                            onShow()
                        } else {
                            builder.setContentTitle(
                                context.getString(
                                    R.string.notification_start_up_description_delay,
                                    i
                                )
                            )
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

    private suspend fun applyConfig() {
        getUserParamsUseCase().forEach {
            applyParamsUseCase(it)
        }
    }

    private suspend fun checkRequirements() = withContext(dispatcher) {
        appPrefs.runOnStartUp && Shell.rootAccess()
    }

    private fun onCleanUp() {
        // avoid memory leaks
        this.handler = null
        coroutineContext[Job]?.cancelChildren()
        rootUtils.finishProcess()
    }

    interface ServiceHandler {
        fun onStartForeground(id: Int, notification: Notification)
        fun onStopForeground(removeNotification: Boolean)
    }

    companion object {
        private const val SERVICE_ID: Int = 2
        private const val NOTIFICATION_ID: String = "2"
    }
}

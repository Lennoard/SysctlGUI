package com.androidvip.sysctlgui.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.ApplyParamUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class StartUpWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val mainContext = Dispatchers.Main + SupervisorJob()
    private val workerContext = Dispatchers.Default
    private val appPrefs: AppPrefs by inject()
    private val rootUtils: RootUtils by inject()
    private val getUserParams: GetUserParamsUseCase by inject()
    private val applyParam: ApplyParamUseCase by inject()

    private val notificationManager: NotificationManagerCompat
        get() = NotificationManagerCompat.from(context)

    override suspend fun doWork(): Result {
        withContext(mainContext) {
            showNotificationAndThen { builder ->
                if (checkRequirements()) {
                    applyConfig(builder)
                }

                delay(1.seconds)
                notificationManager.cancel(SERVICE_ID)

                withContext(workerContext) {
                    rootUtils.finishProcess()
                }
            }
        }


        return Result.success()
    }

    private suspend fun applyConfig(builder: NotificationCompat.Builder) {
        getUserParams().forEach { param ->
            builder.setContentText(param.toString())
            notifyIfPossible(builder)
            delay(250L)
            applyParam(param)
        }
    }

    private suspend fun checkRequirements(): Boolean {
        return appPrefs.runOnStartUp && rootUtils.isRootAvailable()
    }

    private suspend inline fun showNotificationAndThen(
        crossinline onShow: suspend (NotificationCompat.Builder) -> Unit
    ) {
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

                    notifyIfPossible(builder)
                    onShow(builder)
                } else {
                    builder.setContentTitle(
                        context.getString(R.string.notification_start_up_description_delay, i)
                    )
                    builder.setProgress(startupDelay, delayCount, false)
                    notifyIfPossible(builder)
                    delay(1.seconds)
                    delayCount++
                }
            }

        } else {
            builder.setContentText(context.getString(R.string.notification_start_up_description))
            notifyIfPossible(builder)
            onShow(builder)
        }
    }

    private fun notifyIfPossible(builder: NotificationCompat.Builder) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED ||
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU
        ) {
            runCatching { notificationManager.notify(SERVICE_ID, builder.build()) }.onFailure { it.printStackTrace() }
        }
    }

    companion object {
        private const val SERVICE_ID: Int = 2
        private const val NOTIFICATION_ID: String = "2"

        fun enqueue(context: Context) {
            val work: WorkRequest = OneTimeWorkRequestBuilder<StartUpWorker>().build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}

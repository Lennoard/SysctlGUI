package com.androidvip.sysctlgui.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.domain.Consts
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import com.androidvip.sysctlgui.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext

@ExperimentalContracts
class TaskerService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    private val appPrefs: AppPrefs by inject()
    private val getUserParamsUseCase: GetUserParamsUseCase by inject()
    private val applyParamsUseCase: ApplyParamsUseCase by inject()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val taskerList = intent.getIntExtra(
            TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER,
            Consts.LIST_NUMBER_INVALID
        )

        launch {
            applyParams(taskerList)
            if (appPrefs.showTaskerToast) {
                toast(getString(R.string.tasker_toast, taskerList), Toast.LENGTH_LONG)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        coroutineContext[Job]?.cancelChildren()
        super.onDestroy()
    }

    private suspend fun applyParams(listNumber: Int) {
        val params = getUserParamsUseCase().getOrNull().orEmpty()
        when (listNumber) {
            Consts.LIST_NUMBER_PRIMARY_TASKER,
            Consts.LIST_NUMBER_SECONDARY_TASKER -> params.filter { it.taskerParam }
            Consts.LIST_NUMBER_FAVORITES -> params.filter { it.favorite }
            Consts.LIST_NUMBER_APPLY_ON_BOOT -> params

            else -> emptyList()
        }.forEach {
            applyParamsUseCase.execute(it)
        }
    }
}

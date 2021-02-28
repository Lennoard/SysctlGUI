package com.androidvip.sysctlgui.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.prefs.TaskerPrefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import com.androidvip.sysctlgui.toast
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class TaskerService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    private val repository: ParamRepository by inject()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val taskerList = intent.getIntExtra(
            TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER,
            TaskerReceiver.LIST_NUMBER_INVALID
        )

        launch {
            applyParams(taskerList)
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@TaskerService)
            if (prefs.getBoolean(Prefs.SHOW_TASKER_TOAST, true)) {
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
        val params = repository.getParams(ParamRepository.SOURCE_ROOM)
        when(listNumber) {
            TaskerReceiver.LIST_NUMBER_PRIMARY_TASKER,
            TaskerReceiver.LIST_NUMBER_SECONDARY_TASKER -> params.filter { it.taskerParam }
            TaskerReceiver.LIST_NUMBER_FAVORITES -> params.filter { it.favorite }
            TaskerReceiver.LIST_NUMBER_APPLY_ON_BOOT -> params

            else -> emptyList()
        }.forEach {
            repository.update(it, ParamRepository.SOURCE_RUNTIME)
        }
    }

}
package com.androidvip.sysctlgui.work

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.utils.Consts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class TaskerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    private val mainContext = Dispatchers.Main + SupervisorJob()
    private val workerContext = Dispatchers.IO
    private val appPrefs: AppPrefs by inject()
    private val getUserParamsUseCase: GetUserParamsUseCase by inject()
    private val applyParamsUseCase: ApplyParamsUseCase by inject()

    override suspend fun doWork(): Result {
        withContext(workerContext) {
            val taskerList = inputData.getInt(
                TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER,
                Consts.LIST_NUMBER_INVALID
            )
            applyParams(taskerList)
            if (appPrefs.showTaskerToast) {
                withContext(mainContext) {
                    context.toast(
                        context.getString(R.string.tasker_toast, taskerList),
                        Toast.LENGTH_LONG
                    )
                }
            }
        }

        return Result.success()
    }

    private suspend fun applyParams(listNumber: Int) {
        val params = getUserParamsUseCase()
        when (listNumber) {
            Consts.LIST_NUMBER_PRIMARY_TASKER,
            Consts.LIST_NUMBER_SECONDARY_TASKER -> params.filter { it.taskerParam }
            Consts.LIST_NUMBER_FAVORITES -> params.filter { it.favorite }
            Consts.LIST_NUMBER_APPLY_ON_BOOT -> params

            else -> emptyList()
        }.forEach {
            applyParamsUseCase(it)
        }
    }

    companion object {
        fun enqueue(context: Context, listNumber: Int) {
            val work: WorkRequest = OneTimeWorkRequestBuilder<TaskerWorker>()
                .setInputData(workDataOf(TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER to listNumber))
                .build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}

package com.androidvip.sysctlgui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.prefs.TaskerPrefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import kotlinx.coroutines.*
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext

class TaskerReceiver : BroadcastReceiver(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()

    @ExperimentalContracts
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action != ACTION_FIRE_SETTING || context == null) return

        val bundle: Bundle? = intent.getBundleExtra(EXTRA_BUNDLE)
        if (bundle.isValidTaskerBundle()) {
            val taskerList = bundle.getInt(BUNDLE_EXTRA_LIST_NUMBER, LIST_NUMBER_INVALID)
            launch {
                with (context) {
                    applyParams(this.applicationContext, taskerList)
                    toast(getString(R.string.tasker_toast, taskerList), Toast.LENGTH_LONG)
                }
            }
        } else {
            Log.w(TAG, "Invalid tasker bundle: $bundle")
        }
    }

    private suspend fun applyParams(context: Context, listNumber: Int) = withContext(Dispatchers.IO) {
        val prefs : BasePrefs = when (listNumber) {
            LIST_NUMBER_PRIMARY_TASKER,
            LIST_NUMBER_SECONDARY_TASKER -> TaskerPrefs(context, listNumber)
            LIST_NUMBER_FAVORITES -> FavoritePrefs(context)
            LIST_NUMBER_APPLY_ON_BOOT -> Prefs(context)
            else -> return@withContext
        }

        val kernelParamUtils = KernelParamUtils(context)

        prefs.getUserParamsSet().forEach { param ->
            kernelParamUtils.commitChanges(param)
        }
    }

    companion object {
        const val TAG = "TaskerReceiver"
        const val EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE"
        const val EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB"
        const val ACTION_FIRE_SETTING = "com.twofortyfouram.locale.intent.action.FIRE_SETTING"

        const val BUNDLE_EXTRA_LIST_NUMBER = "com.androidvip.sysctlgui.tasker.extra.LIST_NUMBER"
        const val LIST_NUMBER_INVALID: Int = -1
        const val LIST_NUMBER_PRIMARY_TASKER: Int = 0
        const val LIST_NUMBER_SECONDARY_TASKER: Int = 1
        const val LIST_NUMBER_FAVORITES: Int = 2
        const val LIST_NUMBER_APPLY_ON_BOOT: Int = 3
    }

}

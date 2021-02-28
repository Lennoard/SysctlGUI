package com.androidvip.sysctlgui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.androidvip.sysctlgui.isValidTaskerBundle
import com.androidvip.sysctlgui.services.TaskerService
import kotlin.contracts.ExperimentalContracts

class TaskerReceiver : BroadcastReceiver() {

    @ExperimentalContracts
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action != ACTION_FIRE_SETTING || context == null) return

        val bundle: Bundle? = intent.getBundleExtra(EXTRA_BUNDLE)
        if (bundle.isValidTaskerBundle()) {
            val taskerList = bundle.getInt(BUNDLE_EXTRA_LIST_NUMBER, LIST_NUMBER_INVALID)
            val serviceIntent = Intent(context, TaskerService::class.java).apply {
                putExtra(BUNDLE_EXTRA_LIST_NUMBER, taskerList)
            }

            context.startService(serviceIntent)
        } else {
            Log.w(TAG, "Invalid tasker bundle: $bundle")
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


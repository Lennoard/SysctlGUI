package com.androidvip.sysctlgui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.androidvip.sysctlgui.isValidTaskerBundle
import com.androidvip.sysctlgui.utils.Consts
import com.androidvip.sysctlgui.work.TaskerWorker
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class TaskerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action != ACTION_FIRE_SETTING || context == null) return

        val bundle: Bundle? = intent.getBundleExtra(EXTRA_BUNDLE)
        if (bundle.isValidTaskerBundle()) {
            val taskerList = bundle.getInt(BUNDLE_EXTRA_LIST_NUMBER, Consts.LIST_NUMBER_INVALID)
            TaskerWorker.enqueue(context.applicationContext, taskerList)
        } else {
            Log.w(TAG, "Invalid tasker bundle: $bundle")
        }
    }

    internal companion object {
        const val TAG = "TaskerReceiver"
        const val EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE"
        const val EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB"
        const val ACTION_FIRE_SETTING = "com.twofortyfouram.locale.intent.action.FIRE_SETTING"

        const val BUNDLE_EXTRA_LIST_NUMBER = "com.androidvip.sysctlgui.tasker.extra.LIST_NUMBER"
    }
}

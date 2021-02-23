package com.androidvip.sysctlgui.prefs

import android.content.Context
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import com.androidvip.sysctlgui.receivers.TaskerReceiver

/**
 * Tasker prefs manager
 * @param [listNumber] one of tasker's list available to the user
 * @see [TaskerReceiver.LIST_NUMBER_PRIMARY_TASKER]
 * @see [TaskerReceiver.LIST_NUMBER_SECONDARY_TASKER]
 */
class TaskerPrefs(context: Context?, listNumber: Int) :
    BasePrefs(context, fileName = "tasker-params-$listNumber.json") {

    fun isTaskerParam(param: KernelParam): Boolean {
        return paramExists(param, getUserParamsSet())
    }

    override fun changeListener(): ChangeListener? = null

}

package com.androidvip.sysctlgui.widgets

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.androidvip.sysctlgui.domain.enums.Actions
import com.androidvip.sysctlgui.ui.main.MainActivity
import com.androidvip.sysctlgui.ui.start.StartActivity

internal val kernelParamNameKey = ActionParameters.Key<String>("kernelParamNameKey")

class ViewKernelParamDetailsAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val paramName = parameters[kernelParamNameKey] ?: return

        val intent = Intent(context, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = Actions.EditParam.name
            putExtra(MainActivity.EXTRA_PARAM_NAME, paramName)
        }
        context.startActivity(intent)
    }
}

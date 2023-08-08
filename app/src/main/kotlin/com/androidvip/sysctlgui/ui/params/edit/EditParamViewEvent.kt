package com.androidvip.sysctlgui.ui.params.edit

import android.content.Context
import com.androidvip.sysctlgui.data.models.KernelParam

sealed interface EditParamViewEvent {
    class ReceivedParam(val param: KernelParam, val context: Context) : EditParamViewEvent
    object BackPressed : EditParamViewEvent
    object FavoritePressed : EditParamViewEvent
    object TaskerPressed : EditParamViewEvent
    object ApplyPressed : EditParamViewEvent
    object ResetPressed : EditParamViewEvent
    class ParamValueInputChanged(val newValue: String) : EditParamViewEvent
}

package com.androidvip.sysctlgui.ui.params.edit

import android.content.Context
import com.androidvip.sysctlgui.data.models.KernelParam

sealed interface EditParamViewEvent {
    class ReceivedParam(val param: KernelParam, val context: Context) : EditParamViewEvent
    object BackPressed : EditParamViewEvent
    class FavoritePressed(val favorite: Boolean) : EditParamViewEvent
    object TaskerPressed : EditParamViewEvent
    object ApplyPressed : EditParamViewEvent
    object ResetPressed : EditParamViewEvent
    class TaskerListSelected(val listId: Int) : EditParamViewEvent
    class ParamValueInputChanged(val newValue: String) : EditParamViewEvent
}

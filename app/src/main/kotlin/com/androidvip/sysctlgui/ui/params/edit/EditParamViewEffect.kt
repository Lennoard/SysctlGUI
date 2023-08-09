package com.androidvip.sysctlgui.ui.params.edit

import androidx.annotation.StringRes

sealed interface EditParamViewEffect {
    class ShowApplyError(@StringRes val messageRes: Int) : EditParamViewEffect
    object ShowApplySuccess : EditParamViewEffect
    object NavigateBack : EditParamViewEffect
    object ShowTaskerListSelection : EditParamViewEffect
}

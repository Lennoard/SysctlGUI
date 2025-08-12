package com.androidvip.sysctlgui.ui.params.edit

import androidx.compose.ui.text.input.KeyboardType
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.models.UiKernelParam

data class EditParamViewState(
    val kernelParam: UiKernelParam = UiKernelParam(),
    val taskerAvailable: Boolean = false,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val documentation: ParamDocumentation? = null,
)

sealed interface EditParamViewEffect {
    data class OpenBrowser(val url: String) : EditParamViewEffect
    data class ShowApplySuccess(val previousValue: String) : EditParamViewEffect
    data class ShowError(val message: String) : EditParamViewEffect
    data object GoBack : EditParamViewEffect
}

sealed interface EditParamViewEvent {
    data class ApplyPressed(val newValue: String) : EditParamViewEvent
    data object UndoRequested : EditParamViewEvent
    data class FavoriteTogglePressed(val newState: Boolean) : EditParamViewEvent
    data class TaskerTogglePressed(val newState: Boolean, val listId: Int) : EditParamViewEvent
    data object DocumentationReadMoreClicked : EditParamViewEvent
}

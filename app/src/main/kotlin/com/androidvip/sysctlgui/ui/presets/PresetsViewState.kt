package com.androidvip.sysctlgui.ui.presets

import android.net.Uri
import com.androidvip.sysctlgui.domain.models.KernelParam

data class PresetsViewState(
    val paramsToImport: List<KernelParam> = emptyList(),
    val loading: Boolean = false,
    val incomingPresetsScreenState: IncomingPresetsScreenState = IncomingPresetsScreenState.Idle
)

enum class IncomingPresetsScreenState {
    Idle,
    Loading,
    Success
}

sealed interface PresetsViewEvent {
    data class PresetFilePicked(val uri: Uri?) : PresetsViewEvent
    data class BackUpFileCreated(val uri: Uri?) : PresetsViewEvent
    data object ConfirmImportPressed : PresetsViewEvent
    data object CancelImportPressed : PresetsViewEvent
}

sealed interface PresetsViewEffect {
    data class ShowError(val message: String) : PresetsViewEffect
    data class ShowToast(val message: String) : PresetsViewEffect
    data object ShowImportScreen : PresetsViewEffect
    data object GoBack : PresetsViewEffect
}

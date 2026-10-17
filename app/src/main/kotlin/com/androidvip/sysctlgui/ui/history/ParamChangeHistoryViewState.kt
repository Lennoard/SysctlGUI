package com.androidvip.sysctlgui.ui.history

import androidx.compose.runtime.Immutable
import com.androidvip.sysctlgui.domain.models.ParamChange

@Immutable
data class ParamChangeHistoryState(
    val changes: List<ParamChange> = emptyList(),
    val loading: Boolean = true,
    val rollbackInProgress: Boolean = false
)

sealed interface ParamChangeHistoryEvent {
    data class RollbackConfirmed(val change: ParamChange) : ParamChangeHistoryEvent
    data class DeleteConfirmed(val changeId: Long) : ParamChangeHistoryEvent
    data object ClearHistoryConfirmed : ParamChangeHistoryEvent
}

sealed interface ParamChangeHistoryEffect {
    data class RollbackSuccess(val paramName: String) : ParamChangeHistoryEffect
    data class ShowError(val message: String) : ParamChangeHistoryEffect
    data class ShowMessage(val message: String) : ParamChangeHistoryEffect
}

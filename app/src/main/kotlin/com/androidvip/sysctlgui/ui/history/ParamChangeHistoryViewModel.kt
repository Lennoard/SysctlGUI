package com.androidvip.sysctlgui.ui.history

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.domain.StringProvider
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamChange
import com.androidvip.sysctlgui.domain.usecase.ApplyParamUseCase
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamByNameUseCase
import com.androidvip.sysctlgui.domain.usecase.RecordParamChangeUseCase
import com.androidvip.sysctlgui.domain.usecase.UpsertUserParamUseCase
import com.androidvip.sysctlgui.domain.repository.ParamChangeHistoryRepository
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ParamChangeHistoryViewModel(
    private val historyRepository: ParamChangeHistoryRepository,
    private val getRuntimeParam: GetRuntimeParamUseCase,
    private val getUserParam: GetUserParamByNameUseCase,
    private val applyParam: ApplyParamUseCase,
    private val upsertUserParam: UpsertUserParamUseCase,
    private val recordParamChange: RecordParamChangeUseCase,
    private val stringProvider: StringProvider
) : BaseViewModel<ParamChangeHistoryEvent, ParamChangeHistoryState, ParamChangeHistoryEffect>() {

    init {
        viewModelScope.launch {
            try {
                historyRepository.observeChanges().collect { changes ->
                    setState { copy(changes = changes, loading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("ParamChangeHistoryViewModel", "Failed to load change history", error)
                setState { copy(loading = false) }
                setEffect {
                    ParamChangeHistoryEffect.ShowError(
                        error.message?.takeIf(String::isNotBlank)
                            ?: stringProvider.getString(R.string.change_history_load_failed)
                    )
                }
            }
        }
    }

    override fun createInitialState() = ParamChangeHistoryState()

    override fun onEvent(event: ParamChangeHistoryEvent) {
        when (event) {
            is ParamChangeHistoryEvent.RollbackConfirmed -> rollback(event.change)
            is ParamChangeHistoryEvent.DeleteConfirmed -> deleteChange(event.changeId)
            ParamChangeHistoryEvent.ClearHistoryConfirmed -> clearHistory()
        }
    }

    private fun deleteChange(changeId: Long) {
        viewModelScope.launch {
            try {
                historyRepository.deleteChange(changeId)
                setEffect {
                    ParamChangeHistoryEffect.ShowMessage(
                        stringProvider.getString(R.string.change_history_entry_deleted)
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("ParamChangeHistoryViewModel", "Failed to delete history entry", error)
                setEffect {
                    ParamChangeHistoryEffect.ShowError(
                        error.message?.takeIf(String::isNotBlank)
                            ?: stringProvider.getString(R.string.change_history_delete_failed)
                    )
                }
            }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            try {
                historyRepository.clearHistory()
                setEffect {
                    ParamChangeHistoryEffect.ShowMessage(
                        stringProvider.getString(R.string.change_history_cleared)
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("ParamChangeHistoryViewModel", "Failed to clear change history", error)
                setEffect {
                    ParamChangeHistoryEffect.ShowError(
                        error.message?.takeIf(String::isNotBlank)
                            ?: stringProvider.getString(R.string.change_history_delete_failed)
                    )
                }
            }
        }
    }

    private fun rollback(change: ParamChange) {
        viewModelScope.launch {
            setState { copy(rollbackInProgress = true) }
            try {
                val currentParam = getRuntimeParam(change.paramName)
                    ?: error(stringProvider.getString(R.string.change_history_param_unavailable))
                val savedParam = getUserParam(change.paramName) ?: currentParam
                val revertedParam = KernelParam(
                    name = currentParam.name,
                    path = currentParam.path,
                    value = change.previousValue,
                    isFavorite = savedParam.isFavorite,
                    isTaskerParam = savedParam.isTaskerParam,
                    taskerList = savedParam.taskerList
                )
                applyParam(revertedParam)
                upsertUserParam(revertedParam)
                val historySaved = currentParam.value == change.previousValue || runCatching {
                    recordParamChange(
                        paramName = currentParam.name,
                        path = currentParam.path,
                        previousValue = currentParam.value,
                        newValue = change.previousValue
                    )
                }.onFailure {
                    Log.e("ParamChangeHistoryViewModel", "Failed to record parameter rollback", it)
                }.isSuccess
                if (historySaved) {
                    setEffect {
                        ParamChangeHistoryEffect.RollbackSuccess(change.paramName)
                    }
                } else {
                    setEffect {
                        ParamChangeHistoryEffect.ShowError(
                            stringProvider.getString(R.string.change_history_rollback_record_failed)
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("ParamChangeHistoryViewModel", "Failed to roll back parameter", error)
                setEffect {
                    ParamChangeHistoryEffect.ShowError(
                        error.message?.takeIf(String::isNotBlank)
                            ?: stringProvider.getString(R.string.change_history_rollback_failed)
                    )
                }
            } finally {
                setState { copy(rollbackInProgress = false) }
            }
        }
    }
}

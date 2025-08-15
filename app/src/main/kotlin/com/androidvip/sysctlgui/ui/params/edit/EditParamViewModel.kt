package com.androidvip.sysctlgui.ui.params.edit

import android.util.Log
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.ApplyParamUseCase
import com.androidvip.sysctlgui.domain.usecase.GetParamDocumentationUseCase
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamByNameUseCase
import com.androidvip.sysctlgui.domain.usecase.IsTaskerInstalledUseCase
import com.androidvip.sysctlgui.domain.usecase.UpsertUserParamUseCase
import com.androidvip.sysctlgui.helpers.UiKernelParamMapper
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.launch

class EditParamViewModel(
    savedStateHandle: SavedStateHandle,
    private val applyParam: ApplyParamUseCase,
    private val getDocumentation: GetParamDocumentationUseCase,
    private val upsertUserParam: UpsertUserParamUseCase,
    private val getRuntimeParam: GetRuntimeParamUseCase,
    private val getUserParam: GetUserParamByNameUseCase,
    private val isTaskerInstalled: IsTaskerInstalledUseCase,
    private val appPrefs: AppPrefs
) : BaseViewModel<EditParamViewEvent, EditParamViewState, EditParamViewEffect>() {
    private val paramName: String? = savedStateHandle.get<String>(PARAM_NAME_KEY)
    private var previousKernelParamValue: String? = null

    init {
        viewModelScope.launch {
            if (paramName.isNullOrEmpty()) return@launch setEffect { EditParamViewEffect.GoBack }

            val param = runCatching { getUserParam(paramName) }.getOrNull()
                ?: getRuntimeParam(paramName)
                ?: return@launch setEffect { EditParamViewEffect.GoBack }

            setState {
                copy(
                    kernelParam = UiKernelParamMapper.map(param),
                    taskerAvailable = isTaskerInstalled(),
                    keyboardType = guessKeyboardType(param.value)
                )
            }

            val documentation = runCatching { getDocumentation(param) }.getOrNull()
            setState {
                copy(documentation = documentation)
            }
        }
    }

    override fun createInitialState() = EditParamViewState()

    override fun onEvent(event: EditParamViewEvent) {
        when (event) {
            is EditParamViewEvent.ApplyPressed -> applyKernelParam(event.newValue)
            is EditParamViewEvent.UndoRequested -> {
                previousKernelParamValue?.let { applyKernelParam(it, true) }
            }
            is EditParamViewEvent.DocumentationReadMoreClicked -> onDocumentationReadMoreClicked()
            is EditParamViewEvent.FavoriteTogglePressed -> onFavoriteTogglePressed(event.newState)
            is EditParamViewEvent.TaskerTogglePressed -> onTaskerTogglePressed(event.newState, event.listId)
        }
    }

    private fun applyKernelParam(newValue: String, isUndo: Boolean = false) {
        val oldParam = currentState.kernelParam
        viewModelScope.launch {
            val newParam = oldParam.copy(value = newValue)
            runCatching {
                applyParam(newParam)
                upsertUserParam(newParam)
            }.onSuccess {
                setState { copy(kernelParam = newParam) }
                if (!isUndo) {
                    setEffect {
                        EditParamViewEffect.ShowApplySuccess(previousValue = oldParam.value)
                    }
                }
                previousKernelParamValue = oldParam.value
            }.onFailure {
                Log.e("EditParamViewModel", "Failed to apply param", it)
                setEffect {
                    EditParamViewEffect.ShowError(it.message.orEmpty())
                }
            }
        }
    }

    private fun onFavoriteTogglePressed(newState: Boolean) {
        viewModelScope.launch {
            val newParam = currentState.kernelParam.copy(isFavorite = newState)
            runCatching {
                upsertUserParam(newParam)
            }.onSuccess {
                setState { copy(kernelParam = newParam) }
            }.onFailure {
                Log.e("EditParamViewModel", "Failed to update favorite status", it)
                setEffect {
                    EditParamViewEffect.ShowError("Failed to update favorite status")
                }
            }
        }
    }

    private fun onTaskerTogglePressed(newState: Boolean, listId: Int) {
        viewModelScope.launch {
            val newParam = currentState.kernelParam.copy(
                isTaskerParam = newState,
                taskerList = listId
            )
            runCatching {
                upsertUserParam(newParam)
            }.onSuccess {
                setState { copy(kernelParam = newParam) }
            }.onFailure {
                Log.e("EditParamViewModel", "Failed to update tasker status", it)
                setEffect {
                    EditParamViewEffect.ShowError("Failed to update tasker status")
                }
            }
        }
    }

    private fun onDocumentationReadMoreClicked() {
        currentState.documentation?.url?.let { documentationUrl ->
            setEffect { EditParamViewEffect.OpenBrowser(documentationUrl) }
        }
    }

    private fun guessKeyboardType(paramValue: String): KeyboardType {
        if (!appPrefs.guessInputType) return KeyboardType.Text

        return when {
            paramValue.toIntOrNull() != null -> KeyboardType.Number
            paramValue.toDoubleOrNull() != null -> KeyboardType.Decimal
            else -> KeyboardType.Text
        }
    }

    companion object {
        private const val PARAM_NAME_KEY = "paramName"
    }
}

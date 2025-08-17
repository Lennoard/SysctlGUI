package com.androidvip.sysctlgui.ui.presets

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.utils.PresetsFileProcessor
import com.androidvip.sysctlgui.domain.StringProvider
import com.androidvip.sysctlgui.domain.exceptions.EmptyFileException
import com.androidvip.sysctlgui.domain.exceptions.MalformedLineException
import com.androidvip.sysctlgui.domain.exceptions.NoValidParamException
import com.androidvip.sysctlgui.domain.usecase.AddUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.launch
import java.io.IOException

class PresetsViewModel(
    private val getUserParams: GetUserParamsUseCase,
    private val addUserParams: AddUserParamsUseCase,
    private val presetsFileProcessor: PresetsFileProcessor,
    private val stringProvider: StringProvider
) : BaseViewModel<PresetsViewEvent, PresetsViewState, PresetsViewEffect>() {
    override fun createInitialState() = PresetsViewState()

    override fun onEvent(event: PresetsViewEvent) {
        when (event) {
            PresetsViewEvent.CancelImportPressed -> setEffect { PresetsViewEffect.GoBack }
            PresetsViewEvent.ConfirmImportPressed -> confirmImport()
            is PresetsViewEvent.PresetFilePicked -> handleImportPreset(event.uri)
            is PresetsViewEvent.BackUpFileCreated -> handleBackup(event.uri)
        }
    }

    private fun confirmImport() {
        viewModelScope.launch {
            setState { copy(incomingPresetsScreenState = IncomingPresetsScreenState.Loading) }
            val paramsToImport = uiState.value.paramsToImport
            runCatching {
                addUserParams(paramsToImport)
            }.onSuccess {
                setState {
                    copy(
                        paramsToImport = emptyList(),
                        incomingPresetsScreenState = IncomingPresetsScreenState.Success
                    )
                }
            }.onFailure {
                setState { copy(incomingPresetsScreenState = IncomingPresetsScreenState.Idle) }
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.import_failure)) }
            }
        }
    }

    private fun handleImportPreset(uri: Uri?) {
        if (uri == null) {
            setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.preset_error_file_picking)) }
            return
        }

        viewModelScope.launch {
            try {
                val params = presetsFileProcessor.getKernelParamsFromUri(uri)
                setState {
                    copy(
                        paramsToImport = params,
                        incomingPresetsScreenState = IncomingPresetsScreenState.Idle
                    )
                }
                setEffect { PresetsViewEffect.ShowImportScreen }
            } catch (_: EmptyFileException) {
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.import_error_empty_file)) }
            } catch (_: MalformedLineException) {
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.import_error_malformed_line)) }
            } catch (_: NoValidParamException) {
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.export_error_no_param)) }
            } catch (_: IOException) {
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.export_error_io)) }
            } catch (e: Exception) {
                Log.e("PresetsViewModel", "Error importing file", e)
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.import_error)) }
            }
        }
    }

    private fun handleBackup(uri: Uri?) {
        if (uri == null) {
            setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.preset_error_file_creation)) }
            return
        }

        viewModelScope.launch {
            try {
                val userParams = getUserParams()

                runCatching {
                    presetsFileProcessor.backupParamsToUri(uri, userParams)
                }.onSuccess {
                    setEffect { PresetsViewEffect.ShowToast(stringProvider.getString(R.string.export_complete)) }
                }.onFailure {
                    setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.preset_error_processing_file)) }
                }
            } catch (e: Exception) {
                Log.e("PresetsViewModel", "Error saving file", e)
                setEffect { PresetsViewEffect.ShowError(stringProvider.getString(R.string.preset_error_opening_file)) }
            }
        }
    }
}

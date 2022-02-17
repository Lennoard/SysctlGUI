package com.androidvip.sysctlgui.ui.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.SettingsItem
import com.androidvip.sysctlgui.domain.exceptions.EmptyFileException
import com.androidvip.sysctlgui.domain.exceptions.InvalidFileExtensionException
import com.androidvip.sysctlgui.domain.exceptions.MalformedLineException
import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.exceptions.NoValidParamException
import com.androidvip.sysctlgui.domain.models.ViewState
import com.androidvip.sysctlgui.domain.usecase.BackupParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ExportParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ImportParamsUseCase
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class ExportOptionsViewModel(
    private val importParamsUseCase: ImportParamsUseCase,
    private val exportParamsUseCase: ExportParamsUseCase,
    private val backupParamsUseCase: BackupParamsUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val _viewEffect = MutableLiveData<ExportOptionsViewEffect>()
    internal val viewEffect: LiveData<ExportOptionsViewEffect> = _viewEffect

    private val _viewState = MutableLiveData<ViewState<Unit>>()
    val viewState: LiveData<ViewState<Unit>> = _viewState

    fun getBackOptionItems(): List<SettingsItem> = listOf(
        SettingsItem(
            R.string.import_parameters,
            R.string.read_from_file_sum,
            R.drawable.ic_import_params
        ),
        SettingsItem(
            R.string.export_parameters,
            R.string.export_parameters_sum,
            R.drawable.ic_export_params
        ),
        SettingsItem(
            R.string.backup_parameters,
            R.string.backup_parameters_sum,
            R.drawable.ic_backup_params
        ),
        SettingsItem(
            R.string.restore_parameters,
            R.string.restore_parameters_sum,
            R.drawable.ic_restore_params
        )
    )

    fun doWhenImportUserParamsPressed() = _viewEffect.postValue(
        ExportOptionsViewEffect.ImportUserParams
    )

    fun doWhenExportUserParamsPressed() = _viewEffect.postValue(
        ExportOptionsViewEffect.ExportUserParams
    )

    fun doWhenBackupPressed() = _viewEffect.postValue(ExportOptionsViewEffect.BackupRuntimeParams)

    fun doWhenRestorePressed() = _viewEffect.postValue(ExportOptionsViewEffect.RestoreRuntimeParams)

    fun importParams(stream: InputStream, fileExtension: String) = viewModelScope.launch {
        _viewState.postValue(currentViewState.copyState(isLoading = true))

        val postError: (Int) -> Unit = {
            _viewEffect.postValue(ExportOptionsViewEffect.ShowImportError(it))
        }
        val result = importParamsUseCase.execute(stream, fileExtension)
        when (result.exceptionOrNull()) {
            is JsonParseException,
            is JsonSyntaxException -> postError(R.string.import_error_invalid_json)

            is InvalidFileExtensionException -> postError(R.string.import_error_invalid_file_type)

            is EmptyFileException -> postError(R.string.import_error_empty_file)

            is MalformedLineException -> postError(R.string.import_error_malformed_line)

            is NoValidParamException -> postError(R.string.no_parameters_found)

            null -> {
                val successfulParams = result.getOrNull().orEmpty()
                _viewEffect.postValue(
                    ExportOptionsViewEffect.ShowImportSuccess(successfulParams.size)
                )
            }
            else -> postError(R.string.import_error)
        }

        _viewState.postValue(currentViewState.copyState(isLoading = false))
    }

    fun exportParams(target: Uri, context: Context, backup: Boolean) = viewModelScope.launch {
        _viewState.postValue(currentViewState.copyState(isLoading = true))

        val postError: (Int) -> Unit = {
            _viewEffect.postValue(ExportOptionsViewEffect.ShowExportError(it))
        }
        val result = if (backup) {
            backUpParamsWithFileDescriptor(target, context)
        } else {
            exportParamsWithFileDescriptor(target, context)
        }

        result.exceptionOrNull()?.printStackTrace()

        when (result.exceptionOrNull()) {
            is IOException -> postError(R.string.export_error_io)

            is NoParameterFoundException -> postError(R.string.export_error_no_param)

            is EmptyFileException -> postError(R.string.import_error_empty_file)

            null -> _viewEffect.postValue(ExportOptionsViewEffect.ShowExportSuccess)

            else -> postError(R.string.export_error)
        }

        _viewState.postValue(currentViewState.copyState(isLoading = false))
    }

    private suspend fun exportParamsWithFileDescriptor(
        target: Uri,
        context: Context
    ): Result<Unit> = withContext(ioDispatcher) {
        val descriptor = context.contentResolver.openFileDescriptor(target, "w")
            ?: return@withContext Result.failure(IOException())

        return@withContext exportParamsUseCase.execute(descriptor.fileDescriptor)
    }

    private suspend fun backUpParamsWithFileDescriptor(
        target: Uri,
        context: Context
    ): Result<Unit> = withContext(ioDispatcher) {
        val descriptor = context.contentResolver.openFileDescriptor(target, "w")
            ?: return@withContext Result.failure(IOException())

        return@withContext backupParamsUseCase.execute(descriptor.fileDescriptor)
    }

    private val currentViewState: ViewState<Unit>
        get() = viewState.value ?: ViewState()
}

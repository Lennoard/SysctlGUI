package com.androidvip.sysctlgui.ui.params.browse

import androidx.lifecycle.*
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.Consts
import com.androidvip.sysctlgui.domain.models.ViewState
import com.androidvip.sysctlgui.domain.usecase.GetParamsFromFilesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// TODO: Improve by using more view states and view effects
class BrowseParamsViewModel(
    private val getParamsFromFilesUseCase: GetParamsFromFilesUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState<KernelParam>>()
    private var _currentPath = Consts.PROC_SYS
    var listFoldersFirst = true

    val viewState: LiveData<ViewState<KernelParam>> = _viewState

    fun setPath(path: String) {
        _currentPath = path
        viewModelScope.launch {
            loadBrowsableParamFiles()
        }
    }

    private suspend fun getCurrentPathFiles(): Array<out File>? = withContext(dispatcher) {
        runCatching {
            File(_currentPath).listFiles()
        }.getOrDefault(arrayOf())
    }

    private suspend fun loadBrowsableParamFiles() {
        _viewState.postValue(currentViewState.copy(isLoading = true))
        val files = getCurrentPathFiles().maybeDirectorySorted()
        val params = getParamsFromFilesUseCase(files).getOrNull().orEmpty().map {
            DomainParamMapper.map(it)
        }
        _viewState.postValue(currentViewState.copy(isLoading = false, data = params))
    }

    private suspend fun Array<out File>?.maybeDirectorySorted() = withContext(dispatcher) {
        return@withContext this@maybeDirectorySorted?.apply {
            if (listFoldersFirst) {
                sortByDescending { it.isDirectory }
            }
        }?.toList().orEmpty()
    }

    private val currentViewState: ViewState<KernelParam>
        get() = viewState.value ?: ViewState()
}

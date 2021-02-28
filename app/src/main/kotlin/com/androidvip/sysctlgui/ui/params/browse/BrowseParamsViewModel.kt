package com.androidvip.sysctlgui.ui.params.browse

import androidx.lifecycle.*
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.models.ViewState
import com.androidvip.sysctlgui.data.repository.ParamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BrowseParamsViewModel(private val repository: ParamRepository) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState<KernelParam>>()
    private var _currentPath = "/proc/sys"
    var listFoldersFirst = true

    val viewState: LiveData<ViewState<KernelParam>> = _viewState

    fun setPath(path: String) {
        _currentPath = path
        viewModelScope.launch {
            loadBrowsableParamFiles()
        }
    }

    private suspend fun getCurrentPathFiles(): Array<out File>? = withContext(Dispatchers.IO) {
        runCatching {
            File(_currentPath).listFiles()
        }.getOrDefault(arrayOf())
    }

    private suspend fun loadBrowsableParamFiles() {
        _viewState.postValue(currentViewState.copy(isLoading = true))
        val files = getCurrentPathFiles().maybeDirectorySorted()
        val params = repository.getParamsFromFiles(files)
        _viewState.postValue(currentViewState.copy(isLoading = false, data = params))
    }

    private suspend fun Array<out File>?.maybeDirectorySorted() = withContext(Dispatchers.Default) {
        return@withContext this@maybeDirectorySorted?.apply {
            if (listFoldersFirst) {
                sortByDescending { it.isDirectory }
            }
        }?.toList().orEmpty()
    }

    private val currentViewState: ViewState<KernelParam>
        get() = viewState.value ?: ViewState()
}

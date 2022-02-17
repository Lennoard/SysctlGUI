package com.androidvip.sysctlgui.ui.params.browse

import android.app.Activity
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.Consts
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.GetParamsFromFilesUseCase
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.hadilq.liveevent.LiveEvent
import com.hadilq.liveevent.LiveEventConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.util.Pair as PairUtil

class BrowseParamsViewModel(
    private val getParamsFromFilesUseCase: GetParamsFromFilesUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    appPrefs: AppPrefs
) : ViewModel() {
    private val _viewState = MutableLiveData<ParamBrowserViewState>()
    val viewState: LiveData<ParamBrowserViewState> = _viewState
    val viewEffect = LiveEvent<ParamBrowserViewEffect>(config = LiveEventConfig.PreferFirstObserver)

    var listFoldersFirst = true

    init {
        listFoldersFirst = appPrefs.listFoldersFirst
    }

    fun setPath(path: String) {
        viewModelScope.launch {
            loadBrowsableParamFiles(path)
        }
    }

    fun setSearchExpression(expression: String) = updateState {
        searchExpression = expression
    }

    fun doWhenParamItemClicked(param: KernelParam, itemLayout: View, activity: Activity) {
        val sharedElements = arrayOf<PairUtil<View, String>>(
            PairUtil(
                itemLayout.findViewById(R.id.name),
                EditKernelParamActivity.NAME_TRANSITION_NAME
            )
        )
        val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            *sharedElements,
        )

        viewEffect.postValue(ParamBrowserViewEffect.NavigateToParamDetails(param, options))
    }

    fun doWhenDirectoryChanges(newDir: File) {
        val newPath = newDir.absolutePath
        if (newPath.isEmpty() || !newPath.startsWith(Consts.PROC_SYS)) {
            viewEffect.postValue(ParamBrowserViewEffect.ShowToast(R.string.invalid_path))
            return
        }

        setPath(newPath)

        when {
            newPath.startsWith("/proc/sys/abi") -> updateState {
                docUrl = "https://www.kernel.org/doc/Documentation/sysctl/abi.txt"
                showDocumentationMenu = true
            }

            newPath.startsWith("/proc/sys/fs") -> updateState {
                docUrl = "https://www.kernel.org/doc/Documentation/sysctl/fs.txt"
                showDocumentationMenu = true
            }

            newPath.startsWith("/proc/sys/kernel") -> updateState {
                docUrl = "https://www.kernel.org/doc/Documentation/sysctl/kernel.txt"
                showDocumentationMenu = true
            }

            newPath.startsWith("/proc/sys/net") -> updateState {
                docUrl = "https://www.kernel.org/doc/Documentation/sysctl/net.txt"
                showDocumentationMenu = true
            }

            newPath.startsWith("/proc/sys/vm") -> updateState {
                docUrl = "https://www.kernel.org/doc/Documentation/sysctl/vm.txt"
                showDocumentationMenu = true
            }

            else -> updateState {
                showDocumentationMenu = false
            }
        }
    }

    fun doWhenDocumentationMenuClicked() {
        val url = viewState.value?.docUrl.orEmpty()
        viewEffect.postValue(ParamBrowserViewEffect.OpenDocumentationUrl(url))
    }

    private suspend fun getCurrentPathFiles(path: String) = withContext(dispatcher) {
        runCatching {
            File(path).listFiles()?.toList()
        }.getOrDefault(emptyList())
    }

    private suspend fun loadBrowsableParamFiles(path: String) {
        updateState { isLoading = true }
        val files = getCurrentPathFiles(path).maybeDirectorySorted().maybeFiltered()
        val params = getParamsFromFilesUseCase(files).getOrNull().orEmpty().map {
            DomainParamMapper.map(it)
        }

        updateState {
            currentPath = path
            isLoading = false
            data = params
        }
    }
    private suspend fun List<File>?.maybeDirectorySorted() = withContext(dispatcher) {
        return@withContext this@maybeDirectorySorted?.run {
            if (listFoldersFirst) {
                sortedByDescending { it.isDirectory }
            } else this
        }?.toList().orEmpty()
    }

    private suspend fun List<File>?.maybeFiltered() = withContext(dispatcher) {
        val searchExpression = viewState.value?.searchExpression.orEmpty()
        return@withContext this@maybeFiltered?.run {
            if (searchExpression.isNotEmpty()) {
                filter { param ->
                    param.name.lowercase()
                        .replace(".", "")
                        .contains(searchExpression.lowercase())
                }
            } else this
        }?.toList().orEmpty()
    }

    private fun updateState(state: ParamBrowserViewState.() -> Unit) {
        _viewState.value = currentViewState.apply(state)
    }

    private val currentViewState: ParamBrowserViewState
        get() = viewState.value ?: ParamBrowserViewState()
}

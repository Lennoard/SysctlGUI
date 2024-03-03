package com.androidvip.sysctlgui.ui.params.browse

import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.GetParamsFromFilesUseCase
import com.androidvip.sysctlgui.utils.BaseViewModel
import com.androidvip.sysctlgui.utils.Consts
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BrowseParamsViewModel(
    private val getParamsFromFilesUseCase: GetParamsFromFilesUseCase,
    appPrefs: AppPrefs,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel<ParamBrowserViewEvent, ParamBrowserViewState, ParamBrowserViewEffect>() {
    private var listFoldersFirst = true
    private var searchExpression = ""

    init {
        listFoldersFirst = appPrefs.listFoldersFirst
    }

    override fun createInitialState(): ParamBrowserViewState = ParamBrowserViewState()

    override fun onEvent(event: ParamBrowserViewEvent) {
        when (event) {
            ParamBrowserViewEvent.RefreshRequested -> setPath(currentState.currentPath)
            is ParamBrowserViewEvent.DirectoryChanged -> onDirectoryChanged(event.dir)
            is ParamBrowserViewEvent.SearchExpressionChanged -> onSearchExpressionChanged(event.data)
            is ParamBrowserViewEvent.ParamClicked -> setEffect {
                ParamBrowserViewEffect.NavigateToParamDetails(DomainParamMapper.map(event.param))
            }

            ParamBrowserViewEvent.DocumentationMenuClicked -> setEffect {
                ParamBrowserViewEffect.OpenDocumentationUrl(currentState.docUrl)
            }

            ParamBrowserViewEvent.FavoritesMenuClicked -> setEffect {
                ParamBrowserViewEffect.NavigateToFavorite
            }
        }
    }

    private fun setPath(path: String) {
        viewModelScope.launch {
            loadBrowsableParamFiles(path)
        }
    }

    private fun onDirectoryChanged(newDir: File) {
        val newPath = newDir.absolutePath
        if (newPath.isEmpty() || !newPath.startsWith(Consts.PROC_SYS)) {
            setEffect { ParamBrowserViewEffect.ShowToast(R.string.invalid_path) }
            return
        }

        setPath(newPath)

        when {
            newPath.startsWith("/proc/sys/abi") -> setState {
                copy(
                    docUrl = "https://www.kernel.org/doc/Documentation/sysctl/abi.txt",
                    showDocumentationMenu = true
                )
            }

            newPath.startsWith("/proc/sys/fs") -> setState {
                copy(
                    docUrl = "https://www.kernel.org/doc/Documentation/sysctl/fs.txt",
                    showDocumentationMenu = true
                )
            }

            newPath.startsWith("/proc/sys/kernel") -> setState {
                copy(
                    docUrl = "https://www.kernel.org/doc/Documentation/sysctl/kernel.txt",
                    showDocumentationMenu = true
                )
            }

            newPath.startsWith("/proc/sys/net") -> setState {
                copy(
                    docUrl = "https://www.kernel.org/doc/Documentation/sysctl/net.txt",
                    showDocumentationMenu = true
                )
            }

            newPath.startsWith("/proc/sys/vm") -> setState {
                copy(
                    docUrl = "https://www.kernel.org/doc/Documentation/sysctl/vm.txt",
                    showDocumentationMenu = true
                )
            }

            else -> setState { copy(showDocumentationMenu = false) }
        }
    }

    private suspend fun getCurrentPathFiles(path: String) = withContext(dispatcher) {
        runCatching {
            File(path).listFiles()?.toList()
        }.getOrDefault(emptyList())
    }

    private suspend fun loadBrowsableParamFiles(path: String) {
        setState { copy(isLoading = true) }
        val files = getCurrentPathFiles(path).maybeDirectorySorted()
        val params = getParamsFromFilesUseCase(files).map {
            DomainParamMapper.map(it)
        }

        setState {
            copy(
                currentPath = path,
                isLoading = false,
                data = params.filter { param -> byName(param.name, searchExpression) },
                totalData = params
            )
        }
    }

    private suspend fun List<File>?.maybeDirectorySorted() = withContext(dispatcher) {
        return@withContext this@maybeDirectorySorted?.run {
            if (listFoldersFirst) {
                sortedByDescending { it.isDirectory }
            } else {
                this
            }
        }?.toList().orEmpty()
    }

    private fun onSearchExpressionChanged(expression: String) {
        searchExpression = expression

        setState {
            copy(data = this.totalData.filter { kernelParam ->
                byName(
                    kernelParam.name,
                    searchExpression
                )
            })
        }
    }

    private fun byName(current: String, expected: String): Boolean {
        if (expected.isEmpty()) {
            return true
        }
        return current.lowercase()
            .replace(".", "")
            .contains(expected.lowercase())
    }
}

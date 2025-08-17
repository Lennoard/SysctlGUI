package com.androidvip.sysctlgui.ui.params.browse

import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.GetParamDocumentationUseCase
import com.androidvip.sysctlgui.domain.usecase.GetParamsFromFilesUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.helpers.UiKernelParamMapper
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.utils.BaseViewModel
import com.androidvip.sysctlgui.utils.Consts
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ParamBrowseViewModel(
    private val getParamsFromFiles: GetParamsFromFilesUseCase,
    private val getParamDocumentation: GetParamDocumentationUseCase,
    private val getUserParams: GetUserParamsUseCase,
    private val appPrefs: AppPrefs,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel<ParamBrowseViewEvent, ParamBrowseState, ParamBrowseViewEffect>() {
    private val userParams = mutableListOf<KernelParam>()

    override fun createInitialState() = ParamBrowseState()

    init {
        viewModelScope.launch {
            setState { copy(loading = true) }
            val startingDirectory = File(Consts.PROC_SYS)
            val params = getParams(startingDirectory)

            runCatching {
                userParams.clear()
                userParams.addAll(getUserParams())
            }

            setState {
                copy(
                    params = params,
                    currentPath = startingDirectory.absolutePath,
                    loading = false
                )
            }
        }
    }

    override fun onEvent(event: ParamBrowseViewEvent) {
        when (event) {
            is ParamBrowseViewEvent.DocumentationClicked -> setEffect {
                ParamBrowseViewEffect.OpenBrowser(event.docs.url.orEmpty())
            }

            ParamBrowseViewEvent.BackRequested -> onBackRequested()
            is ParamBrowseViewEvent.ParamClicked -> onParamClicked(event.param)
            ParamBrowseViewEvent.RefreshRequested -> handleRefreshRequested()
        }
    }

    private fun handleRefreshRequested() {
        val currentPath = currentState.currentPath
        if (currentPath == Consts.PROC_SYS) return

        fetchChildParams(currentPath)
    }

    private fun fetchChildParams(parentParam: UiKernelParam) {
        viewModelScope.launch {
            setState { copy(loading = true) }
            runCatching {
                val newParamsDeferred = async {
                    getParams(File(parentParam.path))
                }
                val directoryDocumentationDeferred = async {
                    runCatching { getParamDocumentation(parentParam) }.getOrNull()
                }

                val newParams = newParamsDeferred.await()
                val directoryDocumentation = directoryDocumentationDeferred.await()

                setState {
                    copy(
                        params = newParams,
                        currentPath = parentParam.path,
                        backEnabled = parentParam.path != Consts.PROC_SYS,
                        documentation = directoryDocumentation,
                        loading = false
                    )
                }
            }.onFailure {
                setEffect { ParamBrowseViewEffect.ShowError(it.message ?: "Unknown error") }
                setState { copy(loading = false) }
            }
        }
    }

    private fun fetchChildParams(parentPath: String) {
        val param = KernelParam.createFromPath(parentPath, "")
        fetchChildParams(UiKernelParamMapper.map(param))
    }

    private fun onParamClicked(param: UiKernelParam) {
        if (param.isDirectory) {
            fetchChildParams(param)
        } else {
            setEffect {
                ParamBrowseViewEffect.EditKernelParam(param)
            }
        }
    }

    private fun onBackRequested() {
        val currentPath = currentState.currentPath
        if (currentPath == Consts.PROC_SYS) return
        val parentFile = File(currentPath).parentFile ?: return

        fetchChildParams(parentFile.absolutePath)
    }

    private suspend fun getParams(file: File): List<UiKernelParam> = withContext(ioDispatcher) {
        val fileList = if (file.canRead()) {
            file.listFiles()?.toList() ?: emptyList()
        } else {
            val rootAwareFile = FileSystemManager.getLocal().getFile(file.absolutePath)
            rootAwareFile.listFiles()?.toList() ?: emptyList()
        }

        val params = getParamsFromFiles(fileList).map { fileParam ->
            UiKernelParamMapper.map(fileParam).copy(
                isFavorite = userParams
                    .filter { it.isFavorite }
                    .any { it.name == fileParam.name }
            )
        }

        if (appPrefs.listFoldersFirst) {
            params.sortedByDescending { it.isDirectory }
        } else {
            params
        }
    }
}

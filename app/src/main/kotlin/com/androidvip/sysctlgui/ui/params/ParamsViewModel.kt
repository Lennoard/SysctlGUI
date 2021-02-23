package com.androidvip.sysctlgui.ui.params

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.prefs.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ParamsViewModel(
    private val repository: ParamRepository,
    private val prefs: SharedPreferences
) : ViewModel() {
    private val _loading = MutableLiveData(false)
    private val _browsableKernelParams = MutableLiveData(mutableListOf<KernelParam>())
    private val _kernelParams = MutableLiveData(mutableListOf<KernelParam>())
    private var _currentPath = File("/proc/sys")

    val loading: LiveData<Boolean> = _loading
    val browsableKernelParams: MutableLiveData<MutableList<KernelParam>> = _browsableKernelParams
    val kernelParams: MutableLiveData<MutableList<KernelParam>> = _kernelParams

    val localParams = liveData {
        _loading.postValue(true)
        val params = repository.getParams()
        _loading.postValue(false)
        emit(params)
    }

    suspend fun loadBrowsableParams() {
        _loading.postValue(true)
        val files = getCurrentPathFiles()?.apply {
            if (prefs.getBoolean(Prefs.LIST_FOLDERS_FIRST, true)) {
                sortByDescending { it.isDirectory }
            }
        }

        _browsableKernelParams.postValue(repository.getParamsFileList(files.orEmpty()))
        _loading.postValue(false)
    }

    suspend fun getKernelParams() {
        _loading.postValue(true)
        _kernelParams.postValue(repository.getParamsFromKernel())
        _loading.postValue(false)
    }

    suspend fun setPath(file: File) {
        _currentPath = file
        loadBrowsableParams()
    }

    private suspend fun getCurrentPathFiles(): Array<out File>? = withContext(Dispatchers.IO) {
        runCatching {
            _currentPath.listFiles()
        }.getOrDefault(arrayOf())
    }

}
package com.androidvip.sysctlgui.ui.params.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.models.ViewState
import com.androidvip.sysctlgui.data.repository.ParamRepository
import kotlinx.coroutines.launch

typealias ParamFilterPredicate = (KernelParam) -> Boolean

class UserParamsViewModel(private val repository: ParamRepository) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState<KernelParam>>()
    private val _filterPredicate = MutableLiveData<ParamFilterPredicate>()
    val viewState: LiveData<ViewState<KernelParam>> = _viewState

    fun getParams() {
        viewModelScope.launch {
            _viewState.postValue(currentViewState.copy(isLoading = true))
            val params = repository.getParams(ParamRepository.SOURCE_ROOM)
                .filter(currentFilterPredicate)
            _viewState.postValue(currentViewState.copy(isLoading = false, data = params))
        }
    }

    fun setFilterPredicate(predicate: ParamFilterPredicate) {
        _filterPredicate.value = predicate
        getParams()
    }

    fun delete(kernelParam: KernelParam) {
        _viewState.postValue(currentViewState.copy(isLoading = true))
        viewModelScope.launch {
            repository.delete(kernelParam, ParamRepository.SOURCE_ROOM)
            getParams()
        }
    }

    fun update(kernelParam: KernelParam) {
        _viewState.postValue(currentViewState.copy(isLoading = true))
        viewModelScope.launch {
            repository.update(kernelParam, ParamRepository.SOURCE_ROOM)
            getParams()
        }
    }

    private val currentViewState: ViewState<KernelParam>
        get() = viewState.value ?: ViewState()

    private val currentFilterPredicate: ParamFilterPredicate
        get() = _filterPredicate.value ?: { true }
}

package com.androidvip.sysctlgui.ui.params.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.utils.ViewState
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.RemoveUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.UpdateUserParamUseCase
import kotlinx.coroutines.launch

typealias ParamFilterPredicate = (KernelParam) -> Boolean

class UserParamsViewModel(
    private val getParamsUseCase: GetUserParamsUseCase,
    private val removeParamUseCase: RemoveUserParamUseCase,
    private val updateParamUseCase: UpdateUserParamUseCase
) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState<KernelParam>>()
    private val _filterPredicate = MutableLiveData<ParamFilterPredicate>()
    val viewState: LiveData<ViewState<KernelParam>> = _viewState

    fun getParams() {
        viewModelScope.launch {
            _viewState.postValue(currentViewState.copyState(isLoading = true))
            val params = getParamsUseCase()
                .map { DomainParamMapper.map(it) }
                .filter(currentFilterPredicate)
            _viewState.postValue(currentViewState.copyState(isLoading = false, data = params))
        }
    }

    fun setFilterPredicate(predicate: ParamFilterPredicate) {
        _filterPredicate.value = predicate
        getParams()
    }

    fun delete(kernelParam: KernelParam) {
        _viewState.postValue(currentViewState.copyState(isLoading = true))
        viewModelScope.launch {
            removeParamUseCase.execute(kernelParam)
            getParams()
        }
    }

    fun update(kernelParam: KernelParam) {
        _viewState.postValue(currentViewState.copyState(isLoading = true))
        viewModelScope.launch {
            updateParamUseCase(kernelParam)
            getParams()
        }
    }

    private val currentViewState: ViewState<KernelParam>
        get() = viewState.value ?: ViewState()

    private val currentFilterPredicate: ParamFilterPredicate
        get() = _filterPredicate.value ?: { true }
}

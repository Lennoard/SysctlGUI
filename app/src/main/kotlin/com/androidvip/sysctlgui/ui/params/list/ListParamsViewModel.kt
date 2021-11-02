package com.androidvip.sysctlgui.ui.params.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ViewState
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamsUseCase
import kotlinx.coroutines.launch

class ListParamsViewModel(private val getParamsUseCase: GetRuntimeParamsUseCase) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState<KernelParam>>()
    val viewState: LiveData<ViewState<KernelParam>> = _viewState

    fun getKernelParams() {
        viewModelScope.launch {
            _viewState.postValue(currentViewState.copy(isLoading = true))
            val params = getParamsUseCase().getOrNull().orEmpty().map {
                DomainParamMapper.map(it)
            }
            _viewState.postValue(currentViewState.copy(isLoading = false, data = params))
        }
    }

    private val currentViewState: ViewState<KernelParam>
        get() = viewState.value ?: ViewState()
}

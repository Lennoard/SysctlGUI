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

    fun requestKernelParams() {
        val searchExpression = viewState.value?.searchExpression.orEmpty()
        viewModelScope.launch {
            updateState { isLoading = true }
            val params = getParamsUseCase().getOrNull().orEmpty().map {
                DomainParamMapper.map(it)
            }.filter { param ->
                if (searchExpression.isEmpty()) true else {
                    param.name.lowercase()
                        .replace(".", "")
                        .contains(searchExpression.lowercase())
                }
            }
            updateState { isLoading = false; data = params }
        }
    }

    fun setSearchExpression(expression: String) = updateState {
        searchExpression = expression
    }

    private fun updateState(state: ViewState<KernelParam>.() -> Unit) {
        _viewState.value = currentViewState.apply(state)
    }

    private val currentViewState: ViewState<KernelParam>
        get() = viewState.value ?: ViewState()
}

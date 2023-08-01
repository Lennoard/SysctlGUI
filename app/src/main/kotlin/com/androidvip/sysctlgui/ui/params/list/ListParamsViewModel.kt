package com.androidvip.sysctlgui.ui.params.list

import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamsUseCase
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.launch

class ListParamsViewModel(
    private val getParamsUseCase: GetRuntimeParamsUseCase
) : BaseViewModel<ParamViewEvent, ParamViewState, ParamViewEffect>() {
    private var searchExpression = ""

    private fun requestKernelParams() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val params = getParamsUseCase()
                .map(DomainParamMapper::map)
                .filter { param ->
                    if (searchExpression.isNotEmpty()) {
                        param.name.lowercase()
                            .replace(".", "")
                            .contains(searchExpression.lowercase())
                    } else {
                        true
                    }
                }
            setState { copy(isLoading = false, data = params, showEmptyState = params.isEmpty()) }
        }
    }

    override fun createInitialState(): ParamViewState = ParamViewState()

    override fun processEvent(event: ParamViewEvent) {
        when (event) {
            is ParamViewEvent.ParamClicked -> setEffect {
                ParamViewEffect.NavigateToParamDetails(DomainParamMapper.map(event.param))
            }
            is ParamViewEvent.SearchExpressionChanged -> {
                searchExpression = event.data
                requestKernelParams()
            }
            ParamViewEvent.RefreshRequested -> requestKernelParams()
            else -> Unit
        }
    }
}

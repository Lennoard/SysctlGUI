package com.androidvip.sysctlgui.ui.params.user

import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.data.mapper.DomainParamMapper
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.RemoveUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.UpdateUserParamUseCase
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.launch

typealias ParamFilterPredicate = (KernelParam) -> Boolean

class UserParamsViewModel(
    private val getParamsUseCase: GetUserParamsUseCase,
    private val removeParamUseCase: RemoveUserParamUseCase,
    private val updateParamUseCase: UpdateUserParamUseCase
) : BaseViewModel<UserParamsViewEvent, UserParamsViewState, Nothing>() {
    private var baseFilterPredicate: ParamFilterPredicate = { true }
    private var currentFilterPredicate: ParamFilterPredicate = { true }

    override fun createInitialState(): UserParamsViewState = UserParamsViewState()

    override fun onEvent(event: UserParamsViewEvent) {
        when (event) {
            UserParamsViewEvent.ParamsRequested -> getParams()
            UserParamsViewEvent.SearchPressed -> getParams()
            UserParamsViewEvent.CloseSearchPressed -> {
                setState { copy(searchViewVisible = false) }
            }
            UserParamsViewEvent.SearchViewPressed -> {
                setState { copy(searchViewVisible = true) }
            }
            is UserParamsViewEvent.DeleteSwipe -> {
                if (event.param.favorite) {
                    event.param.favorite = false
                    update(event.param)
                } else {
                    delete(event.param)
                }
            }
            is UserParamsViewEvent.SearchQueryChanged -> {
                currentFilterPredicate = {
                    it.name
                        .replace(".", "")
                        .contains(event.query, ignoreCase = true) && 
                        baseFilterPredicate(it)
                }
            }
        }
    }
    private fun getParams() {
        viewModelScope.launch {
            val params = getParamsUseCase()
                .map { DomainParamMapper.map(it) }
                .filter(currentFilterPredicate)

            setState { copy(params = params) }
        }
    }

    fun setBaseFilterPredicate(predicate: ParamFilterPredicate) {
        baseFilterPredicate = predicate
    }

    private fun delete(kernelParam: KernelParam) {
        viewModelScope.launch {
            removeParamUseCase.execute(kernelParam)
            getParams()
        }
    }

    private fun update(kernelParam: KernelParam) {
        viewModelScope.launch {
            updateParamUseCase(kernelParam)
            getParams()
        }
    }
}

package com.androidvip.sysctlgui.ui.user

import androidx.lifecycle.viewModelScope
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.RemoveUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.UpsertUserParamUseCase
import com.androidvip.sysctlgui.helpers.UiKernelParamMapper
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.utils.BaseViewModel
import kotlinx.coroutines.launch

class UserParamsViewModel(
    private val getUserParams: GetUserParamsUseCase,
    private val removeParam: RemoveUserParamUseCase,
    private val upsertParam: UpsertUserParamUseCase,
) : BaseViewModel<UserParamsViewEvent, UserParamsViewState, UserParamsViewEffect>() {
    override fun createInitialState() = UserParamsViewState()

    private var mostRecentlyRemovedParam: UiKernelParam? = null

    override fun onEvent(event: UserParamsViewEvent) {
        when (event) {
            is UserParamsViewEvent.ScreenLoaded -> loadParams(event.filterPredicate)

            is UserParamsViewEvent.ParamClicked -> setEffect {
                UserParamsViewEffect.ShowParamDetails(event.param)
            }

            is UserParamsViewEvent.ParamDeleteRequested -> removeParam(event.param)

            is UserParamsViewEvent.ParamRestoreRequested -> {
                mostRecentlyRemovedParam?.let { reAddParam(it) }
            }
        }
    }

    private fun loadParams(predicate: (UiKernelParam) -> Boolean) {
        viewModelScope.launch {
            val params = getUserParams()
                .map(UiKernelParamMapper::map)
                .filter(predicate)
            setState { copy(userParams = params) }
        }
    }

    private fun removeParam(param: UiKernelParam) {
        viewModelScope.launch {
            runCatching {
                removeParam.invoke(param)
            }.onSuccess {
                setState { copy(userParams = userParams - param) }
                setEffect { UserParamsViewEffect.ShowUndoSnackBar(param) }
                mostRecentlyRemovedParam = param
            }
        }
    }

    private fun reAddParam(param: UiKernelParam) {
        viewModelScope.launch {
            runCatching {
                val newId = upsertParam(param)
                require(newId > 0)
            }.onSuccess {
                setState { copy(userParams = userParams + param) }
                mostRecentlyRemovedParam = null
            }
        }
    }
}

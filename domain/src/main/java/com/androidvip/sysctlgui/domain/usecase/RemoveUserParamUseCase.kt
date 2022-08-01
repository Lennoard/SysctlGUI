package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class RemoveUserParamUseCase(private val repository: ParamsRepository) {
    suspend fun execute(param: DomainKernelParam) = repository.removeUserParam(param)
}

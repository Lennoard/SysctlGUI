package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.UserRepository

class RemoveUserParamUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(param: KernelParam) {
        repository.removeUserParam(param)
    }
}

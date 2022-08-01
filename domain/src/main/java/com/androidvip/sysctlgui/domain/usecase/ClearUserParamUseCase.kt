package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class ClearUserParamUseCase(private val repository: ParamsRepository) {
    suspend operator fun invoke() = repository.clearUserParams()
}

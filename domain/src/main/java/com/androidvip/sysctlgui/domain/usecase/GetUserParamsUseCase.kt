package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class GetUserParamsUseCase(private val repository: ParamsRepository) {
    suspend operator fun invoke() = repository.getUserParams()
}

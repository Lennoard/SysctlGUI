package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.UserRepository

class GetUserParamsUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.getUserParams()
}

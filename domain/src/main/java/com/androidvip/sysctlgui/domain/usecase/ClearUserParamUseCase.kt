package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.UserRepository

class ClearUserParamUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() = repository.clearUserParams()
}

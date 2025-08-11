package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.UserRepository

class GetUserParamByNameUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(paramName: String) = repository.getParamByName(paramName)
}

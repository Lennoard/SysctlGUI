package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.exceptions.BlankValueNotAllowedException
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.UserRepository

class AddUserParamsUseCase(
    private val repository: UserRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(params: List<KernelParam>): List<Long> {
        if (!appPrefs.allowBlankValues) {
            if (params.any { it.value.isBlank() }) {
                throw BlankValueNotAllowedException()
            }
        }
        return repository.upsertUserParams(params)
    }
}

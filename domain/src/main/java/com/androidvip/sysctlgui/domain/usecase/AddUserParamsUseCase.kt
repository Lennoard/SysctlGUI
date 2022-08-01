package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class AddUserParamsUseCase(
    private val repository: ParamsRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(params: List<DomainKernelParam>) {
        return repository.addUserParams(params, appPrefs.allowBlankValues)
    }
}

package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class AddUserParamsUseCase(
    private val repository: ParamsRepository,
    private val appPrefs: AppPrefs
) {
    suspend fun execute(params: List<DomainKernelParam>): Result<Unit> {
        return repository.addUserParams(params, appPrefs.allowBlankValues)
    }
}

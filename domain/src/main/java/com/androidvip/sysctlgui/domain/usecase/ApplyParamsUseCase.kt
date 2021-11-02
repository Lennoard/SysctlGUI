package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class ApplyParamsUseCase(
    private val repository: ParamsRepository,
    private val appPrefs: AppPrefs
) {
    suspend fun execute(param: DomainKernelParam): Result<Unit> {
        return repository.applyParam(
            param,
            appPrefs.commitMode,
            appPrefs.useBusybox,
            appPrefs.allowBlankValues
        )
    }
}

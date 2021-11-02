package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class GetRuntimeParamsUseCase(
    private val repository: ParamsRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(): Result<List<DomainKernelParam>> {
        return repository.getRuntimeParams(appPrefs.useBusybox)
    }
}

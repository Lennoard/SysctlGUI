package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import kotlinx.coroutines.flow.single


/**
 * Fetches a single runtime kernel parameter by its name.
 *
 * This use case interacts with the [ParamsRepository] to retrieve a specific kernel parameter
 * and respects the user's preference for using BusyBox, as defined in [AppPrefs].
 */
class GetRuntimeParamUseCase(
    private val repository: ParamsRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(paramName: String): KernelParam? {
        return repository.getRuntimeParam(
            useBusybox = appPrefs.useBusybox,
            paramName = paramName
        )
    }
}

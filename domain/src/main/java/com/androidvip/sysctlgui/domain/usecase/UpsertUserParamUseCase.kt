package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.BlankValueNotAllowedException
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.UserRepository

/**
 * Updates a user-defined kernel parameter.
 *
 * @property repository The [UserRepository] to interact with user parameters.
 * @property appPrefs The [AppPrefs] to check application preferences
 * @throws BlankValueNotAllowedException if the parameter value is blank and blank values are not allowed.
 */
class UpsertUserParamUseCase(
    private val repository: UserRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(param: KernelParam): Long {
        if (param.value.isBlank() && !appPrefs.allowBlankValues) {
            throw BlankValueNotAllowedException()
        }

        return repository.upsertUserParam(param)
    }
}
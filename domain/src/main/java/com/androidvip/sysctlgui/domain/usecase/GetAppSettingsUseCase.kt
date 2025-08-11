package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.AppSetting
import com.androidvip.sysctlgui.domain.repository.AppSettingsRepository

/**
 * Use case for retrieving app settings.
 *
 * This class provides a way to fetch app settings, optionally filtering them based on a
 * provided predicate.
 *
 * @property repository The [AppSettingsRepository] used to access app settings data.
 */
class GetAppSettingsUseCase(private val repository: AppSettingsRepository) {
    suspend operator fun invoke(
        filterPredicate: (AppSetting<*>) -> Boolean = { true }
    ): List<AppSetting<*>> {
        return repository.getAppSettings().filter(filterPredicate)
    }
}

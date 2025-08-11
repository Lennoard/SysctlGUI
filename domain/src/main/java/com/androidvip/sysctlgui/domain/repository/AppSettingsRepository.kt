package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.AppSetting

fun interface AppSettingsRepository {
    suspend fun getAppSettings(): List<AppSetting<*>>
}

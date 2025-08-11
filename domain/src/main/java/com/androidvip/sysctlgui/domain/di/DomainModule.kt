package com.androidvip.sysctlgui.domain.di

import com.androidvip.sysctlgui.domain.usecase.AddUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ApplyParamUseCase
import com.androidvip.sysctlgui.domain.usecase.BackupParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ClearUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.ExportParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetAppSettingsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetParamDocumentationUseCase
import com.androidvip.sysctlgui.domain.usecase.GetParamsFromFilesUseCase
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamUseCase
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamByNameUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.IsTaskerInstalledUseCase
import com.androidvip.sysctlgui.domain.usecase.RemoveUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.UpsertUserParamUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::AddUserParamsUseCase)
    factoryOf(::ApplyParamUseCase)
    factoryOf(::ClearUserParamUseCase)
    factoryOf(::GetParamsFromFilesUseCase)
    factoryOf(::GetUserParamsUseCase)
    factoryOf(::GetRuntimeParamsUseCase)
    factoryOf(::GetRuntimeParamUseCase)
    factoryOf(::GetUserParamByNameUseCase)
    factoryOf(::RemoveUserParamUseCase)
    factoryOf(::UpsertUserParamUseCase)
    factoryOf(::BackupParamsUseCase)
    factoryOf(::ExportParamsUseCase)
    factoryOf(::GetAppSettingsUseCase)
    factoryOf(::GetParamDocumentationUseCase)
    factory { IsTaskerInstalledUseCase(androidContext()) }
}

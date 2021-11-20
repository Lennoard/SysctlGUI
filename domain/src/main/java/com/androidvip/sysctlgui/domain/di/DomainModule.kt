package com.androidvip.sysctlgui.domain.di

import com.androidvip.sysctlgui.domain.usecase.AddUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.AddUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.BackupParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ClearUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.ExportParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetJsonParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetParamsFromFilesUseCase
import com.androidvip.sysctlgui.domain.usecase.GetRuntimeParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ImportParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.PerformDatabaseMigrationUseCase
import com.androidvip.sysctlgui.domain.usecase.RemoveUserParamUseCase
import com.androidvip.sysctlgui.domain.usecase.UpdateUserParamUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AddUserParamsUseCase(get(), get()) }
    factory { AddUserParamUseCase(get(), get()) }
    factory { ApplyParamsUseCase(get(), get()) }
    factory { ClearUserParamUseCase(get()) }
    factory { GetJsonParamsUseCase(get()) }
    factory { GetParamsFromFilesUseCase(get()) }
    factory { GetUserParamsUseCase(get()) }
    factory { GetRuntimeParamsUseCase(get(), get()) }
    factory { PerformDatabaseMigrationUseCase(get()) }
    factory { RemoveUserParamUseCase(get()) }
    factory { UpdateUserParamUseCase(get(), get()) }
    factory { ImportParamsUseCase(get(), get(), get(), get()) }
    factory { BackupParamsUseCase(get(), get()) }
    factory { ExportParamsUseCase(get(), get()) }
}

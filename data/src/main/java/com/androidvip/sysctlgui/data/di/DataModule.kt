package com.androidvip.sysctlgui.data.di

import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.data.datasource.JsonParamDataSource
import com.androidvip.sysctlgui.data.datasource.RoomParamDataSource
import com.androidvip.sysctlgui.data.datasource.RuntimeParamDataSource
import com.androidvip.sysctlgui.data.db.ParamDatabase
import com.androidvip.sysctlgui.data.db.ParamDatabaseManager
import com.androidvip.sysctlgui.data.repository.AppPrefsImpl
import com.androidvip.sysctlgui.data.repository.ParamsRepositoryImpl
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    factory { RootUtils(Dispatchers.Default) }
}

val dbModule = module {
    single { ParamDatabaseManager.getInstance(androidContext()) }
    factory { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
}

val repositoryModule = module {
    factory<AppPrefs> { AppPrefsImpl(get()) }
    single<ParamsRepository> { ParamsRepositoryImpl(get(), get(), get(), get()) }
}

val dataSourceModule = module {
    single { JsonParamDataSource(androidContext()) }
    single { RuntimeParamDataSource(rootUtils = get()) }
    single {
        val db: ParamDatabase = get()
        RoomParamDataSource(db.paramDao())
    }
}

val dataModules = utilsModule + dbModule + repositoryModule + dataSourceModule

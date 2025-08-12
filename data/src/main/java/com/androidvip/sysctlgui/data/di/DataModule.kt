package com.androidvip.sysctlgui.data.di

import android.util.Log
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.data.db.ParamDatabase
import com.androidvip.sysctlgui.data.db.ParamDatabaseManager
import com.androidvip.sysctlgui.data.repository.AppPrefsImpl
import com.androidvip.sysctlgui.data.repository.AppSettingsRepositoryImpl
import com.androidvip.sysctlgui.data.repository.DocumentationRepositoryImpl
import com.androidvip.sysctlgui.data.repository.ParamsRepositoryImpl
import com.androidvip.sysctlgui.data.repository.PresetRepositoryImpl
import com.androidvip.sysctlgui.data.repository.UserRepositoryImpl
import com.androidvip.sysctlgui.data.source.DocumentationDataSource
import com.androidvip.sysctlgui.data.source.OfflineDocumentationDataSource
import com.androidvip.sysctlgui.data.source.OnlineDocumentationDataSource
import com.androidvip.sysctlgui.data.utils.AndroidStringProvider
import com.androidvip.sysctlgui.data.utils.PresetsFileProcessor
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.StringProvider
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.AppSettingsRepository
import com.androidvip.sysctlgui.domain.repository.DocumentationRepository
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import com.androidvip.sysctlgui.domain.repository.PresetRepository
import com.androidvip.sysctlgui.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val utilsModule = module {
    factoryOf(::RootUtils)
    factory { PresetsFileProcessor(androidContext().contentResolver) }
    factory<StringProvider> { AndroidStringProvider(androidApplication()) }
}

val dbModule = module {
    single { ParamDatabaseManager.getInstance(androidContext()) }
    factory { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
}

val repositoryModule = module {
    factoryOf(::AppPrefsImpl) bind AppPrefs::class
    factoryOf(::ParamsRepositoryImpl) bind ParamsRepository::class
    factoryOf(::PresetRepositoryImpl) bind PresetRepository::class
    factoryOf(::AppSettingsRepositoryImpl) bind AppSettingsRepository::class

    single<UserRepository> { UserRepositoryImpl(paramDao = get<ParamDatabase>().paramDao()) }

    factory<DocumentationRepository> {
        DocumentationRepositoryImpl(
            offlineDataSource = get(named<OfflineDocumentationDataSource>()),
            onlineDataSource = get(named<OnlineDocumentationDataSource>())
        )
    }
}

val dataSourceModule = module {
    factory<DocumentationDataSource>(named<OfflineDocumentationDataSource>()) {
        OfflineDocumentationDataSource(androidContext())
    }

    factory<DocumentationDataSource>(named<OnlineDocumentationDataSource>()) {
        OnlineDocumentationDataSource(get())
    }
}

val networkModule = module {
    single {
        HttpClient(engineFactory = Android) {
            engine {
                connectTimeout = 5000
                socketTimeout = 5000
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.v("KtorHttpClient", message)
                    }
                }
                level = LogLevel.BODY
            }
        }
    }
}

val dataModules = utilsModule + dbModule + repositoryModule + dataSourceModule + networkModule

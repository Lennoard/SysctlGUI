package com.androidvip.sysctlgui

import android.app.Application
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.data.ParamDatabase
import com.androidvip.sysctlgui.data.ParamDatabaseManager
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.ui.params.browse.BrowseParamsViewModel
import com.androidvip.sysctlgui.ui.params.list.ListParamsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class SysctlGuiApp : Application() {

    private val modules = module {
        viewModel { BrowseParamsViewModel(repository = get()) }
        viewModel { ListParamsViewModel(repository = get()) }
        single { PreferenceManager.getDefaultSharedPreferences(applicationContext) }
        single { ParamDatabaseManager.getInstance(applicationContext) }
        single {
            val db: ParamDatabase = get()
            ParamRepository(paramDao = db.paramDao(), prefs = get())
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SysctlGuiApp)
            modules(modules)
        }
    }
}


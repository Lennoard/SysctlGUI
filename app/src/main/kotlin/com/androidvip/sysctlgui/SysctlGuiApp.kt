package com.androidvip.sysctlgui

import android.app.Application
import com.androidvip.sysctlgui.data.di.dataModules
import com.androidvip.sysctlgui.di.presentationModules
import com.androidvip.sysctlgui.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SysctlGuiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SysctlGuiApp)
            modules(dataModules + presentationModules + domainModule)
        }
    }
}

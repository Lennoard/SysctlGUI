package com.androidvip.sysctlgui.data.utils

import android.app.Application
import com.androidvip.sysctlgui.domain.StringProvider

class AndroidStringProvider(private val application: Application) : StringProvider {
    override fun getString(resId: Int): String = application.getString(resId)
    override fun getString(resId: Int, vararg formatArgs: Any): String {
        return application.getString(resId, *formatArgs)
    }
}

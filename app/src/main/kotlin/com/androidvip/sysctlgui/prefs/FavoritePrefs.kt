package com.androidvip.sysctlgui.prefs

import android.content.Context
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.prefs.base.BasePrefs

class FavoritePrefs(context: Context?) : BasePrefs(context, fileName = "favorites-params.json") {

    fun isFavorite(param: KernelParameter): Boolean {
        return paramExists(param, getUserParamsSet())
    }
}

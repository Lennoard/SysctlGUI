package com.androidvip.sysctlgui.ui

import com.androidvip.sysctlgui.ui.base.BaseManageParamsActivity
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs

class ManageFavoritesParamsActivity : BaseManageParamsActivity() {
    override fun setPrefs(): BasePrefs = FavoritePrefs(applicationContext)
}

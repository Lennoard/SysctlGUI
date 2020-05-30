package com.androidvip.sysctlgui.activities

import com.androidvip.sysctlgui.activities.base.BaseManageParamsActivity
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs

class ManageFavoritesParamsActivity : BaseManageParamsActivity() {
    override fun setPrefs(): BasePrefs = FavoritePrefs(applicationContext)
}

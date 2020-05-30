package com.androidvip.sysctlgui.activities

import com.androidvip.sysctlgui.activities.base.BaseManageParamsActivity
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs

class ManageOnStartUpParamsActivity : BaseManageParamsActivity() {
    override fun setPrefs(): BasePrefs = Prefs(applicationContext)
}

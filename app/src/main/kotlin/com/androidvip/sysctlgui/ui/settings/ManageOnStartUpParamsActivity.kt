package com.androidvip.sysctlgui.ui.settings

import com.androidvip.sysctlgui.ui.base.BaseManageParamsActivity
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs

class ManageOnStartUpParamsActivity : BaseManageParamsActivity() {
    override fun setPrefs(): BasePrefs = Prefs(applicationContext)
}

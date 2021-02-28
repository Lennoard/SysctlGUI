package com.androidvip.sysctlgui.ui.settings

import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.prefs.base.BasePrefs

class ManageOnStartUpParamsActivity : BaseManageParamsActivity() {
    override val filterPredicate: (KernelParam) -> Boolean
        get() = { true }
}

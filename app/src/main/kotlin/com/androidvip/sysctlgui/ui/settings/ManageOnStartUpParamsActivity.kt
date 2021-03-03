package com.androidvip.sysctlgui.ui.settings

import com.androidvip.sysctlgui.data.models.KernelParam

class ManageOnStartUpParamsActivity : BaseManageParamsActivity() {
    override val filterPredicate: (KernelParam) -> Boolean
        get() = { true }
}

package com.androidvip.sysctlgui.ui.params.user

import com.androidvip.sysctlgui.data.models.KernelParam

class ManageOnStartUpParamsActivity : BaseManageParamsActivity() {
    override val filterPredicate: (KernelParam) -> Boolean
        get() = { true }
}

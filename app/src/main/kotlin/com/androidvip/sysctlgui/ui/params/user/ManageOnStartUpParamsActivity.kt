package com.androidvip.sysctlgui.ui.params.user

import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam

class ManageOnStartUpParamsActivity : BaseManageParamsActivity() {
    override val title: String
        get() = getString(R.string.manage_parameters)

    override val filterPredicate: (KernelParam) -> Boolean
        get() = { true }
}

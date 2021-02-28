package com.androidvip.sysctlgui.ui.settings

import com.androidvip.sysctlgui.data.models.KernelParam

class ManageFavoritesParamsActivity : BaseManageParamsActivity() {
    override val filterPredicate: (KernelParam) -> Boolean
        get() = { it.favorite }
}

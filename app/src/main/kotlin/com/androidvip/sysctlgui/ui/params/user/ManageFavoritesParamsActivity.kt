package com.androidvip.sysctlgui.ui.params.user

import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam

class ManageFavoritesParamsActivity : BaseManageParamsActivity() {
    override val title: String
        get() = getString(R.string.tasker_list_plugin_favorites)

    override val filterPredicate: (KernelParam) -> Boolean
        get() = { it.favorite }
}

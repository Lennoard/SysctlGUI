package com.androidvip.sysctlgui.ui.params

import com.androidvip.sysctlgui.data.models.KernelParam

interface OnParamItemClickedListener {
    fun onParamItemClicked(param: KernelParam)
}

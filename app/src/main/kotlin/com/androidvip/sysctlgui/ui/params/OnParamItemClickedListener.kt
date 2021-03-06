package com.androidvip.sysctlgui.ui.params

import android.view.View
import com.androidvip.sysctlgui.data.models.KernelParam

interface OnParamItemClickedListener {
    fun onParamItemClicked(param: KernelParam, itemLayout: View)
}

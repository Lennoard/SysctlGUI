package com.androidvip.sysctlgui.ui.params

import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.androidvip.sysctlgui.data.models.KernelParam

interface OnPopUpMenuItemSelectedListener {
    fun onPopUpMenuItemSelected(
        kernelParam: KernelParam,
        @IdRes itemId: Int,
        removableLayout: ConstraintLayout
    )
}

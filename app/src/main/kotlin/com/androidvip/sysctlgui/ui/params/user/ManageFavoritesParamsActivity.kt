package com.androidvip.sysctlgui.ui.params.user

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.addListener
import com.androidvip.sysctlgui.data.models.KernelParam

class ManageFavoritesParamsActivity : BaseManageParamsActivity() {

    override fun onRemoveRequested(
        kernelParam: KernelParam,
        fakeGesture: Boolean,
        removableLayout: View
    ) {
        kernelParam.favorite = false

        if (fakeGesture) {
            val viewWidth = removableLayout.measuredWidth
            ObjectAnimator.ofFloat(
                removableLayout,
                "translationX",
                viewWidth * (-1F)
            ).apply {
                duration = 300
                start()
            }.addListener(onEnd = {
                paramViewModel.update(kernelParam)
            })
        } else {
            paramViewModel.update(kernelParam)
        }
    }

    override val filterPredicate: (KernelParam) -> Boolean
        get() = { it.favorite }
}

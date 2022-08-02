package com.androidvip.sysctlgui.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

abstract class BaseBottomSheetFragment<Binding : ViewBinding> : BottomSheetDialogFragment() {

    lateinit var binding: Binding

    abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = this.setViewBinding(inflater, container)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val bottomSheetBehavior = BottomSheetBehavior.from(view?.parent as? View ?: return)
        bottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            val shape = createMaterialShapeDrawable(bottomSheet)
                            ViewCompat.setBackground(bottomSheet, shape)
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                        else -> Unit
                    }
                }
            }
        )
    }

    private fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable {
        val shapeAppearanceModel = ShapeAppearanceModel.builder(
            context,
            0,
            R.style.ShapeAppearance_SysctlGui_BottomSheet
        ).build()

        val currentShape = bottomSheet.background as MaterialShapeDrawable
        return MaterialShapeDrawable(shapeAppearanceModel).apply {
            initializeElevationOverlay(context)
            fillColor = currentShape.fillColor
            tintList = currentShape.tintList
            elevation = currentShape.elevation
            strokeWidth = currentShape.strokeWidth
            strokeColor = currentShape.strokeColor
        }
    }
}

package com.androidvip.sysctlgui.utils

import android.content.res.ColorStateList
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import com.androidvip.sysctlgui.R
import com.google.android.material.color.MaterialColors
import java.io.File

@BindingAdapter("binding:srcCompatRes")
fun AppCompatImageView.setImageResourceCompat(@DrawableRes res: Int) {
    setImageResource(res)
}

@BindingAdapter("binding:iconTintForFile")
fun AppCompatImageView.setIconTintForFile(file: File) {
    val attr = if (file.isDirectory) R.attr.colorOnPrimaryContainer else R.attr.colorOnSurface
    val color = MaterialColors.getColor(this, attr)
    val colorStateList = ColorStateList.valueOf(color)
    ImageViewCompat.setImageTintList(this, colorStateList)
}

@BindingAdapter("binding:typefaceForFile")
fun AppCompatTextView.setTypefaceForFile(file: File) {
    val familyName = if (file.isDirectory) "sans-serif-medium" else "sans-serif"
    typeface = Typeface.create(familyName, Typeface.NORMAL)
}

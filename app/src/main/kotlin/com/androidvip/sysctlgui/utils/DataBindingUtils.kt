package com.androidvip.sysctlgui.utils

import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("binding:srcCompatRes")
fun AppCompatImageView.setImageResourceCompat(@DrawableRes res: Int) {
    setImageResource(res)
}

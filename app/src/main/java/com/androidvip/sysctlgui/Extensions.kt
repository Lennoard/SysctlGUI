package com.androidvip.sysctlgui

import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.goAway() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Snackbar.showAsDark() {
    view.setBackgroundColor(Color.parseColor("#cfd8dc"))
    setTextColor(Color.parseColor("#DE000000"))
    show()
}

package com.androidvip.sysctlgui

import android.view.View

fun View.goAway() {
    this.visibility = View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

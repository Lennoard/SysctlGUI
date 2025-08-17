package com.androidvip.sysctlgui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun Context?.toast(messageRes: Int, length: Int = Toast.LENGTH_SHORT) {
    if (this == null) return
    toast(getString(messageRes), length)
}

fun Context?.toast(message: String?, length: Int = Toast.LENGTH_SHORT) {
    if (message == null || this == null) return
    val ctx = this

    runOnUiThread { Toast.makeText(ctx, message, length).show() }
}

fun Context.runOnUiThread(f: Context.() -> Unit) {
    if (Looper.getMainLooper() === Looper.myLooper()) f() else Handler(Looper.getMainLooper()).post {
        f()
    }
}

@ExperimentalContracts
fun Bundle?.isValidTaskerBundle() : Boolean {
    contract {
        returns(true) implies (this@isValidTaskerBundle != null)
    }
    return this != null && containsKey(TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER)
}

package com.androidvip.sysctlgui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import com.google.android.material.color.ColorRoles
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun View.goAway() { this.visibility = View.GONE }
fun View.hide() { this.visibility = View.INVISIBLE }
fun View.show() { this.visibility = View.VISIBLE }

fun View.getColorRoles(@AttrRes colorAttrRes: Int = R.attr.colorSecondary): ColorRoles {
    val color = MaterialColors.getColor(this, colorAttrRes)
    return MaterialColors.getColorRoles(context, color)
}

fun Snackbar.showAsLight() {
    view.setBackgroundColor(Color.parseColor("#cfd8dc"))
    setTextColor(Color.parseColor("#DE000000"))
    show()
}

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

fun Uri.readLines(context: Context?, forEachLine: (String) -> Unit) {
    context?.contentResolver?.openInputStream(this).readLines(forEachLine)
}

fun InputStream?.readLines(forEachLine: (String) -> Unit) {
    this?.use { inputStream ->
        inputStream.bufferedReader().use {
            it.readLines().forEach { line ->
                forEachLine(line)
            }
        }
    }
}

@ExperimentalContracts
fun Bundle?.isValidTaskerBundle() : Boolean {
    contract {
        returns(true) implies (this@isValidTaskerBundle != null)
    }
    return this != null && containsKey(TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER)
}

suspend inline fun Activity?.runSafeOnUiThread(crossinline uiBlock: () -> Unit) {
    this?.let {
        if (!it.isFinishing && !it.isDestroyed) {
            withContext(Dispatchers.Main) {
                runCatching(uiBlock)
            }
        }
    }
}

package com.androidvip.sysctlgui.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun Context.browse(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    runCatching { startActivity(intent) }
}


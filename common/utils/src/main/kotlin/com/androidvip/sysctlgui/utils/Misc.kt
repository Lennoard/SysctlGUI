package com.androidvip.sysctlgui.utils

import android.view.View
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat

/**
 * Checks if a string is a valid sysctl line.
 * A valid sysctl line must:
 * - Contain exactly one "=" character.
 * - Not have blank parts before or after the "=".
 * - Have a key that matches the pattern: `^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+$`
 *   (e.g., "vm.swappiness", "net.ipv4.tcp_congestion_control").
 *
 * @return `true` if the string is a valid sysctl line, `false` otherwise.
 */
fun String.isValidSysctlLine(): Boolean {
    val parts = this.split("=", limit = 2)
    if (parts.size != 2 || parts.any { it.isBlank() }) return false

    val keyPattern = "^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+$".toRegex()
    return keyPattern.matches(parts.first())
}

fun performHapticFeedbackForToggle(newState: Boolean, view: View) {
    val feedbackConst = if (newState) {
        HapticFeedbackConstantsCompat.TOGGLE_ON
    } else {
        HapticFeedbackConstantsCompat.TOGGLE_OFF
    }
    ViewCompat.performHapticFeedback(view, feedbackConst)
}

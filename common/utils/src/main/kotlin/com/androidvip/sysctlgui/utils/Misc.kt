package com.androidvip.sysctlgui.utils

import android.view.View
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat


/**
 * Checks if a string is a valid sysctl line.
 * A valid sysctl line must:
 * - Contain an "=" character.
 * - Have a key that matches the pattern: `^[a-zA-Z0-9_]+(?:\.[a-zA-Z0-9_]+)*$`
 *   (e.g., "vm.swappiness", "net.ipv4.tcp_congestion_control").
 * - Have a non-blank value after the "=".
 *
 * @return `true` if the string is a valid sysctl line, `false` otherwise.
 */
fun String.isValidSysctlOutputLine(): Boolean {
    val trimmedLine = this.trim()
    val linePattern = """^([a-zA-Z0-9_]+(?:\.[a-zA-Z0-9_]+)*)\s*=\s*(.+)$""".toRegex()
    val matchResult = linePattern.matchEntire(trimmedLine)

    return matchResult != null
}

fun performHapticFeedbackForToggle(newState: Boolean, view: View) {
    val feedbackConst = if (newState) {
        HapticFeedbackConstantsCompat.TOGGLE_ON
    } else {
        HapticFeedbackConstantsCompat.TOGGLE_OFF
    }
    ViewCompat.performHapticFeedback(view, feedbackConst)
}

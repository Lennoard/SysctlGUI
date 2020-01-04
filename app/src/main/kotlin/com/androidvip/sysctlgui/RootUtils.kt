package com.androidvip.sysctlgui

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object RootUtils {

    fun isBusyboxAvailable(): Boolean {
        val results: List<String> = Shell.sh("which busybox").exec().out
        return if (ShellUtils.isValidOutput(results)) {
            results.last().isNotEmpty()
        } else false
    }

    /**
     * To be used when applying on boot
     */
    fun executeSync(vararg commands: String) : String {
        val out = Shell.su(*commands).exec().out
        return if (ShellUtils.isValidOutput(out)) out.last() else ""
    }

    /**
     * To be used when applying on boot
     */
    fun executeAsync(vararg commands: String) {
        GlobalScope.launch {
            runCatching {
                Shell.su(*commands).exec()
            }.isSuccess
        }
    }

    fun executeWithOutput(command: String, defaultOutput: String = "", forEachLine: ((String?) -> Unit)? = null): String {
        val sb = StringBuilder()

        try {
            val outputs = Shell.su(command).exec().out
            if (ShellUtils.isValidOutput(outputs)) {
                outputs.forEach {
                    if (forEachLine != null) {
                        forEachLine(it)
                        sb.append(it).append("\n")
                    } else {
                        sb.append(it).append("\n")
                    }
                }
            }
        } catch (e: Exception) {
            return defaultOutput
        }

        return sb.toString().trim().removeSuffix("\n")
    }

    fun finishProcess() {
        runCatching {
            Shell.getCachedShell()?.close()
        }
    }
}
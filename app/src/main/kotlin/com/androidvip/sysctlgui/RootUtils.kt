package com.androidvip.sysctlgui

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootUtils {

    suspend fun isBusyboxAvailable(): Boolean = withContext(Dispatchers.Default) {
        val results: List<String> = Shell.sh("which busybox").exec().out
        return@withContext if (ShellUtils.isValidOutput(results)) {
            results.first().isNotEmpty()
        } else false
    }

    suspend fun executeWithOutput(
        command: String,
        defaultOutput: String = "",
        forEachLine: ((String?) -> Unit)? = null
    ): String = withContext(Dispatchers.Default) {
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
            return@withContext defaultOutput
        }

        return@withContext sb.toString().trim().removeSuffix("\n")
    }

    fun finishProcess() {
        runCatching {
            Shell.getCachedShell()?.close()
        }
    }
}
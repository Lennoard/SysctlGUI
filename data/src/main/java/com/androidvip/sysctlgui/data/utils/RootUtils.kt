package com.androidvip.sysctlgui.data.utils

import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RootUtils(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) {

    suspend fun isBusyboxAvailable(): Boolean = withContext(dispatcher) {
        val results: List<String> = Shell.cmd("which busybox").exec().out
        return@withContext ShellUtils.isValidOutput(results) && results.firstOrNull()
            ?.isNotEmpty() == true
    }

    suspend fun executeWithOutput(
        command: String,
        defaultOutput: String = "",
        forEachLine: ((String) -> Unit)? = null
    ): String = withContext(dispatcher) {
        return@withContext runCatching {
            buildString {
                val outputs = Shell.cmd(command).exec().out
                if (!ShellUtils.isValidOutput(outputs)) {
                    append(defaultOutput)
                    return@buildString
                }
                outputs.forEach { line ->
                    if (forEachLine != null) {
                        forEachLine(line.orEmpty())
                        appendLine(line.orEmpty())
                    } else {
                        appendLine(line.orEmpty())
                    }
                }
            }.trim().removeSuffix("\n")
        }.getOrDefault(defaultOutput)
    }

    fun finishProcess() {
        runCatching {
            Shell.getCachedShell()?.close()
        }
    }
}

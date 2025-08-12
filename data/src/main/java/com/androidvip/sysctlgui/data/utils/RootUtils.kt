package com.androidvip.sysctlgui.data.utils

import android.util.Log
import com.androidvip.sysctlgui.domain.exceptions.ShellCommandException
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RootUtils(private val shellDispatcher: CoroutineDispatcher = Dispatchers.Default) {
    suspend fun getRootShell(): Shell? = withContext(shellDispatcher) {
        Shell.getShell().takeIf { it.isRoot }
    }

    suspend fun isRootAvailable(): Boolean = withContext(shellDispatcher) {
        Shell.isAppGrantedRoot() == true
    }

    suspend fun isBusyboxAvailable(): Boolean = withContext(shellDispatcher) {
        val results: List<String> = Shell.cmd("which busybox").exec().out
        return@withContext ShellUtils.isValidOutput(results) && results.firstOrNull()
            ?.isNotEmpty() == true
    }

    fun executeCommandAndStreamOutput(command: String): Flow<String> = flow {
        val result = Shell.cmd(command).exec()
        val outputs = result.out

        if (ShellUtils.isValidOutput(outputs)) {
            outputs.forEach { line ->
                emit(line.orEmpty())
            }
        } else {
            if (result.isSuccess.not()) {
                result.err.forEach { errorLine -> Log.e("RootUtils", errorLine) }
                throw ShellCommandException(
                    message = "Command execution failed",
                    cause = Exception(result.err.joinToString("\n"))
                )
            }
        }
    }.flowOn(shellDispatcher)

    fun finishProcess() {
        runCatching {
            Shell.getCachedShell()?.close()
        }
    }
}

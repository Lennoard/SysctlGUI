package com.androidvip.sysctlgui.data.datasource

import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.datasource.RuntimeDataSourceContract
import com.androidvip.sysctlgui.domain.exceptions.ApplyValueException
import com.androidvip.sysctlgui.domain.exceptions.CommitModeException
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import java.io.File
import java.lang.IllegalArgumentException

class RuntimeParamDataSource(
    private val rootUtils: RootUtils
) : RuntimeDataSourceContract<DomainKernelParam> {
    override suspend fun edit(
        param: DomainKernelParam,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    ) {
        val commitResult = commitChanges(param, commitMode, useBusybox, allowBlank)

        when {
            commitMode == "sysctl" -> {
                if (commitResult == "error" || !commitResult.contains(param.name)) {
                    throw CommitModeException("Value refused to apply. Try using 'echo' mode.")
                }
            }
            commitResult == "error" -> {
                throw ApplyValueException("Value refused to apply")
            }
        }
    }

    override suspend fun getData(useBusybox: Boolean): List<DomainKernelParam> {
        val command = if (useBusybox) "busybox sysctl -a" else "sysctl -a"
        val lines = mutableListOf<String>()
        rootUtils.executeWithOutput(command) { lines += it }

        return lines.filter {
            it.isValidSysctlOutput()
        }.map {
            // Expected output: grandparent.parent.name = value
            val split = it.split("=")
            split.first().trim() to split.last().trim()
        }.mapIndexed { index, paramPair ->
            DomainKernelParam(
                id = index + 1,
                name = paramPair.first,
                value = paramPair.second
            ).apply {
                setPathFromName(paramPair.first)
            }
        }
    }

    override suspend fun getParamsFromFiles(files: List<File>): List<DomainKernelParam> {
        return files.map {
            it.absolutePath
        }.mapIndexed { index, path ->
            DomainKernelParam(
                id = index + 1,
                path = path
            ).apply {
                setNameFromPath(path)
                value = rootUtils.executeWithOutput("cat $path", "")
            }
        }
    }

    private suspend fun commitChanges(
        param: DomainKernelParam,
        commitMode: String,
        useBusybox: Boolean,
        allowBlank: Boolean
    ): String {
        if (!allowBlank && param.value.isBlank()) throw IllegalArgumentException(
            "Param contains blank value while ALLOW_BLANK is not active"
        )

        val prefix = if (useBusybox) "busybox " else ""
        val command = when (commitMode) {
            "sysctl" -> "${prefix}sysctl -w ${param.name}=${param.value}"
            "echo" -> "echo '${param.value}' > ${param.path}"
            else -> "busybox sysctl -w ${param.name}=${param.value}"
        }

        return rootUtils.executeWithOutput(command, "error")
    }

    private fun String.isValidSysctlOutput(): Boolean {
        return !contains("denied") && !startsWith("sysctl") && contains("=")
    }
}

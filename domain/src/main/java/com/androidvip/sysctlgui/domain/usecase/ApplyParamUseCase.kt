package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.exceptions.ApplyValueException
import com.androidvip.sysctlgui.domain.exceptions.BlankValueNotAllowedException
import com.androidvip.sysctlgui.domain.exceptions.CommitModeException
import com.androidvip.sysctlgui.domain.exceptions.ShellCommandException
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class ApplyParamUseCase(
    private val repository: ParamsRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(param: KernelParam) {
        if (param.value.isBlank() && !appPrefs.allowBlankValues) {
            throw BlankValueNotAllowedException()
        }

        val commitMode = CommitMode.parse(appPrefs.commitMode)

        try {
            val output = repository.setRuntimeParam(
                param = param,
                commitMode = commitMode,
                useBusybox = appPrefs.useBusybox,
            )
            when (commitMode) {
                CommitMode.SYSCTL -> {
                    if (!output.contains(param.name)) {
                        throw CommitModeException(
                            "Sysctl command for '${param.name}' executed, but output did not confirm the change. " +
                                    "Output: '$output'. Try using '${CommitMode.ECHO}' mode."
                        )
                    }
                }

                CommitMode.ECHO -> {
                    if (output.isEmpty().not()) {
                        throw CommitModeException(
                            "Echo command for '${param.path}' executed, but output was not empty. " +
                                    "Output: '$output'. Try using '${CommitMode.SYSCTL}' mode."
                        )
                    }
                }
            }

        } catch (e: ShellCommandException) {
            val message = e.cause?.message.orEmpty()
            throwApplyValueException(
                message = "$message <- ${e.message}",
                commitMode = commitMode,
                param = param
            )
        } catch (e: Exception) {
            throwApplyValueException(
                message = e.message.orEmpty(),
                commitMode = commitMode,
                param = param
            )
        }
    }

    private fun throwApplyValueException(
        message: String,
        commitMode: CommitMode,
        param: KernelParam
    ) {
        val errorMessage = when (commitMode) {
            CommitMode.SYSCTL -> "Failed to execute sysctl command for '${param.name}'"
            CommitMode.ECHO -> "Failed to write value '${param.value}' to '${param.path}'"
        }
        throw ApplyValueException(errorMessage)
    }
}

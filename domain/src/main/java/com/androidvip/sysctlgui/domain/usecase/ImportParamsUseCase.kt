package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.InvalidFileExtensionException
import com.androidvip.sysctlgui.domain.exceptions.NoValidParamException
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.InputStream

class ImportParamsUseCase(
    private val clearUserParamUseCase: ClearUserParamUseCase,
    private val addUserParamsUseCase: AddUserParamsUseCase,
    private val applyParamsUseCase: ApplyParamsUseCase,
    private val repository: ParamsRepository
) {
    suspend operator fun invoke(
        stream: InputStream,
        fileExtension: String
    ): List<DomainKernelParam> {
        val isBackup = fileExtension.endsWith(".conf")
        val params = when {
            fileExtension.endsWith(".json") -> repository.importParamsFromJson(stream)
            isBackup -> repository.importParamsFromConf(stream)
            else -> throw InvalidFileExtensionException()
        }

        if (params.isEmpty()) throw NoValidParamException()

        val successfulParams = mutableListOf<DomainKernelParam>()
        params.forEach { param ->
            // Apply the param to check if valid
            runCatching { applyParamsUseCase(param) }.onSuccess {
                successfulParams.add(param)
            }
        }

        clearUserParamUseCase()

        // Prevent adding full backups to the apply-on-boot list
        if (!isBackup) {
            addUserParamsUseCase(successfulParams)
        }

        return successfulParams
    }
}

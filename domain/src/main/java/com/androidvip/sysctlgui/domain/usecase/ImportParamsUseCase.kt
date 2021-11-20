package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.InvalidFileExtensionException
import com.androidvip.sysctlgui.domain.exceptions.NoValidParamException
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.InputStream

class ImportParamsUseCase(
    private val clearUserParamUseCase: ClearUserParamUseCase,
    private val addUserParamsUseCase: AddUserParamsUseCase,
    private val applyParamsUseCase: ApplyParamsUseCase,
    private val repository: ParamsRepository
) {
    suspend fun execute(
        stream: InputStream,
        fileExtension: String
    ): Result<List<DomainKernelParam>> {
        return runCatching {
            val importResult = when {
                fileExtension.endsWith(".json") -> repository.importParamsFromJson(stream)
                fileExtension.endsWith(".conf") -> repository.importParamsFromConf(stream)
                else -> throw InvalidFileExtensionException()
            }

            val params = importResult.getOrThrow()
            if (params.isEmpty()) throw NoValidParamException()

            val successfulParams = mutableListOf<DomainKernelParam>()
            params.forEach {
                // Apply the param to check if valid
                val result = applyParamsUseCase.execute(it)
                if (result.isSuccess) {
                    successfulParams.add(it)
                }
            }

            clearUserParamUseCase.execute()
            addUserParamsUseCase.execute(successfulParams)

            successfulParams
        }
    }
}

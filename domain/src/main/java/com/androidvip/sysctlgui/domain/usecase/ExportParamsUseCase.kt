package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.FileDescriptor

class ExportParamsUseCase(
    private val getUserParamUseCase: GetUserParamsUseCase,
    private val repository: ParamsRepository
) {
    suspend fun execute(fileDescriptor: FileDescriptor): Result<Unit> {
        val params = getUserParamUseCase().getOrNull()
        if (params.isNullOrEmpty()) return Result.failure(NoParameterFoundException())

        return repository.exportParams(params, fileDescriptor)
    }
}

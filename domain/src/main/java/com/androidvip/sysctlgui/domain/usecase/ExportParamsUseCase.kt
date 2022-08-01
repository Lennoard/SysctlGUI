package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.FileDescriptor

class ExportParamsUseCase(
    private val getUserParamUseCase: GetUserParamsUseCase,
    private val repository: ParamsRepository
) {
    suspend operator fun invoke(fileDescriptor: FileDescriptor) {
        val params = getUserParamUseCase()
        if (params.isEmpty()) throw NoParameterFoundException()

        return repository.exportParams(params, fileDescriptor)
    }
}

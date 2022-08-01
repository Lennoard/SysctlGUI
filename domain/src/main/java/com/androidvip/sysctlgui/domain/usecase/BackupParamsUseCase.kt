package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.FileDescriptor

class BackupParamsUseCase(
    private val getRuntimeParamsUseCase: GetRuntimeParamsUseCase,
    private val repository: ParamsRepository
) {
    suspend operator fun invoke(fileDescriptor: FileDescriptor) {
        val params = getRuntimeParamsUseCase()
        if (params.isEmpty()) throw NoParameterFoundException()

        return repository.backupParams(params, fileDescriptor)
    }
}

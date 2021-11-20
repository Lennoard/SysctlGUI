package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.FileDescriptor

class BackupParamsUseCase(
    private val getRuntimeParamsUseCase: GetRuntimeParamsUseCase,
    private val repository: ParamsRepository
) {
    suspend fun execute(fileDescriptor: FileDescriptor): Result<Unit> {
        val params = getRuntimeParamsUseCase().getOrNull()
        if (params.isNullOrEmpty()) return Result.failure(Exception())

        return repository.backupParams(params, fileDescriptor)
    }
}

package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.repository.PresetRepository
import java.io.FileDescriptor

class BackupParamsUseCase(
    private val getRuntimeParams: GetRuntimeParamsUseCase,
    private val repository: PresetRepository
) {
    suspend operator fun invoke(fileDescriptor: FileDescriptor) {
        val params = getRuntimeParams()
        if (params.isEmpty()) throw NoParameterFoundException()

        return repository.backupParams(params, fileDescriptor)
    }
}

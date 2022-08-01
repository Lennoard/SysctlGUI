package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import java.io.File

class GetParamsFromFilesUseCase(private val repository: ParamsRepository) {
    suspend operator fun invoke(files: List<File>): List<DomainKernelParam> {
        return repository.getParamsFromFiles(files)
    }
}

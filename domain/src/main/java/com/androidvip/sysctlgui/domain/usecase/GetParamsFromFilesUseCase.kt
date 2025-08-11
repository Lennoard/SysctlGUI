package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import kotlinx.coroutines.flow.single
import java.io.File

class GetParamsFromFilesUseCase(private val repository: ParamsRepository) {
    suspend operator fun invoke(files: List<File>): List<KernelParam> {
        return repository.getParamsFromFiles(files).single()
    }
}

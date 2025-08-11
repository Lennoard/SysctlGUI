package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.repository.PresetRepository
import java.io.FileDescriptor

/**
 * Exports the current user parameters to a preset file.
 *
 * @throws NoParameterFoundException if there are no parameters to export.
 */
class ExportParamsUseCase(
    private val getUserParams: GetUserParamsUseCase,
    private val repository: PresetRepository
) {
    /**
     * @param fileDescriptor The file descriptor to write the preset to.
     */
    suspend operator fun invoke(fileDescriptor: FileDescriptor) {
        val params = getUserParams()
        if (params.isEmpty()) throw NoParameterFoundException()

        return repository.exportToPreset(params, fileDescriptor)
    }
}

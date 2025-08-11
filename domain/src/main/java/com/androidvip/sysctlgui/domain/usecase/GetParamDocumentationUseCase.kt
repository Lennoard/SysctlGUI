package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.DocumentationRepository

class GetParamDocumentationUseCase(
    private val repository: DocumentationRepository,
    private val appPrefs: AppPrefs
) {
    suspend operator fun invoke(param: KernelParam): ParamDocumentation? {
        return repository.getDocumentation(param, appPrefs.useOnlineDocs)
    }
}

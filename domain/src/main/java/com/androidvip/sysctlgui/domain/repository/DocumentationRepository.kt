package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation

/**
 * Repository interface for fetching documentation for kernel parameters.
 */
fun interface DocumentationRepository {
    /**
     * Retrieves documentation for a given kernel parameter.
     *
     * @param param The kernel parameter for which to fetch documentation.
     * @param online Whether to use the online documentation source.
     * @return The documentation if found, null otherwise.
     */
    suspend fun getDocumentation(param: KernelParam, online: Boolean): ParamDocumentation?
}

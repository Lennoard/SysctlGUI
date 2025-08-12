package com.androidvip.sysctlgui.data.source

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation


/**
 * Data source interface for fetching documentation for kernel parameters.
 * This interface defines the contract for any class that provides access
 * to kernel parameter documentation.
 */
fun interface DocumentationDataSource {
    /**
     * Retrieves documentation for a given kernel parameter.
     *
     * @param param The kernel parameter for which to fetch documentation.
     * @return The documentation if found, null otherwise.
     */
    suspend fun getDocumentation(param: KernelParam): ParamDocumentation?
}

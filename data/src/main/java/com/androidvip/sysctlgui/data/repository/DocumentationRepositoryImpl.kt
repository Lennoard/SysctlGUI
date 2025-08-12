package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.source.DocumentationDataSource
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.DocumentationRepository

/**
 * Repository for fetching documentation for kernel parameters.
 *
 * This repository can fetch documentation from either an online or offline data source,
 * depending on the user's preference set in [AppPrefs].
 *
 * @property offlineDataSource The data source for fetching documentation offline.
 * @property onlineDataSource The data source for fetching documentation online.
 * @property appPrefs The application preferences, used to determine whether to use online or
 * offline documentation.
 */
class DocumentationRepositoryImpl(
    private val offlineDataSource: DocumentationDataSource,
    private val onlineDataSource: DocumentationDataSource
) : DocumentationRepository {
    override suspend fun getDocumentation(
        param: KernelParam,
        online: Boolean
    ): ParamDocumentation? {
        return if (online) {
            onlineDataSource.getDocumentation(param)
        } else {
            offlineDataSource.getDocumentation(param)
        }
    }
}

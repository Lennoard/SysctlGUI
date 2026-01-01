package com.androidvip.sysctlgui.data.models

import com.androidvip.sysctlgui.domain.models.ParamDocumentation

data class ParamDocumentationDTO(
    override val title: String = "",
    override val documentationText: String = "",
    override val documentationHtml: String? = null,
    override val url: String? = null
) : ParamDocumentation

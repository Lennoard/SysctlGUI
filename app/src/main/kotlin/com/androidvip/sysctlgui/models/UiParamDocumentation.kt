package com.androidvip.sysctlgui.models

import androidx.compose.runtime.Immutable
import com.androidvip.sysctlgui.domain.models.ParamDocumentation

@Immutable
data class UiParamDocumentation(
    override val title: String = "",
    override val documentationText: String = "",
    override val documentationHtml: String? = null,
    override val url: String? = null
) : ParamDocumentation

fun ParamDocumentation.toUiParamDocumentation() = UiParamDocumentation(
    title = this.title,
    documentationText = this.documentationText,
    documentationHtml = this.documentationHtml,
    url = this.url
)

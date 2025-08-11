package com.androidvip.sysctlgui.domain.models

/**
 * Represents documentation for a kernel parameter.
 *
 * @property title The title of the documentation.
 * @property documentationText The plain text documentation.
 * @property documentationHtml The HTML formatted documentation, if available.
 * @property url The URL to the online documentation, if available.
 */
data class ParamDocumentation(
    val title: String = "",
    val documentationText: String = "",
    val documentationHtml: String? = null,
    val url: String? = null
)

package com.androidvip.sysctlgui.data.source

import android.util.Log
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.coroutines.CoroutineContext


class OnlineDocumentationDataSource(
    private val client: HttpClient,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : DocumentationDataSource {

    /**
     * Fetches the documentation for a given kernel parameter.
     *
     * This function constructs the documentation URL based on the parameter's name,
     * retrieves the HTML content from that URL using Ktor and extracts the
     * relevant documentation text using Jsoup.
     *
     * @param param The [KernelParam] for which to fetch documentation.
     * @return The documentation text as a [ParamDocumentation], or `null` if the
     * documentation could not be found or an error occurred.
     */
    override suspend fun getDocumentation(
        param: KernelParam
    ): ParamDocumentation? = withContext(coroutineContext) {
        val url = getDocumentationUrl(param)

        return@withContext runCatching {
            val response = client.get(urlString = url)

            if (!response.status.isSuccess()) {
                Log.w(
                    "OnlineDocRepo",
                    "Failed to fetch docs from $url. Status: ${response.status}"
                )
                return@withContext null
            }

            val html = response.bodyAsText()
            val document = Jsoup.parse(html)
            val htmlElementId = param.lastNameSegment.replace('_', '-')
            val elements = document.select("section#$htmlElementId p")

            if (elements.isEmpty()) {
                Log.w(
                    "OnlineDocRepo",
                    "No documentation found for ${param.name} with id $htmlElementId on $url"
                )
                return@withContext null
            }

            return@withContext ParamDocumentation(
                title = param.name,
                documentationText = elements.text(),
                documentationHtml = elements.html().optimizedDocumentationHtml(),
                url = url
            )
        }.getOrElse {
            Log.w("OnlineDocRepo", "Failed to fetch docs from $url", it)
            return@withContext null
        }
    }

    private fun getDocumentationUrl(param: KernelParam): String {
        val configName = param.groupName
        return "${DOC_BASE_URL}$configName.html#${param.name}"
    }

    /**
     * Optimizes HTML documentation for display.
     *
     * This function performs a series of replacements on the input HTML string
     * to try and improve its rendering in a basic HTML text renderer, such as Android's TextView.
     * @return The optimized HTML string.
     */
    private fun String.optimizedDocumentationHtml(): String {
        return this.trimIndent()
            .replace("<pre>", "<font face=\"monospace\"><b>")
            .replace("</pre>", "</b><font>") // For "code" blocks
            .replace("<code>", "<font face=\"monospace\" color=\"#222\"><b><span style=\"background-color: #DCDCF5\">")
            .replace("</code>", "</span></b></font>") // For code tags
            .replace("<li><p>", "<li>")
            .replace("</p></li>", "</li>") // For spaced bullet points
            .replace("<p>", "<br /><p>") // For line breaks in paragraphs
            .removeSuffix("<br />") // Remove the last line break
    }

    companion object {
        internal const val DOC_BASE_URL = "https://docs.kernel.org/admin-guide/sysctl/"
    }
}

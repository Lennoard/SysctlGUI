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
import java.io.File
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


            if (File(param.path).isDirectory) {
                // If we got something out of the request, might as well return at least the URL
                return@withContext ParamDocumentation(
                    title = param.name,
                    documentationText = "",
                    documentationHtml = "", // HTML might be huge (directory documentation)
                    url = url
                )
            }

            val elements = document.select("section#$htmlElementId :not(h2)")

            if (elements.isEmpty()) {
                Log.w(
                    "OnlineDocRepo",
                    "No documentation found for ${param.name} with id $htmlElementId on $url"
                )
                return@withContext null
            } else {
                // Remove first element (usually a heading remnant)
                elements.removeAt(0)
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
        if (File(param.path).isDirectory) {
            return "${DOC_BASE_URL}${param.name}.html"
        }
        val configName = param.groupName
        return "${DOC_BASE_URL}$configName.html#${param.lastNameSegment.replace('_', '-')}"
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
            .replace("<code ", "<font face=\"monospace\" color=\"#222\"><b><span style=\"background-color: #DCDCF5\" ")
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

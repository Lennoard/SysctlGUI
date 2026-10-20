package com.androidvip.sysctlgui.data.source

import android.util.Log
import com.androidvip.sysctlgui.data.models.ParamDocumentationDTO
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
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
                return@withContext ParamDocumentationDTO(
                    title = param.name,
                    documentationText = "",
                    documentationHtml = "", // HTML might be huge (directory documentation)
                    url = url
                )
            }

            val elements = document.documentationElements(htmlElementId)

            if (elements.isEmpty()) {
                Log.w(
                    "OnlineDocRepo",
                    "No documentation found for ${param.name} with id $htmlElementId on $url"
                )
                return@withContext null
            }

            return@withContext ParamDocumentationDTO(
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

    companion object {
        const val DOC_BASE_URL = "https://docs.kernel.org/admin-guide/sysctl/"
    }
}

internal fun Document.documentationElements(htmlElementId: String): Elements =
    select("section#$htmlElementId > :not(h2)")

/**
 * Optimizes HTML documentation for display in Compose's HTML text renderer.
 */
internal fun String.optimizedDocumentationHtml(): String =
    Jsoup.parseBodyFragment(trimIndent()).apply {
        select("div.highlight-none, div.highlight").unwrap()
        select("pre span:empty").remove()
    }.body().html()
        .replace(Regex("<dt\\b[^>]*>"), "<p>")
        .replace("</dt>", "</p>")
        .replace(
            Regex("<dd\\b[^>]*>\\s*<p\\b[^>]*>"),
            "<p>&nbsp;&nbsp;&nbsp;&nbsp;"
        )
        .replace(Regex("</p>\\s*</dd>"), "</p>")
        .replace(Regex("<pre\\b[^>]*>(.*?)</pre>", RegexOption.DOT_MATCHES_ALL)) { match ->
            "<br><font face=\"monospace\" color=\"#222\"><b><span style=\"background-color: #DCE9F5\">${match.groupValues[1].trim().preservePreformattedWhitespace()}</span></b></font><br>"
        }
        .replace(
            "<code ", // Handles <code ATTRIBUTES...>
            "<font face=\"monospace\" color=\"#222\"><b><span style=\"background-color: #DCDCF5\" "
        )
        .replace(
            "<code>", // Handles <code NO ATTRIBUTES>
            "<font face=\"monospace\" color=\"#222\"><b><span style=\"background-color: #DCDCF5\">"
        )
        .replace("</code>", "</span></b></font>") // Universal closer for the above
        .removeSuffix("<br />")

private fun String.preservePreformattedWhitespace(): String {
    val result = StringBuilder()
    var inTag = false
    var index = 0

    while (index < length) {
        val character = this[index]
        when {
            character == '<' -> {
                inTag = true
                result.append(character)
            }
            character == '>' -> {
                inTag = false
                result.append(character)
            }
            !inTag && character == '\r' -> {
                if (getOrNull(index + 1) == '\n') index++
                result.append("<br>")
            }
            !inTag && character == '\n' -> result.append("<br>")
            !inTag && character == '\t' -> result.append("&nbsp;&nbsp;&nbsp;&nbsp;")
            !inTag && character == ' ' -> result.append("&nbsp;")
            else -> result.append(character)
        }
        index++
    }
    return result.toString()
}

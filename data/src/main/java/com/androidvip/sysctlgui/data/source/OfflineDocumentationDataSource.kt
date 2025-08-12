package com.androidvip.sysctlgui.data.source

import android.annotation.SuppressLint
import android.content.Context
import com.androidvip.sysctlgui.data.R
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/**
 * Fetches documentation from offline sources.
 *
 * It first tries to find a string resource matching the parameter name.
 * If not found, it attempts to extract documentation from raw text files
 * bundled with the application, categorized by the parameter's path.
 *
 * @property context The application context, used to access resources.
 * @property coroutineContext The coroutine context on which to perform operations.
 */
class OfflineDocumentationDataSource(
    private val context: Context,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : DocumentationDataSource {

    /**
     * Retrieves documentation for a given kernel parameter.
     *
     * This function attempts to find documentation in the following order:
     * 1. **String Resource:** Checks for a string resource matching the parameter's name (normalized by replacing hyphens with underscores).
     * 2. **Raw Text File:** If no string resource is found, it tries to locate documentation within a raw text file based on the parameter's path.
     *    - The path is expected to be in the format `/proc/sys/category/...`.
     *    - The "category" segment determines which raw file to read (e.g., `abi.txt`, `fs.txt`).
     *    - Inside the raw file, it searches for a section matching the parameter's name, delimited by "====" lines.
     *
     * @param param The [KernelParam] for which to retrieve documentation.
     * @return A [String] containing the documentation if found, or `null` otherwise.
     */
    @SuppressLint("DiscouragedApi") // Resource name is determined dynamically from name.
    override suspend fun getDocumentation(
        param: KernelParam)
    : ParamDocumentation? = withContext(coroutineContext) {
        val paramName = param.lastNameSegment
        val resources = context.resources

        val normalizedResourceName = paramName.replace("-", "_")
        val resId = resources.getIdentifier(
            normalizedResourceName,
            "string",
            context.packageName
        )
        val stringRes = runCatching { context.getString(resId) }.getOrNull()

        // Prefer the documented string resource
        if (stringRes != null) return@withContext ParamDocumentation(
            title = param.name,
            documentationText = stringRes
        )

        // Assuming path is like /proc/sys/category/further/path
        val pathSegments = param.path.trim('/').split('/')
        if (pathSegments.size < MIN_PATH_SEGMENTS_FOR_CATEGORY) return@withContext null

        // Validate fixed parts like "proc" and "sys"
        if (pathSegments.getOrNull(0) != "proc" || pathSegments.getOrNull(1) != "sys") {
            // We did our best
            return@withContext null
        }

        // Index 2 after splitting by '/' and removing leading '/'
        val category = pathSegments.getOrNull(2)
        val rawInputStream: InputStream? = when (category) {
            "abi" -> resources.openRawResource(R.raw.abi)
            "fs" -> resources.openRawResource(R.raw.fs)
            "kernel" -> resources.openRawResource(R.raw.kernel)
            "net" -> resources.openRawResource(R.raw.net)
            "vm" -> resources.openRawResource(R.raw.vm)
            else -> null
        }

        val documentation = rawInputStream?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }
        if (documentation.isNullOrEmpty()) return@withContext null

        /*
        Trying to match:

        ===============

        paramName

        the               <==
        actual            <==
        documentation     <==

        ===============
         */
        val info: String? = runCatching {
            documentation
                .split("=+".toRegex())
                .last { it.contains("$paramName\n") }
                .split("$paramName\n")
                .last()
        }.getOrNull()

        val documentationText = info.takeIf { it.isNullOrEmpty().not() }
        if (documentationText == null) return@withContext null
        return@withContext ParamDocumentation(
            title = param.name,
            documentationText = documentationText
        )
    }

    companion object {
        private const val MIN_PATH_SEGMENTS_FOR_CATEGORY = 4
    }
}

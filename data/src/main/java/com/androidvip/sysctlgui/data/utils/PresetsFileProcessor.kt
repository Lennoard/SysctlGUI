package com.androidvip.sysctlgui.data.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.androidvip.sysctlgui.domain.exceptions.InvalidFileException
import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.utils.isValidSysctlOutputLine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class PresetsFileProcessor(
    private val contentResolver: ContentResolver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getKernelParamsFromUri(
        uri: Uri
    ): List<KernelParam> = withContext(ioDispatcher) {
        checkFileType(uri)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val lines = inputStream.bufferedReader().readLines()
            val params = lines.mapNotNull { line ->
                if (line.isValidSysctlOutputLine()) {
                    runCatching {
                        KernelParam.createFromName(
                            name = line.substringBefore('=').trim(),
                            value = line.substringAfter('=').trim(),
                            isFavorite = true
                        )
                    }.getOrNull()
                } else {
                    Log.w("PresetsFileProcessor", "Invalid line: $line")
                    null
                }
            }

            if (params.isEmpty()) {
                throw NoParameterFoundException()
            }

            params
        } ?: throw IOException("Failed to open input stream for URI: $uri")
    }

    suspend fun backupParamsToUri(
        uri: Uri,
        params: List<KernelParam>
    ) = withContext(ioDispatcher) {
        val fileContent = params.joinToString("\n") { "${it.name}=${it.value}" }

        contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.bufferedWriter().use { writer ->
                writer.write(fileContent)
                writer.flush()
            }
        } ?: throw IOException("Failed to open output stream for URI: $uri")
    }


    private suspend fun checkFileType(uri: Uri) = withContext(ioDispatcher) {
        val mimeType = contentResolver.getType(uri)
        if (mimeType != null && mimeType.startsWith("text/")) {
            return@withContext // It's likely a text file, we're good.
        }

        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
        val allowedExtensions = listOf("conf", "cfg", "config", "ini", "txt")

        if (fileExtension in allowedExtensions) return@withContext

        throw InvalidFileException("Unsupported file type. MIME type: $mimeType.")
    }
}

package com.androidvip.sysctlgui.data.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.utils.isValidSysctlLine
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
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val lines = inputStream.bufferedReader().readLines()
            lines.mapNotNull { line ->
                if (line.isValidSysctlLine()) {
                    runCatching {
                        KernelParam.Companion.createFromName(
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
}
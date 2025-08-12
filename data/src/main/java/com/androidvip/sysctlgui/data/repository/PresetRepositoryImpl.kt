package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.exceptions.EmptyFileException
import com.androidvip.sysctlgui.domain.exceptions.MalformedLineException
import com.androidvip.sysctlgui.domain.repository.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

class PresetRepositoryImpl(
    private val ioCoroutineContext: CoroutineContext = Dispatchers.IO
) : PresetRepository {
    override suspend fun readPreset(
        stream: InputStream
    ): List<KernelParam> = withContext(ioCoroutineContext) {
        if (stream.available() == 0) throw EmptyFileException()

        return@withContext stream.bufferedReader().use { reader ->
            reader.lineSequence()
                .filter { it.validConfLine() }
                .map { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val name = parts[0].trim()
                        val value = parts[1].trim()
                        runCatching {
                            KernelParam.createFromName(name = name, value = value)
                        }.getOrElse {
                            throw MalformedLineException("Invalid format for line: $line", it)
                        }
                    } else {
                        throw MalformedLineException("Line doesn't contain '=' separator: $line")
                    }
                }.toList()
        }
    }

    override suspend fun exportToPreset(params: List<KernelParam>, fileDescriptor: FileDescriptor) {
        val content = params.joinToString(separator = "\n") { param ->
            "${param.name}=${param.value}"
        }

        writeContentToFileDescriptor(fileDescriptor, content)
    }

    override suspend fun backupParams(params: List<KernelParam>, fileDescriptor: FileDescriptor) {
        val content = Json.encodeToString(params)
        writeContentToFileDescriptor(fileDescriptor, content)
    }

    private suspend fun writeContentToFileDescriptor(
        fileDescriptor: FileDescriptor,
        content: String
    ) = withContext(ioCoroutineContext) {
        FileOutputStream(fileDescriptor).use { fileOutputStream ->
            fileOutputStream.writer(Charsets.UTF_8).buffered().use { writer ->
                writer.write(content)
            }
        }
    }
}

private fun String.validConfLine(): Boolean {
    return !startsWith("#") && !startsWith(";") && isNotEmpty()
}

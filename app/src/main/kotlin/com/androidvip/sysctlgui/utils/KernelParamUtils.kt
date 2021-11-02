package com.androidvip.sysctlgui.utils

import android.content.Context
import android.net.Uri
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.readLines
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.lang.reflect.Type

// TODO: move to repository
object KernelParamUtils {

    suspend fun writeParamsToUri(
        context: Context,
        params: List<DomainKernelParam>,
        uri: Uri
    ) = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            context.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fileOutputStream ->
                    fileOutputStream.write(Gson().toJson(params).toByteArray())
                }
            }
            true
        }.getOrElse {
            it.printStackTrace()
            false
        }
    }

    fun getParamsFromJsonUri(context: Context, uri: Uri): List<DomainKernelParam>? {
        val sb = StringBuilder()
        uri.readLines(context) { sb.append(it) }

        val type: Type = object : TypeToken<List<DomainKernelParam>>() {}.type
        return Gson().fromJson(sb.toString(), type)
    }

    fun getParamsFromConfUri(context: Context, uri: Uri): List<DomainKernelParam> {
        val readParams = mutableListOf<DomainKernelParam>()

        var cont = 0
        uri.readLines(context) { line ->
            if (!line.startsWith("#") && !line.startsWith(";") && line.isNotEmpty()) {
                runCatching {
                    readParams.add(
                        DomainKernelParam(
                            id = ++cont,
                            name = line.split("=").first().trim(),
                            value = line.split("=")[1].trim()
                        ).apply {
                            setPathFromName(this.name)
                        }
                    )
                }
            }
        }

        return readParams
    }
}

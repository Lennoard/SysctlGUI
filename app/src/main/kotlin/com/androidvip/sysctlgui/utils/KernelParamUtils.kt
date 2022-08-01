package com.androidvip.sysctlgui.utils

import android.content.Context
import android.net.Uri
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

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

}

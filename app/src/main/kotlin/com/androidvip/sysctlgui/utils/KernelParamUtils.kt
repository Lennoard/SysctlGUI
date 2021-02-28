package com.androidvip.sysctlgui.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.readLines
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.lang.reflect.Type

object KernelParamUtils {

    suspend fun writeParamsToUri(
        context: Context,
        params: List<KernelParam>,
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

    fun getParamsFromJsonUri(context: Context, uri: Uri): MutableList<KernelParam>? {
        val sb = StringBuilder()
        uri.readLines(context) { sb.append(it) }

        val type: Type = object : TypeToken<List<KernelParam>>() {}.type
        return Gson().fromJson(sb.toString(), type)
    }

    fun getParamsFromConfUri(context: Context, uri: Uri): MutableList<KernelParam> {
        val readParams = mutableListOf<KernelParam>()

        var cont = 0
        uri.readLines(context) { line ->
            if (!line.startsWith("#") && !line.startsWith(";") && line.isNotEmpty()) {
                runCatching {
                    readParams.add(KernelParam(
                        id = ++cont,
                        name = line.split("=").first().trim(),
                        value = line.split("=")[1].trim()
                    ).apply {
                        setPathFromName(this.name)
                    })
                }
            }
        }

        return readParams
    }

    suspend fun commitChanges(kernelParam: KernelParam, prefs: SharedPreferences): String {
        val prefix = if (prefs.getBoolean(Prefs.USE_BUSYBOX, false)) "busybox " else ""
        val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
            "sysctl" -> "${prefix}sysctl -w ${kernelParam.name}=${kernelParam.value}"
            "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
            else -> "busybox sysctl -w ${kernelParam.name}=${kernelParam.value}"
        }

        return RootUtils.executeWithOutput(command, "error")
    }
}

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

class KernelParamUtils(val context: Context) {
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val paramPrefs by lazy {
        Prefs(context)
    }

    fun exportParamsToUri(uri: Uri): Boolean = runCatching {
        context.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { fileOutputStream ->
                fileOutputStream.write(Gson().toJson(paramPrefs.getUserParamsSet()).toByteArray())
            }
        }
        true
    }.getOrElse {
        it.printStackTrace()
        false
    }

    fun getParamsFromJsonUri(uri: Uri): MutableList<KernelParam>? {
        val sb = StringBuilder()
        uri.readLines(context) { sb.append(it) }

        val type: Type = object : TypeToken<List<KernelParam>>() {}.type
        return Gson().fromJson(sb.toString(), type)
    }

    fun getParamsFromConfUri(uri: Uri): MutableList<KernelParam>? {
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

    suspend fun commitChanges(kernelParam: KernelParam) = withContext(Dispatchers.Default) {
        val prefix = if (prefs.getBoolean(Prefs.USE_BUSYBOX, false)) "busybox " else ""
        val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
            "sysctl" -> "${prefix}sysctl -w ${kernelParam.name}=${kernelParam.value}"
            "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
            else -> "busybox sysctl -w ${kernelParam.name}=${kernelParam.value}"
        }

        RootUtils.executeWithOutput(command, "error")
    }

    /**
     * Suspend function to apply kernel parameters. Should be called with [Dispatchers.Default]
     * as it will swap dispatchers according to the result/operation
     *
     * @param kernelParam: parameter to apply
     * @param kernelParamApply: callback interface
     * @param customApply whether or not to delegate this application of parameters
     */
    suspend fun applyParam(
        kernelParam: KernelParam,
        customApply: Boolean,
        kernelParamApply: KernelParamApply
    ) = withContext(Dispatchers.Default) {

        val newValue = kernelParam.value
        val commitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")

        if (!prefs.getBoolean(Prefs.ALLOW_BLANK, false) && newValue.isEmpty()) {
            // Probably only used to show errors anyway.
            // If we need logging to a file, we can log it here before onEmptyValue()
            withContext(Dispatchers.Main) { kernelParamApply.onEmptyValue() }
        } else {
            if (customApply) {
                kernelParamApply.onCustomApply(kernelParam) // Keeping the current dispatcher
            } else {
                val result = commitChanges(kernelParam) // IO suspend
                var success = true
                val feedback = if (commitMode == "sysctl") {
                    if (result == "error" || !result.contains(kernelParam.name)) {
                        success = false
                        context.getString(R.string.failed)
                    } else {
                        result
                    }
                } else {
                    if (result == "error") {
                        success = false
                        context.getString(R.string.failed)
                    } else {
                        context.getString(R.string.done)
                    }
                }

                // Fire onSuccess and onFeedBack on Main thread
                withContext(Dispatchers.Main) {
                    if (success) {
                        paramPrefs.putParam(kernelParam)
                        kernelParamApply.onSuccess()
                    }
                    kernelParamApply.onFeedBack(feedback)
                }
            }
        }
    }

    interface KernelParamApply {
        fun onEmptyValue()
        fun onFeedBack(feedback: String)
        fun onSuccess()
        suspend fun onCustomApply(kernelParam: KernelParam)
    }
}

package com.androidvip.sysctlgui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type

class KernelParamUtils(val context: Context) {
    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    fun exportParamsToUri(uri: Uri): Boolean {

        return try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fileOutputStream ->
                    fileOutputStream.write(Gson().toJson(Prefs.getUserParamsSet(context)).toByteArray())
                }
            }
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun getParamsFromJsonUri(uri: Uri): MutableList<KernelParameter>? {
        val sb = StringBuilder()
        uri.readLines(context) { sb.append(it) }

        val type: Type = object : TypeToken<List<KernelParameter>>() {}.type
        return Gson().fromJson(sb.toString(), type)
    }

    fun getParamsFromConfUri(uri: Uri): MutableList<KernelParameter>? {
        val readParams = mutableListOf<KernelParameter>()

        uri.readLines(context) { line ->
            if (!line.startsWith("#") and !line.startsWith(";") and line.isNotEmpty()) {
                runCatching {
                    readParams.add(KernelParameter().apply {
                        name = line.split("=").first().trim()
                        value = line.split("=")[1].trim()
                        setPathFromName(this.name)
                    })
                }
            }
        }

        return readParams
    }

    suspend fun commitChanges(kernelParam: KernelParameter) = withContext(Dispatchers.Default) {
        val commandPrefix = if (prefs.getBoolean(Prefs.USE_BUSYBOX, false)) "busybox " else ""
        val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
            "sysctl" -> "${commandPrefix}sysctl -w ${kernelParam.name}=${kernelParam.value}"
            "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
            else -> "busybox sysctl -w ${kernelParam.name}=${kernelParam.value}"
        }

        RootUtils.executeWithOutput(command, "error")
    }

    /**
     * Suspend function to apply kernel parameters. Should be called with [Dispatchers.Default]
     * as it will swap dispatchers according to the result/operation
     *
     * @param kernelParameter: parameter to apply
     * @param kernelParamApply: callback interface
     * @param customApply whether or not to delegate this application of parameters
     */
    suspend fun applyParam(
        kernelParameter: KernelParameter,
        customApply: Boolean,
        kernelParamApply: KernelParamApply
    ) = withContext(Dispatchers.Default) {

        val newValue = kernelParameter.value
        val commitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")

        if (!prefs.getBoolean(Prefs.ALLOW_BLANK, false) && newValue.isEmpty()) {
            // Probably only used to show errors anyway.
            // If we need logging to a file, we can log it here before onEmptyValue()
            withContext(Dispatchers.Main) { kernelParamApply.onEmptyValue() }
        } else {
            if (customApply) {
                kernelParamApply.onCustomApply(kernelParameter) // Keeping the current dispatcher
            } else {
                val result = commitChanges(kernelParameter) // IO suspend
                var success = true
                val feedback = if (commitMode == "sysctl") {
                    if (result == "error" || !result.contains(kernelParameter.name)) {
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
                        Prefs.putParam(kernelParameter, context)
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
        suspend fun onCustomApply(kernelParam: KernelParameter)
    }
}

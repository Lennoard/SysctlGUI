package com.androidvip.sysctlgui

import android.content.Context
import com.androidvip.sysctlgui.Prefs.containsParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

object Prefs {
    private const val USER_PARAMS_FILENAME = "user-params.json"
    const val LIST_FOLDERS_FIRST = "list_folders_first"
    const val GUESS_INPUT_TYPE = "guess_input_type"
    const val COMMIT_MODE = "commit_mode"
    const val ALLOW_BLANK = "allow_blank_values"
    const val USE_BUSYBOX = "use_busybox"
    const val RUN_ON_START_UP = "run_on_start_up"
    const val START_UP_DELAY = "startup_delay"

    fun getUserParamsSet(context: Context?): MutableList<KernelParameter> {
        if (context == null) return mutableListOf()

        val paramsFile = File(context.filesDir, USER_PARAMS_FILENAME)
        if (!paramsFile.exists()) return mutableListOf()

        val type: Type = object : TypeToken<List<KernelParameter>>(){}.type
        return Gson().fromJson(paramsFile.readText(), type)
    }

    fun putParam(param: KernelParameter, context: Context?): Boolean {
        if (context == null) return false

        return try {
            val list = getUserParamsSet(context).apply {
                if (this.containsParam(param)) {
                    val index = indexOf(param)
                    if (index >= 0) this[index] = param
                } else {
                    this.add(param)
                }
            }

            val paramsFile = File(context.filesDir, USER_PARAMS_FILENAME)
            paramsFile.writeText(Gson().toJson(list))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeParam(param: KernelParameter, context: Context?): Boolean {
        if (context == null) return false

        return try {
            val list = getUserParamsSet(context)
            if (list.containsParam(param)) {
                val index = list.indexOf(param)
                list.removeAt(index)

                val paramsFile = File(context.filesDir, USER_PARAMS_FILENAME)
                paramsFile.writeText(Gson().toJson(list))
                true
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun putParams(params: MutableList<KernelParameter>, context: Context?) : Boolean {
        return params.map { kernelParameter: KernelParameter ->
            putParam(kernelParameter, context)
        }.contains(false).not()
    }

    fun removeAllParams(context: Context?): MutableList<KernelParameter> {
        val oldParams = getUserParamsSet(context)
        try {
            val paramFile = File(context?.filesDir, USER_PARAMS_FILENAME)
            paramFile.writeText("[]")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return oldParams
    }

    private fun List<KernelParameter>.containsParam(param: KernelParameter): Boolean {
        for (p in this) if (p.name == param.name) return true
        return false
    }
}

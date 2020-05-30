package com.androidvip.sysctlgui.prefs.base

import android.app.Presentation
import android.content.Context
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.prefs.Prefs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

abstract class BasePrefs(val context: Context?, val fileName: String) {

    fun getUserParamsSet(): MutableList<KernelParameter> {
        if (context == null) return mutableListOf()

        val paramsFile = File(context.filesDir, fileName)
        if (!paramsFile.exists()) return mutableListOf()

        val type: Type = object : TypeToken<List<KernelParameter>>() {}.type
        return Gson().fromJson(paramsFile.readText(), type)
    }

    fun putParam(param: KernelParameter): Boolean {
        if (context == null) return false

        return try {
            val list = getUserParamsSet().apply {
                if (this.containsParam(param)) {
                    val index = indexOf(param)
                    if (index >= 0) this[index] = param
                } else {
                    this.add(param)
                }
            }

            val paramsFile = File(context.filesDir, fileName)
            paramsFile.writeText(Gson().toJson(list))
            changeListener()?.onChanged()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeParam(param: KernelParameter): Boolean {
        if (context == null) return false

        return try {
            val list = getUserParamsSet()
            if (list.containsParam(param)) {
                val index = list.indexOf(param)
                list.removeAt(index)

                val paramsFile = File(context.filesDir, fileName)
                paramsFile.writeText(Gson().toJson(list))
                changeListener()?.onChanged()
                true
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun putParams(params: MutableList<KernelParameter>): Boolean {
        return params.map { kernelParameter: KernelParameter ->
            putParam(kernelParameter)
        }.contains(false).not().also {
            changeListener()?.onChanged()
        }
    }

    fun removeAllParams(): MutableList<KernelParameter> {
        val oldParams = getUserParamsSet()
        try {
            val paramFile = File(context?.filesDir, fileName)
            paramFile.writeText("[]")
            changeListener()?.onChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return oldParams
    }

    fun paramExists(param: KernelParameter, params: List<KernelParameter>): Boolean {
        return params.containsParam(param)
    }

    abstract fun changeListener() : ChangeListener?

    private fun List<KernelParameter>.containsParam(param: KernelParameter): Boolean {
        for (p in this) if (p.name == param.name) return true
        return false
    }

    interface ChangeListener {
        fun onChanged()
    }
}

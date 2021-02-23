package com.androidvip.sysctlgui.prefs.base

import android.content.Context
import com.androidvip.sysctlgui.data.models.KernelParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

abstract class BasePrefs(val context: Context?, val fileName: String) {

    fun getUserParamsSet(): MutableList<KernelParam> {
        if (context == null) return mutableListOf()

        val paramsFile = File(context.filesDir, fileName)
        if (!paramsFile.exists()) return mutableListOf()

        val type: Type = object : TypeToken<List<KernelParam>>() {}.type
        return Gson().fromJson(paramsFile.readText(), type)
    }

    fun putParam(param: KernelParam): Boolean {
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

    fun removeParam(param: KernelParam): Boolean {
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

    fun putParams(params: MutableList<KernelParam>): Boolean {
        return params.map { kernelParameter: KernelParam ->
            putParam(kernelParameter)
        }.contains(false).not().also {
            changeListener()?.onChanged()
        }
    }

    fun removeAllParams(): MutableList<KernelParam> {
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

    fun paramExists(param: KernelParam, params: List<KernelParam>): Boolean {
        return params.containsParam(param)
    }

    abstract fun changeListener() : ChangeListener?

    private fun List<KernelParam>.containsParam(param: KernelParam): Boolean {
        for (p in this) if (p.name == param.name) return true
        return false
    }

    interface ChangeListener {
        fun onChanged()
    }
}

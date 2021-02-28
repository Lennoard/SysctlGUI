package com.androidvip.sysctlgui.data.repository

import android.content.Context
import com.androidvip.sysctlgui.data.models.KernelParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class JsonRepository(private val context: Context?, private val fileName: String) {

    fun getUserParamsSet(): MutableList<KernelParam> {
        if (context == null) return mutableListOf()

        val paramsFile = File(context.filesDir, fileName)
        if (!paramsFile.exists()) return mutableListOf()

        val type: Type = object : TypeToken<List<KernelParam>>() {}.type
        return Gson().fromJson(paramsFile.readText(), type)
    }

    @Deprecated("User params are no longer used this way, use room database instead")
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
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Deprecated("User params are no longer used this way, use room database instead")
    fun removeParam(param: KernelParam): Boolean {
        if (context == null) return false

        return try {
            val list = getUserParamsSet()
            if (list.containsParam(param)) {
                val index = list.indexOf(param)
                list.removeAt(index)

                val paramsFile = File(context.filesDir, fileName)
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

    @Deprecated("User params are no longer used this way, use room database instead")
    fun putParams(params: MutableList<KernelParam>): Boolean {
        return params.map { kernelParameter: KernelParam ->
            putParam(kernelParameter)
        }.contains(false).not()
    }

    @Deprecated("User params are no longer used this way, use room database instead")
    fun removeAllParams(): MutableList<KernelParam> {
        val oldParams = getUserParamsSet()
        try {
            val paramFile = File(context?.filesDir, fileName)
            paramFile.writeText("[]")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return oldParams
    }

    @Deprecated("User params are no longer used this way, use room database instead")
    fun paramExists(param: KernelParam, params: List<KernelParam>): Boolean {
        return params.containsParam(param)
    }

    private fun List<KernelParam>.containsParam(param: KernelParam): Boolean {
        for (p in this) if (p.name == param.name) return true
        return false
    }
}


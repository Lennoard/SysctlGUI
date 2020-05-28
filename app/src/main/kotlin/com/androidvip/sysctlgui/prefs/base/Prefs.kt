package com.androidvip.sysctlgui.prefs.base

import android.content.Context
import com.androidvip.sysctlgui.KernelParameter

interface Prefs {
    fun getUserParamsSet(context: Context?): MutableList<KernelParameter>
    fun putParam(param: KernelParameter, context: Context?): Boolean
    fun removeParam(param: KernelParameter, context: Context?): Boolean
    fun putParams(params: MutableList<KernelParameter>, context: Context?) : Boolean
    fun removeAllParams(context: Context?): MutableList<KernelParameter>
}
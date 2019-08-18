package com.androidvip.sysctlgui

import java.io.Serializable

data class KernelParameter(var path: String = "", var param: String = "", var value: String = "") : Serializable {

    fun setParamFromPath(path: String) {
        if (path.trim().isEmpty() || !path.startsWith("/proc/sys/")) return
        if (path.contains(".")) return

        this.param =  path.removeSuffix("/").removePrefix("/proc/sys/").replace("/", ".")
    }

    fun setPathFromParam(kernelParam: String) {
        if (kernelParam.trim().isEmpty() || kernelParam.contains("/")) return
        if (kernelParam.startsWith(".") || kernelParam.endsWith(".")) return

        this.path = "/proc/sys/${kernelParam.replace(".", "/")}"
    }

    fun hasValidPath() : Boolean {
        if (path.trim().isEmpty() || !path.startsWith("/proc/sys/")) return false
        if (path.contains(".")) return false

        return true
    }

    fun hasValidParam() : Boolean {
        if (param.trim().isEmpty() || param.contains("/")) return false
        if (param.startsWith(".") || param.endsWith(".")) return false

        return true
    }
}
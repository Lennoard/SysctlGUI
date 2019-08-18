package com.androidvip.sysctlgui

data class KernelParam(var path: String = "", var param: String = "", var value: String = "") {

    fun setPathFromParam(kernelParam: String) {
        if (kernelParam.trim().isEmpty() || kernelParam.contains("/")) return
        if (kernelParam.startsWith(".") || kernelParam.endsWith(".")) return

        this.path = "/proc/sys/${kernelParam.replace(".", "/")}"
    }

    fun setParamFromPath(path: String) {
        if (path.trim().isEmpty() || path.startsWith("/proc/sys/")) return
        if (path.contains(".")) return

        this.param =  path.removeSuffix("/").removePrefix("/proc/sys/").replace("/", ".")
    }
}
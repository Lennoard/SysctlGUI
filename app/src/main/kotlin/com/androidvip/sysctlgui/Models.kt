package com.androidvip.sysctlgui

import java.io.Serializable

data class KernelParameter(var path: String = "", var name: String = "", var value: String = "") : Serializable {

    fun setNameFromPath(path: String) {
        if (path.trim().isEmpty() || !path.startsWith("/proc/sys/")) return
        if (path.contains(".")) return

        this.name =  path.removeSuffix("/").removePrefix("/proc/sys/").replace("/", ".")
    }

    fun setPathFromName(kernelParam: String) {
        if (kernelParam.trim().isEmpty() || kernelParam.contains("/")) return
        if (kernelParam.startsWith(".") || kernelParam.endsWith(".")) return

        this.path = "/proc/sys/${kernelParam.replace(".", "/")}"
    }

    fun hasValidPath() : Boolean {
        if (path.trim().isEmpty() || !path.startsWith("/proc/sys/")) return false
        if (path.contains(".")) return false

        return true
    }

    fun hasValidName() : Boolean {
        if (name.trim().isEmpty() || name.contains("/")) return false
        if (name.startsWith(".") || name.endsWith(".")) return false

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KernelParameter) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        if (name.isEmpty()) setNameFromPath(path)
        return "$name = $value"
    }

}
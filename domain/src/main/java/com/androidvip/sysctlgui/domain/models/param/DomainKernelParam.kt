package com.androidvip.sysctlgui.domain.models.param

import com.androidvip.sysctlgui.domain.Consts

open class DomainKernelParam(
    open var id: Int = 0,
    open var name: String = "",
    open var path: String = "",
    open var value: String = "",
    open var favorite: Boolean = false,
    open var taskerParam: Boolean = false,
    open var taskerList: Int = Consts.LIST_NUMBER_PRIMARY_TASKER
) : KernelParamContract {
    override val shortName: String get() = name.split(".").last()

    override fun setNameFromPath(path: String) {
        if (path.trim().isEmpty() || !path.startsWith(Consts.PROC_SYS)) return
        if (path.contains(".")) return

        name = path.removeSuffix("/")
            .removePrefix(Consts.PROC_SYS)
            .replace("/", ".")
            .removePrefix(".")
    }

    override fun setPathFromName(kernelParam: String) {
        if (kernelParam.trim().isEmpty() || kernelParam.contains("/")) return
        if (kernelParam.startsWith(".") || kernelParam.endsWith(".")) return

        path = "${Consts.PROC_SYS}/${kernelParam.replace(".", "/")}"
    }

    override fun hasValidPath(): Boolean {
        if (path.trim().isEmpty() || !path.startsWith(Consts.PROC_SYS)) return false
        if (path.contains(".")) return false

        return true
    }

    override fun hasValidName(): Boolean {
        if (name.trim().isEmpty() || name.contains("/")) return false
        if (name.startsWith(".") || name.endsWith(".")) return false

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DomainKernelParam
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "$name = $value"
    }
}

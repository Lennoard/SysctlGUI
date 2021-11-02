package com.androidvip.sysctlgui.domain.models.param

interface KernelParamContract {
    val shortName: String

    fun setNameFromPath(path: String)
    fun setPathFromName(kernelParam: String)
    fun hasValidPath(): Boolean
    fun hasValidName(): Boolean
}

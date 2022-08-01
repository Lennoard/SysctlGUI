package com.androidvip.sysctlgui.data.models

import android.os.Parcelable
import com.androidvip.sysctlgui.utils.Consts
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import kotlinx.parcelize.Parcelize

@Parcelize
data class KernelParam(
    override var id: Int = 0,
    override var name: String = "",
    override var path: String = "",
    override var value: String = "",
    override var favorite: Boolean = false,
    override var taskerParam: Boolean = false,
    override var taskerList: Int = Consts.LIST_NUMBER_PRIMARY_TASKER
) : DomainKernelParam(), Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KernelParam
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

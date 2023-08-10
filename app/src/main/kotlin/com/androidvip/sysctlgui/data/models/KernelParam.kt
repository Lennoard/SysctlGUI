package com.androidvip.sysctlgui.data.models

import android.os.Parcelable
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.androidvip.sysctlgui.utils.Consts
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
    override fun toString(): String {
        if (name.isEmpty()) setNameFromPath(path)
        return "$name = $value"
    }
}

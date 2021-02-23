package com.androidvip.sysctlgui.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Entity
@Parcelize
data class KernelParam(
    @PrimaryKey(autoGenerate = true)   var id: Int = 0,
    @ColumnInfo(name = "name")         var name: String = "",
    @ColumnInfo(name = "path")         var path: String = "",
    @ColumnInfo(name = "value")        var value: String = "",
    @ColumnInfo(name = "favorite")     var favorite: Boolean = false,
    @ColumnInfo(name = "tasker_param") var taskerParam: Boolean = false
): Parcelable {

    fun setNameFromPath(path: String) {
        if (path.trim().isEmpty() || !path.startsWith("/proc/sys/")) return
        if (path.contains(".")) return

        name = path.removeSuffix("/")
            .removePrefix("/proc/sys/")
            .replace("/", ".")
    }

    fun setPathFromName(kernelParam: String) {
        if (kernelParam.trim().isEmpty() || kernelParam.contains("/")) return
        if (kernelParam.startsWith(".") || kernelParam.endsWith(".")) return

        path = "/proc/sys/${kernelParam.replace(".", "/")}"
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
        if (javaClass != other?.javaClass) return false

        other as KernelParam

        if (id != other.id) return false
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
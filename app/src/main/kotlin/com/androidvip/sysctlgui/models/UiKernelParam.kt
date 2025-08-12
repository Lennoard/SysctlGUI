package com.androidvip.sysctlgui.models

import android.os.Build
import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.utils.Consts
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.isDirectory

/**
 * Represents a kernel parameter with additional UI-specific properties.
 */
@Stable
@Parcelize
data class UiKernelParam(
    override val name: String = "",
    override val path: String = "",
    override val value: String = "",
    override val isFavorite: Boolean = false,
    override val isTaskerParam: Boolean = false,
    override val taskerList: Int = Consts.LIST_NUMBER_PRIMARY_TASKER
) : KernelParam(name, path, value, isFavorite, isTaskerParam, taskerList), Parcelable {

    /**
     * Lazily determines if the [path] represents a directory.
     * Uses [Paths] for Android O and above, otherwise falls back to [File].
     */
    @IgnoredOnParcel
    val isDirectory by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Paths.get(path).isDirectory()
        } else {
            File(path).isDirectory
        }
    }

    /**
     *  The last segment of the parameter's name or path.
     *
     *  If the parameter represents a directory, this will be the last segment of its [path]
     *  after the last `/`. For example: `/proc/sys/vm/` -> `vm`.
     *
     *  If the parameter represents a file, this will be the last segment of its [name]
     *  after the last `.`. For example: `vm.swappiness` -> `swappiness`.
     *
     *  If there is no `.` in the name, the full [name] is returned.
     */
    override val lastNameSegment: String
        get() = if (isDirectory) {
            path.substringAfterLast('/')
        } else {
            name.substringAfterLast('.', name)
        }
}

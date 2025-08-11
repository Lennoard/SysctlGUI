package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.enums.CommitMode
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for managing kernel parameters.
 */
interface ParamsRepository {
    /**
     * Gets all available kernel parameters at runtime.
     *
     * @param useBusybox whether to use busybox or not.
     * @param userParams optional user params list to be merged with runtime params.
     * @return a [List] of [KernelParam]s.
     */
    fun getRuntimeParams(
        useBusybox: Boolean,
        userParams: List<KernelParam> = emptyList()
    ): Flow<List<KernelParam>>

    /**
     * Gets a kernel parameter value at runtime.
     *
     * @param paramName the name of the parameter to get, in the group.name format:
     *  - **vm.admin_reserve_kbytes (OK ✅)**
     *  - admin_reserve_kbytes (NO ❌)
     *  - vm/admin_reserve_kbytes (NO ❌)
     *  - /proc/sys/vm/admin_reserve_kbytes (NO ❌)
     * @param useBusybox whether to use busybox or not.
     * @return the [KernelParam] or null if not found or an error occurred.
     */
    suspend fun getRuntimeParam(paramName: String, useBusybox: Boolean): KernelParam?

    /**
     * Sets the value of a kernel parameter at runtime.
     * @param param The [KernelParam] object representing the kernel parameter to be set.
     * @param commitMode The commit mode to use when setting the parameter.
     * @param useBusybox Whether to use busybox or not.
     */
    suspend fun setRuntimeParam(
        param: KernelParam,
        commitMode: CommitMode,
        useBusybox: Boolean
    ): String

    /**
     * Reads kernel parameters from a list of files.
     * Each file is expected to contain a single line with the parameter value.
     *
     * @param files A list of [File] objects representing the files to read parameters from.
     * @return A [Flow] emitting a list of [KernelParam] objects.
     */
    fun getParamsFromFiles(files: List<File>): Flow<List<KernelParam>>

    /**
     * Gets a list of kernel parameters from the given [path].
     *
     * @param path The path to search for kernel parameters.
     * @return A list of [KernelParam] objects found in the given path.
     */
    fun getParamsFromPath(path: String): Flow<List<KernelParam>>

    companion object {
        const val DEFAULT_ERROR_MESSAGE = "error"
    }
}

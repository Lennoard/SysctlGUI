package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.KernelParam
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing user-specific kernel parameters.
 */
interface UserRepository {
    /**
     * Retrieves a [Flow] that emits a list of user-configurable kernel parameters.
     * The [Flow] will emit a new list whenever the underlying data changes.
     */
    val userParamsFlow: Flow<List<KernelParam>>

    suspend fun getUserParams(): List<KernelParam>

    suspend fun getParamByName(name: String): KernelParam?

    /**
     * Inserts or updates a user-configurable kernel parameter.
     * If a parameter with the same ID already exists, it will be updated.
     * Otherwise, a new parameter will be inserted.
     *
     * @param param The [KernelParam] to upsert.
     * @return The row ID of the inserted or updated parameter.
     */
    suspend fun upsertUserParam(param: KernelParam): Long

    /**
     * Adds a list of kernel parameters to the list of user-configurable parameters.
     *
     * @param params The list of [KernelParam] objects to be added.
     * @return A list of Long values representing the row IDs of the newly inserted parameters.
     */
    suspend fun upsertUserParams(params: List<KernelParam>): List<Long>

    /**
     * Removes a kernel parameter from the list of user-configurable parameters.
     * @param param The [KernelParam] to be removed.
     * @return The number of rows deleted.
     */
    suspend fun removeUserParam(param: KernelParam): Int

    /**
     * Clears all user-configurable kernel parameters.
     */
    suspend fun clearUserParams()
}

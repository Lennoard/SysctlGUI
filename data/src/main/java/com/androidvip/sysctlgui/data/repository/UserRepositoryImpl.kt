package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.db.ParamDao
import com.androidvip.sysctlgui.data.models.KernelParamDTO
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of [UserRepository] that uses a [ParamDao] to store and retrieve user parameters.
 *
 * @param paramDao The DAO used to interact with the database.
 * @param coroutineContext The coroutine context to use for database operations. Defaults to [Dispatchers.IO].
 */
class UserRepositoryImpl(
    private val paramDao: ParamDao,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : UserRepository {
    override val userParamsFlow: Flow<List<KernelParam>>
        get() = paramDao.getAllAsFlow()

    override suspend fun getUserParams(): List<KernelParam> = withContext(coroutineContext) {
        paramDao.getAll()
    }

    override suspend fun getParamByName(name: String) = withContext(coroutineContext) {
        paramDao.getParamByName(name)
    }

    override suspend fun upsertUserParam(param: KernelParam) = withContext(coroutineContext) {
        paramDao.upsert(KernelParamDTO.fromKernelParam(param))
    }

    override suspend fun upsertUserParams(params: List<KernelParam>) =
        withContext(coroutineContext) {
            paramDao.upsertAll(params.map { KernelParamDTO.fromKernelParam(it) })
        }

    override suspend fun removeUserParam(param: KernelParam) = withContext(coroutineContext) {
        paramDao.delete(KernelParamDTO.fromKernelParam(param))
    }

    override suspend fun clearUserParams() = withContext(coroutineContext) {
        paramDao.clearTable()
    }
}

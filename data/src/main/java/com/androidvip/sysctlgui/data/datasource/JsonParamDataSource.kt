package com.androidvip.sysctlgui.data.datasource

import android.content.Context
import com.androidvip.sysctlgui.utils.Consts
import com.androidvip.sysctlgui.domain.datasource.LocalDataSourceContract
import com.androidvip.sysctlgui.domain.models.DomainKernelParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type

class JsonParamDataSource(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalDataSourceContract<DomainKernelParam> {
    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.add(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun add(param: DomainKernelParam, allowBlank: Boolean) {
        throw UnsupportedOperationException("Adding json params is not supported")
    }

    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.addAll(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun addAll(params: List<DomainKernelParam>, allowBlank: Boolean){
        throw UnsupportedOperationException("Adding json params is not supported")
    }

    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.remove(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun remove(param: DomainKernelParam) {
        throw UnsupportedOperationException("Deleting params is only supported in room database")
    }

    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.edit(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun edit(
        param: DomainKernelParam,
        allowBlank: Boolean
    ) {
        throw UnsupportedOperationException("Updating json params is no longer supported")
    }

    override suspend fun clear() = withContext(dispatcher) {
        arrayOf(
            "favorites-params",
            "user-params",
            "tasker-params-${Consts.LIST_NUMBER_PRIMARY_TASKER}",
            "tasker-params-${Consts.LIST_NUMBER_SECONDARY_TASKER}",
            "tasker-params-${Consts.LIST_NUMBER_FAVORITES}",
            "tasker-params-${Consts.LIST_NUMBER_APPLY_ON_BOOT}"
        ).forEach { fileName ->
            val paramFile = File(context.filesDir, fileName)
            paramFile.writeText("[]")
        }
    }

    override suspend fun getData(): List<DomainKernelParam> = withContext(dispatcher) {
        val gson = Gson()
        val params = mutableListOf<DomainKernelParam>()

        arrayOf(
            "favorites-params",
            "user-params",
            "tasker-params-${Consts.LIST_NUMBER_PRIMARY_TASKER}",
            "tasker-params-${Consts.LIST_NUMBER_SECONDARY_TASKER}",
            "tasker-params-${Consts.LIST_NUMBER_FAVORITES}",
            "tasker-params-${Consts.LIST_NUMBER_APPLY_ON_BOOT}"
        ).forEach { fileName ->
            val paramsFile = File(context.filesDir, "$fileName.json")
            if (!paramsFile.exists()) return@forEach

            val type: Type = object : TypeToken<List<DomainKernelParam>>() {}.type
            params.addAll(gson.fromJson(paramsFile.readText(), type))
        }

        return@withContext params.distinct()
    }
}

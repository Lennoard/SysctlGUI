package com.androidvip.sysctlgui.data.datasource

import android.content.Context
import com.androidvip.sysctlgui.domain.Consts
import com.androidvip.sysctlgui.domain.datasource.LocalDataSourceContract
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class JsonParamDataSource(private val context: Context) : LocalDataSourceContract<DomainKernelParam> {
    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.add(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun add(
        param: DomainKernelParam,
        allowBlank: Boolean
    ): Result<Unit> = runCatching {
        throw UnsupportedOperationException("Adding json params is not supported")
    }

    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.addAll(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun addAll(
        params: List<DomainKernelParam>,
        allowBlank: Boolean
    ): Result<Unit> {
        throw UnsupportedOperationException("Adding json params is not supported")
    }

    @Deprecated(
        "JSON database is no longer updated.",
        replaceWith = ReplaceWith("roomParamDatasource.remove(param)"),
        level = DeprecationLevel.ERROR
    )
    override suspend fun remove(param: DomainKernelParam): Result<Unit> = runCatching {
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
    ): Result<Unit> = runCatching {
        throw UnsupportedOperationException("Updating json params is no longer supported")
    }

    override suspend fun clear(): Result<Unit> = runCatching {
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

    override suspend fun getData(): Result<List<DomainKernelParam>> = runCatching {
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

        return@runCatching params.distinct()
    }
}

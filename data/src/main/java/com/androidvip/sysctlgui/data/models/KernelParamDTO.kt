package com.androidvip.sysctlgui.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidvip.sysctlgui.data.utils.KernelParamSerializer
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.utils.Consts
import kotlinx.serialization.Serializable

@Entity(tableName = PARAMS_TABLE_NAME)
@Serializable(with = KernelParamSerializer::class)
data class KernelParamDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    override val name: String = "",
    @ColumnInfo(name = "path")
    override val path: String = "",
    @ColumnInfo(name = "value")
    override val value: String = "",
    @ColumnInfo(name = "favorite")
    override val isFavorite: Boolean = false,
    @ColumnInfo(name = "tasker_param")
    override val isTaskerParam: Boolean = false,
    @ColumnInfo(name = "tasker_list")
    override val taskerList: Int = Consts.LIST_NUMBER_PRIMARY_TASKER
) : KernelParam(name, path, value, isFavorite, isTaskerParam, taskerList)

internal const val PARAMS_TABLE_NAME = "roomKernelParam"

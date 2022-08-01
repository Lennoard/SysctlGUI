package com.androidvip.sysctlgui.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidvip.sysctlgui.utils.Consts

@Entity
data class RoomKernelParam(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "path")
    var path: String = "",
    @ColumnInfo(name = "value")
    var value: String = "",
    @ColumnInfo(name = "favorite")
    var favorite: Boolean = false,
    @ColumnInfo(name = "tasker_param")
    var taskerParam: Boolean = false,
    @ColumnInfo(name = "tasker_list")
    var taskerList: Int = Consts.LIST_NUMBER_PRIMARY_TASKER
)

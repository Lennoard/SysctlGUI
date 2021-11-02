package com.androidvip.sysctlgui.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidvip.sysctlgui.data.models.RoomKernelParam

@Database(entities = [RoomKernelParam::class], version = 1, exportSchema = false)
abstract class ParamDatabase : RoomDatabase() {
    abstract fun paramDao(): ParamDao
}

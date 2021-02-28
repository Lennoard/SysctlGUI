package com.androidvip.sysctlgui.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidvip.sysctlgui.data.models.KernelParam

@Database(entities = [KernelParam::class], version = 1, exportSchema = false)
abstract class ParamDatabase : RoomDatabase() {
    abstract fun paramDao(): ParamDao
}

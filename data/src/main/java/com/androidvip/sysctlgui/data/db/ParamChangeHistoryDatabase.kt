package com.androidvip.sysctlgui.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidvip.sysctlgui.data.models.ParamChangeDTO

@Database(entities = [ParamChangeDTO::class], version = 1, exportSchema = false)
abstract class ParamChangeHistoryDatabase : RoomDatabase() {
    abstract fun paramChangeHistoryDao(): ParamChangeHistoryDao
}

package com.androidvip.sysctlgui.data.db

import android.content.Context
import androidx.room.Room

class ParamChangeHistoryDatabaseManager {

    companion object {
        @Volatile
        private var INSTANCE: ParamChangeHistoryDatabase? = null

        fun getInstance(context: Context): ParamChangeHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ParamChangeHistoryDatabase::class.java,
                    "param_change_history"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

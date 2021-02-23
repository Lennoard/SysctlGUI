package com.androidvip.sysctlgui.data

import android.content.Context
import androidx.room.Room

class ParamDatabaseManager {

    companion object {
        @Volatile private var INSTANCE: ParamDatabase? = null

        fun getInstance(context: Context): ParamDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }
        }

        private fun buildDatabase(applicationContext: Context): ParamDatabase {
            return Room.databaseBuilder(
                applicationContext,
                ParamDatabase::class.java, "params"
            ).build()
        }
    }
}
package com.androidvip.sysctlgui.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.androidvip.sysctlgui.data.models.RoomKernelParam

@Dao
interface ParamDao {
    @Query("SELECT * FROM roomKernelParam")
    suspend fun getAll(): List<RoomKernelParam>?

    @Insert
    suspend fun insert(vararg params: RoomKernelParam)

    @Delete
    suspend fun delete(param: RoomKernelParam)

    @Update
    suspend fun update(param: RoomKernelParam)

    @Query("DELETE FROM roomKernelParam")
    suspend fun clearTable()
}

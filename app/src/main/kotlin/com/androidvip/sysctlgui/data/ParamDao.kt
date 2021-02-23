package com.androidvip.sysctlgui.data

import androidx.room.*
import com.androidvip.sysctlgui.data.models.KernelParam

@Dao
interface ParamDao {
    @Query("SELECT * FROM kernelParam")
    suspend fun getAll(): List<KernelParam>?

    @Insert
    suspend fun insertAll(vararg params: KernelParam)

    @Delete
    suspend fun delete(param: KernelParam)

    @Update
    suspend fun update(param: KernelParam)

    @Query("DELETE FROM kernelParam")
    suspend fun clearTable()
}
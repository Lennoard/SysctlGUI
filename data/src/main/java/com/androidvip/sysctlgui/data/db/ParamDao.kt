package com.androidvip.sysctlgui.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.androidvip.sysctlgui.data.models.KernelParamDTO
import com.androidvip.sysctlgui.data.models.PARAMS_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface ParamDao {
    @Query("SELECT * FROM $PARAMS_TABLE_NAME")
    suspend fun getAll(): List<KernelParamDTO>

    @Query("SELECT * FROM $PARAMS_TABLE_NAME")
    fun getAllAsFlow(): Flow<List<KernelParamDTO>>

    @Query("SELECT * FROM $PARAMS_TABLE_NAME WHERE name = :name")
    suspend fun getParamByName(name: String): KernelParamDTO?

    @Upsert
    suspend fun upsert(param: KernelParamDTO): Long

    @Upsert
    suspend fun upsertAll(params: List<KernelParamDTO>): List<Long>

    @Delete
    suspend fun delete(param: KernelParamDTO): Int

    @Query("DELETE FROM $PARAMS_TABLE_NAME")
    suspend fun clearTable()
}

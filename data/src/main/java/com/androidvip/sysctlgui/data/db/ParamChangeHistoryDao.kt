package com.androidvip.sysctlgui.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.androidvip.sysctlgui.data.models.ParamChangeDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface ParamChangeHistoryDao {
    @Query("SELECT * FROM ParamChangeHistory ORDER BY changedAt DESC, id DESC")
    fun observeChanges(): Flow<List<ParamChangeDTO>>

    @Insert
    suspend fun insert(change: ParamChangeDTO)

    @Query("DELETE FROM ParamChangeHistory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ParamChangeHistory")
    suspend fun clear()
}

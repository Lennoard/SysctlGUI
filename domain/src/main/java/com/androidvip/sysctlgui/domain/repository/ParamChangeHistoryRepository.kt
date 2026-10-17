package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.ParamChange
import kotlinx.coroutines.flow.Flow

interface ParamChangeHistoryRepository {
    fun observeChanges(): Flow<List<ParamChange>>

    suspend fun recordChange(change: ParamChange)

    suspend fun deleteChange(id: Long)

    suspend fun clearHistory()
}

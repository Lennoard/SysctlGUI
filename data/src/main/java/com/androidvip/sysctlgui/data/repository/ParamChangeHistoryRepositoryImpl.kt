package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.db.ParamChangeHistoryDao
import com.androidvip.sysctlgui.data.models.ParamChangeDTO
import com.androidvip.sysctlgui.domain.models.ParamChange
import com.androidvip.sysctlgui.domain.repository.ParamChangeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ParamChangeHistoryRepositoryImpl(
    private val dao: ParamChangeHistoryDao
) : ParamChangeHistoryRepository {
    override fun observeChanges(): Flow<List<ParamChange>> =
        dao.observeChanges().map { changes -> changes.map(ParamChangeDTO::toParamChange) }

    override suspend fun recordChange(change: ParamChange) {
        dao.insert(ParamChangeDTO.fromParamChange(change))
    }

    override suspend fun deleteChange(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun clearHistory() {
        dao.clear()
    }
}

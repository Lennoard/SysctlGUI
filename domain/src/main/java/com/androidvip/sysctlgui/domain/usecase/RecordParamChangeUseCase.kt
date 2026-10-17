package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.models.ParamChange
import com.androidvip.sysctlgui.domain.repository.ParamChangeHistoryRepository

class RecordParamChangeUseCase(
    private val repository: ParamChangeHistoryRepository
) {
    suspend operator fun invoke(
        paramName: String,
        path: String,
        previousValue: String,
        newValue: String
    ) {
        repository.recordChange(
            ParamChange(
                paramName = paramName,
                path = path,
                previousValue = previousValue,
                newValue = newValue,
                changedAt = System.currentTimeMillis()
            )
        )
    }
}

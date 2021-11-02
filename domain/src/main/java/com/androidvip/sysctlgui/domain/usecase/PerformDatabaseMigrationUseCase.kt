package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class PerformDatabaseMigrationUseCase(private val repository: ParamsRepository) {
    suspend fun execute(): Result<Unit> {
        return repository.performDatabaseMigration()
    }
}

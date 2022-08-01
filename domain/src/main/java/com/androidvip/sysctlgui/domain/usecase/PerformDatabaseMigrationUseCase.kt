package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.repository.ParamsRepository

class PerformDatabaseMigrationUseCase(private val repository: ParamsRepository) {
    suspend operator fun invoke() {
        return repository.performDatabaseMigration()
    }
}

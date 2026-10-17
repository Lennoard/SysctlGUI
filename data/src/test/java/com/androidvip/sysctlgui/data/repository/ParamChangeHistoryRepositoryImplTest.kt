package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.db.ParamChangeHistoryDao
import com.androidvip.sysctlgui.data.models.ParamChangeDTO
import com.androidvip.sysctlgui.domain.models.ParamChange
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ParamChangeHistoryRepositoryImplTest {
    private val dao: ParamChangeHistoryDao = mockk(relaxed = true)
    private lateinit var repository: ParamChangeHistoryRepositoryImpl

    @Before
    fun setUp() {
        repository = ParamChangeHistoryRepositoryImpl(dao)
    }

    @Test
    fun `observeChanges maps database records to domain records`() = runTest {
        val dto = ParamChangeDTO(
            id = 3,
            paramName = "vm.swappiness",
            path = "/proc/sys/vm/swappiness",
            previousValue = "60",
            newValue = "30",
            changedAt = 123L
        )
        every { dao.observeChanges() } returns flowOf(listOf(dto))

        val changes = repository.observeChanges().first()

        assertEquals(
            listOf(
                ParamChange(
                    id = 3,
                    paramName = "vm.swappiness",
                    path = "/proc/sys/vm/swappiness",
                    previousValue = "60",
                    newValue = "30",
                    changedAt = 123L
                )
            ),
            changes
        )
    }

    @Test
    fun `recordChange inserts its mapped database record`() = runTest {
        val change = ParamChange(
            paramName = "vm.swappiness",
            path = "/proc/sys/vm/swappiness",
            previousValue = "60",
            newValue = "30",
            changedAt = 123L
        )

        repository.recordChange(change)

        coVerify {
            dao.insert(
                ParamChangeDTO(
                    paramName = "vm.swappiness",
                    path = "/proc/sys/vm/swappiness",
                    previousValue = "60",
                    newValue = "30",
                    changedAt = 123L
                )
            )
        }
    }

    @Test
    fun `deleteChange delegates to dao`() = runTest {
        repository.deleteChange(42L)

        coVerify { dao.deleteById(42L) }
    }

    @Test
    fun `clearHistory delegates to dao`() = runTest {
        repository.clearHistory()

        coVerify { dao.clear() }
    }
}

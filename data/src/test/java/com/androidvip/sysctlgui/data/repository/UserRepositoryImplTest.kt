
package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.db.ParamDao
import com.androidvip.sysctlgui.data.models.KernelParamDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private val paramDao: ParamDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        repository = UserRepositoryImpl(paramDao, testDispatcher)
    }

    @Test
    fun `given a new parameter, when upsertUserParam is called, then DAO is called with id 0`() = runTest(testDispatcher) {
        // Given
        val newParam = KernelParamDTO(name = "net.ipv4.ip_forward", value = "1")
        coEvery { paramDao.getParamByName(newParam.name) } returns null

        // When
        repository.upsertUserParam(newParam)

        // Then
        val expectedDto = KernelParamDTO.fromKernelParam(newParam).copy(id = 0)
        coVerify { paramDao.upsert(expectedDto) }
    }

    @Test
    fun `when removeUserParam is called, then DAO delete is called with converted DTO`() = runTest(testDispatcher) {
        // Given
        val paramToRemove = KernelParamDTO(id = 1, name = "net.ipv4.ip_forward", value = "1")

        // When
        repository.removeUserParam(paramToRemove)

        // Then
        val expectedDto = KernelParamDTO.fromKernelParam(paramToRemove)
        coVerify { paramDao.deleteSingle(expectedDto.name,expectedDto.path) }
    }
}

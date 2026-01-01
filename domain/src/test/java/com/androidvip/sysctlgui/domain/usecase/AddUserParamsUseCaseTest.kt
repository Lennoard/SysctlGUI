package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.BlankValueNotAllowedException
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.UserRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class AddUserParamsUseCaseTest {

    private val repository: UserRepository = mockk(relaxed = true)
    private val appPrefs: AppPrefs = mockk()
    private lateinit var useCase: AddUserParamsUseCase

    @Before
    fun setUp() {
        useCase = AddUserParamsUseCase(repository, appPrefs)
    }

    @Test
    fun `given allow blanks is false and params contain blank value, when invoked, then throw exception`() = runTest {
        // Given
        every { appPrefs.allowBlankValues } returns false
        val params = listOf(KernelParam(name = "net.ipv4.ip_forward", value = "", path = ""))

        // Then
        assertThrows(BlankValueNotAllowedException::class.java) {
            // When
            runBlocking { useCase(params) }
        }
        coVerify(exactly = 0) { repository.upsertUserParams(any()) }
    }

    @Test
    fun `given allow blanks is false and params do not contain blank value, when invoked, then upsert params`() = runTest {
        // Given
        every { appPrefs.allowBlankValues } returns false
        val params = listOf(KernelParam(name = "net.ipv4.ip_forward", value = "1", path = ""))

        // When
        useCase(params)

        // Then
        coVerify(exactly = 1) { repository.upsertUserParams(params) }
    }

    @Test
    fun `given allow blanks is true and params contain blank value, when invoked, then upsert params`() = runTest {
        // Given
        every { appPrefs.allowBlankValues } returns true
        val params = listOf(KernelParam(name = "net.ipv4.ip_forward", value = "", path = ""))

        // When
        useCase(params)

        // Then
        coVerify(exactly = 1) { repository.upsertUserParams(params) }
    }
}

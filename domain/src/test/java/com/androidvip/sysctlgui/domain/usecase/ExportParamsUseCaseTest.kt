package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.exceptions.NoParameterFoundException
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.PresetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.FileDescriptor

class ExportParamsUseCaseTest {

    private val repository: PresetRepository = mockk(relaxed = true)
    private val getUserParamsUseCase: GetUserParamsUseCase = mockk(relaxed = true)
    private lateinit var useCase: ExportParamsUseCase

    @Before
    fun setUp() {
        useCase = ExportParamsUseCase(getUserParamsUseCase, repository)
    }

    @Test
    fun `given non-empty user params, when invoked, then export to preset`() = runTest {
        // Given
        val fileDescriptor = FileDescriptor()
        val params = listOf(KernelParam(name = "net.ipv4.ip_forward", value = "1", path = ""))
        coEvery { getUserParamsUseCase() } returns params

        // When
        useCase(fileDescriptor)

        // Then
        coVerify(exactly = 1) { repository.exportToPreset(params, fileDescriptor) }
    }

    @Test(expected = NoParameterFoundException::class)
    fun `given empty params, when invoked, then throw NoParameterFoundException`() = runTest {
        // Given
        val fileDescriptor = FileDescriptor()
        val emptyParams = emptyList<KernelParam>()
        coEvery { getUserParamsUseCase() } returns emptyParams

        // When
        useCase(fileDescriptor)
    }
}

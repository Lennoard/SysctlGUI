package com.androidvip.sysctlgui.domain.usecase

import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.exceptions.ApplyValueException
import com.androidvip.sysctlgui.domain.exceptions.BlankValueNotAllowedException
import com.androidvip.sysctlgui.domain.exceptions.CommitModeException
import com.androidvip.sysctlgui.domain.exceptions.ShellCommandException
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.repository.ParamsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ApplyParamUseCaseTest {

    private val repository: ParamsRepository = mockk()
    private val appPrefs: AppPrefs = mockk()
    private lateinit var useCase: ApplyParamUseCase

    @Before
    fun setUp() {
        useCase = ApplyParamUseCase(repository, appPrefs)
    }

    @Test(expected = BlankValueNotAllowedException::class)
    fun `given blank value not allowed, when invoked with blank value, then throw exception`() =
        runTest {
            // Given
            every { appPrefs.allowBlankValues } returns false
            val param = KernelParam(name = "net.ipv4.ip_forward", value = "", path = "")

            // When
            useCase(param)
        }

    @Test(expected = CommitModeException::class)
    fun `given sysctl mode, when output does not confirm change, then throw exception`() = runTest {
        // Given
        every { appPrefs.commitMode } returns CommitMode.SYSCTL.name
        every { appPrefs.useBusybox } returns false
        val param = KernelParam(name = "net.ipv4.ip_forward", value = "1", path = "")
        coEvery { repository.setRuntimeParam(param, CommitMode.SYSCTL, false) } returns ""

        // When
        useCase(param)
    }

    @Test(expected = CommitModeException::class)
    fun `given echo mode, when output is not empty, then throw exception`() = runTest {
        // Given
        every { appPrefs.commitMode } returns CommitMode.ECHO.name.lowercase()
        every { appPrefs.useBusybox } returns false
        val param = KernelParam(name = "net.ipv4.ip_forward", value = "1", path = "")
        coEvery { repository.setRuntimeParam(param, CommitMode.ECHO, false) } returns "error"

        // When
        useCase(param)
    }

    @Test(expected = ApplyValueException::class)
    fun `when repository throws ShellCommandException, then throw ApplyValueException`() = runTest {
        // Given
        every { appPrefs.commitMode } returns CommitMode.SYSCTL.name.lowercase()
        every { appPrefs.useBusybox } returns false
        val param = KernelParam(name = "net.ipv4.ip_forward", value = "1", path = "")
        coEvery {
            repository.setRuntimeParam(
                param,
                CommitMode.SYSCTL,
                false
            )
        } throws ShellCommandException(
            "error",
            Throwable()
        )

        // When
        useCase(param)
    }

    @Test(expected = ApplyValueException::class)
    fun `when repository throws generic exception, then throw ApplyValueException`() = runTest {
        // Given
        every { appPrefs.commitMode } returns CommitMode.SYSCTL.name.lowercase()
        every { appPrefs.useBusybox } returns false
        val param = KernelParam(name = "net.ipv4.ip_forward", value = "1", path = "")
        coEvery {
            repository.setRuntimeParam(
                param,
                CommitMode.SYSCTL,
                false
            )
        } throws Exception("error")

        // When
        useCase(param)
    }
}

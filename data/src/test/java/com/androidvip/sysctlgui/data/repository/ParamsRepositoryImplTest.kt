package com.androidvip.sysctlgui.data.repository

import android.util.Log
import com.androidvip.sysctlgui.data.utils.RootUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ParamsRepositoryImplTest {

    private lateinit var repository: ParamsRepositoryImpl
    private val rootUtils: RootUtils = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = ParamsRepositoryImpl(rootUtils, testDispatcher)
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @Test
    fun `given valid sysctl output, when getRuntimeParams is called, then return kernel params`() = runTest(testDispatcher) {
        // Given
        val commandOutput = flowOf("net.ipv4.ip_forward = 1", "vm.swappiness = 60")
        coEvery { rootUtils.executeCommandAndStreamOutput(any()) } returns commandOutput

        // When
        val params = repository.getRuntimeParams(useBusybox = false, userParams = emptyList()).first()

        // Then
        assertEquals(2, params.size)
        assertEquals("net.ipv4.ip_forward", params[0].name)
        assertEquals("1", params[0].value)
        assertEquals("vm.swappiness", params[1].name)
        assertEquals("60", params[1].value)
    }

    @Test
    fun `given valid sysctl output, when getRuntimeParam is called, then return kernel param`() = runTest(testDispatcher) {
        // Given
        val paramName = "net.ipv4.ip_forward"
        val commandOutput = flowOf("1")
        coEvery { rootUtils.executeCommandAndStreamOutput(any()) } returns commandOutput

        // When
        val param = repository.getRuntimeParam(paramName, useBusybox = false)

        // Then
        assertNotNull(param)
        assertEquals(paramName, param?.name)
        assertEquals("1", param?.value)
    }

    @Test
    fun `given empty sysctl output, when getRuntimeParam is called, then return null`() = runTest(testDispatcher) {
        // Given
        val paramName = "net.ipv4.ip_forward"
        coEvery { rootUtils.executeCommandAndStreamOutput(any()) } returns flowOf()

        // When
        val param = repository.getRuntimeParam(paramName, useBusybox = false)

        // Then
        assertNull(param)
    }
}

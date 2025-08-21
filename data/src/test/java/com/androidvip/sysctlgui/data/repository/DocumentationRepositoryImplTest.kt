package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.source.DocumentationDataSource
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DocumentationRepositoryImplTest {
    private val offlineDataSource: DocumentationDataSource = mockk(relaxed = true)
    private val onlineDataSource: DocumentationDataSource = mockk(relaxed = true)
    private lateinit var repository: DocumentationRepositoryImpl

    private val testParam = KernelParam(
        name = "vm.swappiness",
        path = "/proc/sys/vm/swappiness",
        value = "100"
    )

    private val expectedDocumentation = ParamDocumentation(
        title = "Swappiness",
        documentationText = "Controls swappiness",
        url = "https://docs.kernel.org/admin-guide/sysctl/vm.html#swappiness"
    )

    @Before
    fun setUp() {
        repository = DocumentationRepositoryImpl(offlineDataSource, onlineDataSource)
    }

    @Test
    fun `getDocumentation when online is true and online source succeeds then return online documentation`() =
        runTest {
            // Given
            coEvery { onlineDataSource.getDocumentation(testParam) } returns expectedDocumentation

            // When
            val result = repository.getDocumentation(testParam, online = true)

            // Then
            coVerify(exactly = 1) { onlineDataSource.getDocumentation(testParam) }
            verify { offlineDataSource wasNot called }

            Assert.assertEquals(expectedDocumentation, result)
        }

    @Test
    fun `getDocumentation when online is true and online source times out then return offline documentation`() = runTest {
        // Given
        coEvery { onlineDataSource.getDocumentation(testParam) } coAnswers {
            delay(DocumentationRepositoryImpl.REQUEST_TIMEOUT_MS + 100)
            null
        }

        coEvery { onlineDataSource.getDocumentation(testParam) } returns null
        coEvery { offlineDataSource.getDocumentation(testParam) } returns expectedDocumentation

        // When
        val result = repository.getDocumentation(testParam, online = true)

        // Then
        coVerify(exactly = 1) { onlineDataSource.getDocumentation(testParam) }
        coVerify(exactly = 1) { offlineDataSource.getDocumentation(testParam) }
        Assert.assertEquals(expectedDocumentation, result)
    }

    @Test
    fun `getDocumentation when online is false then return offline documentation`() = runTest {
        // Given
        coEvery { offlineDataSource.getDocumentation(testParam) } returns expectedDocumentation

        // When
        val result = repository.getDocumentation(testParam, online = false)

        // Then
        coVerify(exactly = 1) { offlineDataSource.getDocumentation(testParam) }
        verify { onlineDataSource wasNot called }
        Assert.assertEquals(expectedDocumentation, result)
    }

    @Test
    fun `getDocumentation when online is true and both sources return null then return null`() = runTest {
        // Given
        coEvery { onlineDataSource.getDocumentation(testParam) } returns null
        coEvery { offlineDataSource.getDocumentation(testParam) } returns null

        // When
        val result = repository.getDocumentation(testParam, online = true)

        // Then
        coVerify(exactly = 1) { onlineDataSource.getDocumentation(testParam) }
        coVerify(exactly = 1) { offlineDataSource.getDocumentation(testParam) }
        Assert.assertNull(result)
    }

    @Test
    fun `getDocumentation when online is false and offline source returns null then return null`() = runTest {
        // Given
        coEvery { offlineDataSource.getDocumentation(testParam) } returns null

        // When
        val result = repository.getDocumentation(testParam, online = false)

        // Then
        coVerify(exactly = 1) { offlineDataSource.getDocumentation(testParam) }
        verify { onlineDataSource wasNot called }
        Assert.assertNull(result)
    }
}

package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.domain.exceptions.EmptyFileException
import com.androidvip.sysctlgui.domain.exceptions.MalformedLineException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class PresetRepositoryImplTest {

    private lateinit var repository: PresetRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = PresetRepositoryImpl(testDispatcher)
    }

    @Test
    fun `given valid preset stream, when readPreset is called, then return kernel params`() = runTest(testDispatcher) {
        // Given
        val presetContent = "net.ipv4.ip_forward=1\nvm.swappiness=60"
        val inputStream = ByteArrayInputStream(presetContent.toByteArray())

        // When
        val params = repository.readPreset(inputStream)

        // Then
        assertEquals(2, params.size)
        assertEquals("net.ipv4.ip_forward", params[0].name)
        assertEquals("1", params[0].value)
        assertEquals("vm.swappiness", params[1].name)
        assertEquals("60", params[1].value)
    }

    @Test(expected = EmptyFileException::class)
    fun `given empty preset stream, when readPreset is called, then throw EmptyFileException`() = runTest(testDispatcher) {
        // Given
        val inputStream = ByteArrayInputStream(byteArrayOf())
        // When
        repository.readPreset(inputStream)
    }

    @Test(expected = MalformedLineException::class)
    fun `given preset with malformed line, when readPreset is called, then throw MalformedLineException`() = runTest(testDispatcher) {
        // Given
        @Language("conf")
        val presetContent = """
            net.ipv4.ip_forward=1
            vm.swappiness
        """.trimIndent()
        val inputStream = ByteArrayInputStream(presetContent.toByteArray())

        // When
        repository.readPreset(inputStream)
    }

    @Test
    fun `given preset with comments, when readPreset is called, then ignore comments`() = runTest(testDispatcher) {
        // Given
        @Language("conf")
        val presetContent = """
            # This is a comment
            net.ipv4.ip_forward=1
            ; another comment
            vm.swappiness=60
        """.trimIndent()
        val inputStream = ByteArrayInputStream(presetContent.toByteArray())

        // When
        val params = repository.readPreset(inputStream)

        // Then
        assertEquals(2, params.size)
        assertEquals("net.ipv4.ip_forward", params[0].name)
        assertEquals("1", params[0].value)
        assertEquals("vm.swappiness", params[1].name)
        assertEquals("60", params[1].value)
    }

    @Test
    fun `given preset with empty lines, when readPreset is called, then ignore empty lines`() = runTest(testDispatcher) {
        // Given
        @Language("conf")
        val presetContent = """
            
            net.ipv4.ip_forward=1
        
            vm.swappiness=60

        """.trimIndent()
        val inputStream = ByteArrayInputStream(presetContent.toByteArray())

        // When
        val params = repository.readPreset(inputStream)

        // Then
        assertEquals(2, params.size)
    }
}

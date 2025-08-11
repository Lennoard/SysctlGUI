package com.androidvip.sysctlgui.domain.repository

import com.androidvip.sysctlgui.domain.models.KernelParam
import java.io.FileDescriptor
import java.io.InputStream


/**
 * Interface defining operations for managing kernel parameter presets.
 * This interface provides methods to read and write kernel parameter presets,
 * allowing users to save and load configurations.
 */
interface PresetRepository {
    /**
     * Reads a preset of kernel parameters from an input stream.
     *
     * This function attempts to determine if the input stream contains JSON or CONF formatted data
     * and parses it accordingly.
     *
     * @param stream The input stream containing the kernel parameter preset.
     * @return A list of [KernelParam] objects parsed from the stream.
     * @throws IllegalArgumentException if the stream format cannot be determined or if parsing fails.
     */
    suspend fun readPreset(stream: InputStream): List<KernelParam>

    /**
     * Exports a list of kernel parameters to a preset file.
     *
     * This function writes the provided kernel parameters to a specified file descriptor,
     * typically for creating a user-defined preset.
     *
     * @param params The list of [KernelParam] objects to export.
     * @param fileDescriptor The `FileDescriptor` of the file to write the parameters to.
     */
    suspend fun exportToPreset(params: List<KernelParam>, fileDescriptor: FileDescriptor)

    /**
     * Backs up a list of kernel parameters.
     *
     * @param params The list of `KernelParam` objects to backup.
     * @param fileDescriptor The `FileDescriptor` of the file to write the backup to.
     */
    suspend fun backupParams(params: List<KernelParam>, fileDescriptor: FileDescriptor)
}

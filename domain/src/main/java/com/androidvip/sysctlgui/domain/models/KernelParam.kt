package com.androidvip.sysctlgui.domain.models

import com.androidvip.sysctlgui.utils.Consts

/**
 * Represents a kernel parameter.
 */
open class KernelParam(
    /**
     * The name of the kernel parameter (e.g., "vm.swappiness")
     */
    open val name: String,

    /**
     * The path of the kernel parameter (e.g., "/proc/sys/vm/swappiness")
     */
    open val path: String,

    /**
     * The value of the kernel parameter (e.g., "60")
     */
    open val value: String,

    /**
     * Indicates whether the parameter is marked as a favorite by the user.
     */
    open val isFavorite: Boolean = false,

    /**
     * Indicates whether the parameter is used in a Tasker profile
     */
    open val isTaskerParam: Boolean = false,

    /**
     * Indicates the Tasker list number (primary or secondary)
     */
    open val taskerList: Int = Consts.LIST_NUMBER_INVALID,
) {

    open val lastNameSegment: String
        get() = name.substringAfterLast('.', name)

    /**
     * The configuration part of the name, excluding the lastNameSegment.
     * For example, for `vm.swappiness`, configName would be `vm`.
     */
    open val groupName: String
        get() = name.substringBeforeLast('.', "")

    /**
     * Checks if the [path] is valid for a kernel parameter.
     */
    fun hasValidPath() = path.isKernelPathValid()

    /**
     * Checks if the [name] is valid for a kernel parameter.
     */
    fun hasValidName() = name.isKernelNameValid()

    companion object {

        /**
         * Creates a new instance with its `path` derived from a given `newName`.
         * Example: If `newName` is "vm.swappiness", the derived path will be "/proc/sys/vm/swappiness".
         *
         * @param name The name to derive the path from. It must be a valid kernel parameter name.
         * @return A new [KernelParam] instance with the derived path.
         * @throws IllegalArgumentException if `newName` is not a valid kernel parameter name.
         */
        fun createFromName(
            name: String,
            value: String,
            isFavorite: Boolean = false
        ): KernelParam {
            require(name.isKernelNameValid()) { "Invalid name: $name" }
            val derivedPath = "${Consts.PROC_SYS}/${name.replace(".", "/")}"
            return KernelParam(name, value, derivedPath, isFavorite)
        }

        /**
         * Creates a [KernelParam] instance from a given path and value.
         * The name is derived from the path.
         * For example, for `/proc/sys/vm/swappiness/`, the derived name will be `vm.swappiness`.
         *
         * @param path The path of the kernel parameter (e.g., "/proc/sys/vm/swappiness").
         *             It must be a valid path as defined by [isKernelPathValid].
         * @param value The value of the kernel parameter (e.g., "60").
         * @return A new [KernelParam] instance.
         * @throws IllegalArgumentException if the provided [path] is invalid.
         */
        fun createFromPath(path: String, value: String): KernelParam {
            require(path.isKernelPathValid()) { "Invalid path: $path" }

            val derivedName = path.removeSuffix("/")
                .removePrefix(Consts.PROC_SYS)
                .replace("/", ".")
                .removePrefix(".")

            return KernelParam(derivedName, value, path)
        }
    }
}

/**
 * Checks if the path of this kernel parameter is valid.
 * A path is considered valid if:
 * - It is not empty after trimming whitespace.
 * - It starts with [Consts.PROC_SYS].
 * - It does not contain any "." characters (as paths use "/" as separators).
 *
 * @return `true` if the path is valid, `false` otherwise.
 */
private fun String.isKernelPathValid(): Boolean {
    if (this.trim().isEmpty() || !this.startsWith(Consts.PROC_SYS)) return false
    if (this.contains(".")) return false
    return true
}

/**
 * Checks if a string is a valid kernel parameter name.
 * A valid name:
 * - Is not empty or blank.
 * - Does not contain forward slashes ('/').
 * - Does not start or end with a dot ('.').
 *
 * @return `true` if the string is a valid name, `false` otherwise.
 */
private fun String.isKernelNameValid(): Boolean {
    if (this.trim().isEmpty() || this.contains("/")) return false
    if (this.startsWith(".") || this.endsWith(".")) return false
    return true
}


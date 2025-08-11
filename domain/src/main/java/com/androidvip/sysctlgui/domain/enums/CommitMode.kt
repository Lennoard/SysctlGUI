package com.androidvip.sysctlgui.domain.enums

/**
 * Defines the method used to commit kernel parameter changes.
 */
enum class CommitMode {
    /**
     * Commits the value using the `sysctl -w` command.
     * This is the default mode.
     */
    SYSCTL,
    /**
     * Commits the value to the file using `echo` command.
     * This method is generally safer and more reliable.
     */
    ECHO;

    companion object {
        fun parse(value: String): CommitMode {
            return when (value) {
                "sysctl" -> SYSCTL
                "echo" -> ECHO
                else -> SYSCTL
            }
        }
    }
}

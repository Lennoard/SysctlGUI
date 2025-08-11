package com.androidvip.sysctlgui.domain.exceptions

// TODO: Use sealed classes instead of exceptions
/**
 * Exception thrown when a value commit fails or refuses to be applied (value remains the same)
 */
class ApplyValueException(message: String) : Exception(message)

/**
 * Exception thrown when a value commit fails and the commit mode is "sysctl"
 */
class CommitModeException(message: String) : Exception(message)

/**
 * Exception thrown when a value to be committed is blank and blank values are not allowed
 */
class BlankValueNotAllowedException() : IllegalArgumentException()

/**
 * Exception thrown when a shell command fails
 */
class ShellCommandException(message: String, cause: Throwable) : Exception(message, cause)

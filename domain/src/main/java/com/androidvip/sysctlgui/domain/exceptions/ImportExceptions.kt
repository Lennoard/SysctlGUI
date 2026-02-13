package com.androidvip.sysctlgui.domain.exceptions

/**
 * Thrown when an invalid file type is trying to be imported.
 */
class InvalidFileException(message: String) : Exception(message)
/**
 * Thrown when an imported file is empty
 */
class EmptyFileException : Exception()
/**
 * Thrown when an invalid line is found during import.
 */
class MalformedLineException(message: String, cause: Throwable? = null) : Exception(message, cause)
class NoValidParamException : Exception()

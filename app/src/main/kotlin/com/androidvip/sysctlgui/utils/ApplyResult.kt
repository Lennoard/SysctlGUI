package com.androidvip.sysctlgui.utils

sealed class ApplyResult {
    object Success: ApplyResult()
    class Failure(val exception: Throwable): ApplyResult()
}
package com.androidvip.sysctlgui.domain

import androidx.annotation.StringRes

/**
 * Provides access to string resources.
 * This interface allows for fetching localized strings, potentially with formatting arguments.
 */
interface StringProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

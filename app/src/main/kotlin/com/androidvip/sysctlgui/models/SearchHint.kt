package com.androidvip.sysctlgui.models

/**
 * Represents a search hint displayed to the user.
 *
 * This holds information about a single search suggestion, including the text of the hint
 * and whether it originates from the user's search history.
 *
 * @property hint The text of the search hint.
 * @property isFromHistory A boolean flag indicating whether the hint is from the user's search history
 */
data class SearchHint(
    val hint: String,
    val isFromHistory: Boolean = false
)

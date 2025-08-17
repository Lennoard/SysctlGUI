package com.androidvip.sysctlgui.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface for accessing and modifying application preferences.
 *
 * This interface defines the contract for interacting with the application's settings,
 * allowing various parts of the app to read and write preference values.
 */
interface AppPrefs {
    fun <T> observeKey(key: String, default: T): Flow<T>
    var listFoldersFirst: Boolean
    var guessInputType: Boolean
    var commitMode: String
    var allowBlankValues: Boolean
    var useBusybox: Boolean
    var runOnStartUp: Boolean
    var startUpDelay: Int
    var showTaskerToast: Boolean
    var forceDark: Boolean
    var dynamicColors: Boolean
    var askedForNotificationPermission: Boolean
    var useOnlineDocs: Boolean
    var contrastLevel: Int
    val searchHistory: Set<String>
    fun addSearchToHistory(query: String)
    fun removeSearchFromHistory(query: String)
}

package com.androidvip.sysctlgui.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.androidvip.sysctlgui.data.Prefs
import com.androidvip.sysctlgui.domain.repository.AppPrefs

/**
 * Implementation of [AppPrefs] that uses [SharedPreferences] to store and retrieve app preferences.
 */
class AppPrefsImpl(private val prefs: SharedPreferences) : AppPrefs {
    override var listFoldersFirst: Boolean
        get() = prefs.getBoolean(Prefs.ListFoldersFirst.key, true)
        set(value) {
            prefs.edit { putBoolean(Prefs.ListFoldersFirst.key, value) }
        }
    override var guessInputType: Boolean
        get() = prefs.getBoolean(Prefs.GuessInputType.key, true)
        set(value) {
            prefs.edit { putBoolean(Prefs.GuessInputType.key, value) }
        }
    override var commitMode: String
        get() = prefs.getString(Prefs.CommitMode.key, "sysctl") ?: "sysctl"
        set(value) {
            prefs.edit { putString(Prefs.CommitMode.key, value) }
        }
    override var allowBlankValues: Boolean
        get() = prefs.getBoolean(Prefs.ALLOW_BLANK.key, false)
        set(value) {
            prefs.edit { putBoolean(Prefs.ALLOW_BLANK.key, value) }
        }
    override var useBusybox: Boolean
        get() = prefs.getBoolean(Prefs.UseBusybox.key, false)
        set(value) {
            prefs.edit { putBoolean(Prefs.UseBusybox.key, value) }
        }
    override var runOnStartUp: Boolean
        get() = prefs.getBoolean(Prefs.RunOnStartup.key, false)
        set(value) {
            prefs.edit { putBoolean(Prefs.RunOnStartup.key, value) }
        }
    override var startUpDelay: Int
        get() = prefs.getInt(Prefs.StartupDelay.key, 0)
        set(value) {
            prefs.edit { putInt(Prefs.StartupDelay.key, value) }
        }
    override var showTaskerToast: Boolean
        get() = prefs.getBoolean(Prefs.ShowTaskerToast.key, true)
        set(value) {
            prefs.edit { putBoolean(Prefs.ShowTaskerToast.key, value) }
        }
    override var forceDark: Boolean
        get() = prefs.getBoolean(Prefs.ForceDarkTheme.key, false)
        set(value) {
            prefs.edit { putBoolean(Prefs.ForceDarkTheme.key, value) }
        }
    override var dynamicColors: Boolean
        get() = prefs.getBoolean(Prefs.DynamicColors.key, false)
        set(value) {
            prefs.edit { putBoolean(Prefs.DynamicColors.key, value) }
        }
    override var askedForNotificationPermission: Boolean
        get() = prefs.getBoolean(Prefs.AskedNotificationPermission.key, false)
        set(value) {
            prefs.edit { putBoolean(Prefs.AskedNotificationPermission.key, value) }
        }
    override var useOnlineDocs: Boolean
        get() = prefs.getBoolean(Prefs.UseOnlineDocs.key, true)
        set(value) {
            prefs.edit { putBoolean(Prefs.UseOnlineDocs.key, value) }
        }
    override var contrastLevel: Int
        get() = prefs.getInt(Prefs.ContrastLevel.key, 1)
        set(value) {
            prefs.edit { putInt(Prefs.ContrastLevel.key, value) }
        }
    override val searchHistory: Set<String>
        get() = prefs.getStringSet(Prefs.SearchHistory.key, emptySet()) ?: emptySet()

    override fun addSearchToHistory(query: String) {
        val currentHistory = searchHistory.toMutableSet()
        currentHistory.add(query)
        prefs.edit { putStringSet(Prefs.SearchHistory.key, currentHistory) }
    }

    override fun removeSearchFromHistory(query: String) {
        val currentHistory = searchHistory.toMutableSet()
        currentHistory.remove(query)
        prefs.edit { putStringSet(Prefs.SearchHistory.key, currentHistory) }
    }
}

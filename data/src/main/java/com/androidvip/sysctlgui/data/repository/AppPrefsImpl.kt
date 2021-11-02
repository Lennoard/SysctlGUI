package com.androidvip.sysctlgui.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.androidvip.sysctlgui.domain.Consts
import com.androidvip.sysctlgui.domain.repository.AppPrefs

class AppPrefsImpl(private val prefs: SharedPreferences) : AppPrefs {
    override var listFoldersFirst: Boolean
        get() = prefs.getBoolean(Consts.Prefs.LIST_FOLDERS_FIRST, true)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.LIST_FOLDERS_FIRST, value) }
        }
    override var guessInputType: Boolean
        get() = prefs.getBoolean(Consts.Prefs.GUESS_INPUT_TYPE, true)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.GUESS_INPUT_TYPE, value) }
        }
    override var commitMode: String
        get() = prefs.getString(Consts.Prefs.COMMIT_MODE, "sysctl") ?: "sysctl"
        set(value) {
            prefs.edit { putString(Consts.Prefs.COMMIT_MODE, value) }
        }
    override var allowBlankValues: Boolean
        get() = prefs.getBoolean(Consts.Prefs.ALLOW_BLANK, false)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.ALLOW_BLANK, value) }
        }
    override var useBusybox: Boolean
        get() = prefs.getBoolean(Consts.Prefs.USE_BUSYBOX, false)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.USE_BUSYBOX, value) }
        }
    override var runOnStartUp: Boolean
        get() = prefs.getBoolean(Consts.Prefs.RUN_ON_START_UP, true)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.RUN_ON_START_UP, value) }
        }
    override var startUpDelay: Int
        get() = prefs.getInt(Consts.Prefs.START_UP_DELAY, 0)
        set(value) {
            prefs.edit { putInt(Consts.Prefs.START_UP_DELAY, value) }
        }
    override var showTaskerToast: Boolean
        get() = prefs.getBoolean(Consts.Prefs.SHOW_TASKER_TOAST, true)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.SHOW_TASKER_TOAST, value) }
        }
    override var migrationCompleted: Boolean
        get() = prefs.getBoolean(Consts.Prefs.MIGRATION_COMPLETED, false)
        set(value) {
            prefs.edit { putBoolean(Consts.Prefs.MIGRATION_COMPLETED, value) }
        }
}

package com.androidvip.sysctlgui.prefs

import android.content.Context
import com.androidvip.sysctlgui.prefs.base.BasePrefs


class Prefs(context: Context?): BasePrefs(context, fileName = "user-params.json") {
    companion object {
        const val LIST_FOLDERS_FIRST = "list_folders_first"
        const val GUESS_INPUT_TYPE = "guess_input_type"
        const val COMMIT_MODE = "commit_mode"
        const val ALLOW_BLANK = "allow_blank_values"
        const val USE_BUSYBOX = "use_busybox"
        const val RUN_ON_START_UP = "run_on_start_up"
        const val START_UP_DELAY = "startup_delay"
        const val SHOW_TASKER_TOAST = "show_tasker_toast"
    }

    override fun changeListener(): ChangeListener? = null
}

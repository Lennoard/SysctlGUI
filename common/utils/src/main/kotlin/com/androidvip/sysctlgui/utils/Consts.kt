package com.androidvip.sysctlgui.utils

object Consts {
    const val PROC_SYS = "/proc/sys"

    const val LIST_NUMBER_INVALID: Int = -1
    const val LIST_NUMBER_PRIMARY_TASKER: Int = 0
    const val LIST_NUMBER_SECONDARY_TASKER: Int = 1
    const val LIST_NUMBER_FAVORITES: Int = 2
    const val LIST_NUMBER_APPLY_ON_BOOT: Int = 3

    object Prefs {
        const val LIST_FOLDERS_FIRST = "list_folders_first"
        const val GUESS_INPUT_TYPE = "guess_input_type"
        const val COMMIT_MODE = "commit_mode"
        const val ALLOW_BLANK = "allow_blank_values"
        const val USE_BUSYBOX = "use_busybox"
        const val RUN_ON_START_UP = "run_on_start_up"
        const val START_UP_DELAY = "startup_delay"
        const val SHOW_TASKER_TOAST = "show_tasker_toast"
        const val MIGRATION_COMPLETED = "migration_completed"
    }
}

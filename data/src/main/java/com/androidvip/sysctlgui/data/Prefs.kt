package com.androidvip.sysctlgui.data

enum class Prefs(val key: String) {
    ListFoldersFirst("list_folders_first"),
    GuessInputType("guess_input_type"),
    CommitMode("commit_mode"),
    ALLOW_BLANK("allow_blank_values"),
    UseBusybox("use_busybox"),
    RunOnStartup("run_on_start_up"),
    StartupDelay("startup_delay"),
    ShowTaskerToast("show_tasker_toast"),
    ForceDarkTheme("force_dark_theme"),
    DynamicColors("dynamic_colors"),
    AskedNotificationPermission("asked_notification_permission"),
    UseOnlineDocs("use_online_docs"),
    ContrastLevel("contrast_level"),
    SearchHistory("search_history")
}

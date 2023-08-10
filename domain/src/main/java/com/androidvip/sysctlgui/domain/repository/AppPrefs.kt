package com.androidvip.sysctlgui.domain.repository

interface AppPrefs {
    var listFoldersFirst: Boolean
    var guessInputType: Boolean
    var commitMode: String
    var allowBlankValues: Boolean
    var useBusybox: Boolean
    var runOnStartUp: Boolean
    var startUpDelay: Int
    var showTaskerToast: Boolean
    var migrationCompleted: Boolean
    var forceDark: Boolean
    var dynamicColors: Boolean
}

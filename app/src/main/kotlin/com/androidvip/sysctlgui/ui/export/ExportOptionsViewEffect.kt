package com.androidvip.sysctlgui.ui.export

import androidx.annotation.StringRes

sealed interface ExportOptionsViewEffect {
    object ImportUserParams : ExportOptionsViewEffect
    object ExportUserParams : ExportOptionsViewEffect

    object BackupRuntimeParams : ExportOptionsViewEffect
    object RestoreRuntimeParams : ExportOptionsViewEffect

    class ShowImportError(@StringRes val messageRes: Int) : ExportOptionsViewEffect
    class ShowImportSuccess(val paramCount: Int) : ExportOptionsViewEffect

    class ShowExportError(@StringRes val messageRes: Int) : ExportOptionsViewEffect
    object ShowExportSuccess : ExportOptionsViewEffect
}

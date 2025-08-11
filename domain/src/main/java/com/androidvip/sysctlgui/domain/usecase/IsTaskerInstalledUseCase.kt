package com.androidvip.sysctlgui.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class IsTaskerInstalledUseCase(private val context: Context) {
    operator fun invoke(): Boolean {
        val packageManager = context.packageManager

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    TASKER_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                packageManager.getPackageInfo(TASKER_PACKAGE_NAME, 0)
            }
            true
        }.getOrDefault(false)
    }

    companion object {
        private const val TASKER_PACKAGE_NAME = "net.dinglisch.android.taskerm"
    }
}

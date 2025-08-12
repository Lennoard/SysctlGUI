package com.androidvip.sysctlgui.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.androidvip.sysctlgui.data.Prefs
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.enums.SettingItemType
import com.androidvip.sysctlgui.domain.models.AppSetting
import com.androidvip.sysctlgui.domain.repository.AppSettingsRepository
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AppSettingsRepositoryImpl(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val rootUtils: RootUtils,
    private val ioContext: CoroutineContext
) : AppSettingsRepository {
    override suspend fun getAppSettings(): List<AppSetting<*>> = withContext(ioContext) {
        val usingDynamicColors = sharedPreferences.getBoolean(Prefs.DynamicColors.key, false)
        val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        listOf(
            ///////////  GENERAL SETTINGS  ////////////
            AppSetting(
                key = Prefs.ListFoldersFirst.key,
                value = sharedPreferences.getBoolean(Prefs.ListFoldersFirst.key, true),
                category = "General",
                title = "List folders first",
                description = "List folders first when using the kernel parameter browser option",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.GuessInputType.key,
                value = sharedPreferences.getBoolean(Prefs.GuessInputType.key, true),
                category = "General",
                title = "Guess input type",
                description = "Try to set the best input type for the keyboard based on the value of the parameter",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.UseOnlineDocs.key,
                value = sharedPreferences.getBoolean(Prefs.UseOnlineDocs.key, true),
                category = "General",
                title = "Use online docs",
                description = "Try to use online documentation when displaying parameter descriptions",
                type = SettingItemType.Switch,
            ),

            ///////////  THEME SETTINGS  ////////////

            AppSetting(
                key = Prefs.ForceDarkTheme.key,
                value = sharedPreferences.getBoolean(Prefs.ForceDarkTheme.key, false),
                category = "Theme",
                title = "Force Dark",
                description = "Force dark theme when available",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.DynamicColors.key,
                value = usingDynamicColors,
                enabled = supportsDynamicColors,
                category = "Theme",
                title = "Dynamic Colors",
                description = "Use dynamic colors when available",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.ContrastLevel.key,
                enabled = !usingDynamicColors,
                value = sharedPreferences.getInt(Prefs.ContrastLevel.key, 1),
                category = "Theme",
                title = "Contrast level",
                description = "Contrast level for the theme colors",
                type = SettingItemType.Slider,
                values = listOf(1, 2, 3),
            ),

            ///////////  COMMIT SETTINGS  ////////////

            AppSetting(
                key = Prefs.CommitMode.key,
                value = sharedPreferences.getString(
                    Prefs.CommitMode.key,
                    CommitMode.SYSCTL.name.lowercase()
                ) ?: "sysctl",
                category = "Operations",
                title = "Commit mode",
                description = "Command used when applying the parameter value",
                type = SettingItemType.List,
                values = listOf(
                    CommitMode.SYSCTL.name.lowercase(),
                    CommitMode.ECHO.name.lowercase(),
                )
            ),
            AppSetting(
                key = Prefs.UseBusybox.key,
                value = sharedPreferences.getBoolean(Prefs.UseBusybox.key, false),
                enabled = rootUtils.isBusyboxAvailable(),
                category = "Operations",
                title = "Use busybox",
                description = "Use busybox to execute commands",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.ALLOW_BLANK.key,
                value = sharedPreferences.getBoolean(Prefs.ALLOW_BLANK.key, false),
                category = "Operations",
                title = "Allow blank values",
                type = SettingItemType.Switch,
            ),

            ///////////  STARTUP SETTINGS  ////////////

            AppSetting(
                key = Prefs.RunOnStartup.key,
                value = sharedPreferences.getBoolean(Prefs.RunOnStartup.key, false),
                category = "Startup",
                title = "Run on startup",
                description = "Allow the application to apply parameters on startup",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.StartupDelay.key,
                value = sharedPreferences.getInt(Prefs.StartupDelay.key, 0),
                category = "Startup",
                title = "Startup delay",
                description = "Delay in seconds before applying parameters on startup",
                type = SettingItemType.Slider,
                values = (0..10).toList(),
            ),
            AppSetting(
                key = "",
                value = Unit,
                category = "Startup",
                title = "Manage parameters",
                description = "Manage the parameters that will be applied at startup",
                type = SettingItemType.Text,
            )
        )
    }
}

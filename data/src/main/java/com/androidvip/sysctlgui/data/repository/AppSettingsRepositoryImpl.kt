package com.androidvip.sysctlgui.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.androidvip.sysctlgui.data.Prefs
import com.androidvip.sysctlgui.data.R
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.enums.SettingItemType
import com.androidvip.sysctlgui.domain.models.AppSetting
import com.androidvip.sysctlgui.domain.repository.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AppSettingsRepositoryImpl(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val rootUtils: RootUtils,
    private val ioContext: CoroutineContext = Dispatchers.IO
) : AppSettingsRepository {
    override suspend fun getAppSettings(): List<AppSetting<*>> = withContext(ioContext) {
        val usingDynamicColors = sharedPreferences.getBoolean(Prefs.DynamicColors.key, false)
        val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val currentCommitMode = sharedPreferences.getString(
            Prefs.CommitMode.key,
            CommitMode.SYSCTL.name.lowercase()
        ) ?: CommitMode.SYSCTL.name

        listOf(
            ///////////  GENERAL SETTINGS  ////////////
            AppSetting(
                key = Prefs.ListFoldersFirst.key,
                value = sharedPreferences.getBoolean(Prefs.ListFoldersFirst.key, true),
                category = context.getString(R.string.prefs_category_general),
                title = context.getString(R.string.prefs_list_folders_first_title),
                description = context.getString(R.string.prefs_list_folders_first_description),
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.GuessInputType.key,
                value = sharedPreferences.getBoolean(Prefs.GuessInputType.key, true),
                category = context.getString(R.string.prefs_category_general),
                title = context.getString(R.string.prefs_guess_input_type_title),
                description = context.getString(R.string.prefs_guess_input_type_description),
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.UseOnlineDocs.key,
                value = sharedPreferences.getBoolean(Prefs.UseOnlineDocs.key, true),
                category = context.getString(R.string.prefs_category_general),
                title = context.getString(R.string.prefs_online_docs_title),
                description = context.getString(R.string.prefs_online_docs_description),
                type = SettingItemType.Switch,
            ),

            ///////////  THEME SETTINGS  ////////////

            AppSetting(
                key = Prefs.ForceDarkTheme.key,
                value = sharedPreferences.getBoolean(Prefs.ForceDarkTheme.key, false),
                category = context.getString(R.string.prefs_category_theme),
                title = context.getString(R.string.prefs_force_dark_title),
                description = context.getString(R.string.prefs_force_dark_description),
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.DynamicColors.key,
                value = usingDynamicColors,
                enabled = supportsDynamicColors,
                category = context.getString(R.string.prefs_category_theme),
                title = context.getString(R.string.prefs_dynamic_colors_title),
                description = context.getString(R.string.prefs_dynamic_colors_description),
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.ContrastLevel.key,
                enabled = !usingDynamicColors,
                value = sharedPreferences.getInt(Prefs.ContrastLevel.key, 1),
                category = context.getString(R.string.prefs_category_theme),
                title = context.getString(R.string.prefs_contrast_level_title),
                description = context.getString(R.string.prefs_contrast_level_description),
                type = SettingItemType.Slider,
                values = listOf(CONTRAST_LEVEL_NORMAL, CONTRAST_LEVEL_MEDIUM, CONTRAST_LEVEL_HEIGH),
            ),

            ///////////  COMMIT SETTINGS  ////////////

            AppSetting(
                key = Prefs.CommitMode.key,
                value = currentCommitMode,
                category = context.getString(R.string.prefs_category_operations),
                title = context.getString(
                    R.string.prefs_commit_mode_title_format,
                    currentCommitMode
                ),
                description = context.getString(R.string.prefs_commit_mode_description),
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
                category = context.getString(R.string.prefs_category_operations),
                title = context.getString(R.string.prefs_use_busybox_title),
                description = context.getString(R.string.prefs_use_busybox_description),
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.ALLOW_BLANK.key,
                value = sharedPreferences.getBoolean(Prefs.ALLOW_BLANK.key, false),
                category = context.getString(R.string.prefs_category_operations),
                title = context.getString(R.string.prefs_allow_blank_values_title),
                type = SettingItemType.Switch,
            ),

            ///////////  STARTUP SETTINGS  ////////////

            AppSetting(
                key = Prefs.RunOnStartup.key,
                value = sharedPreferences.getBoolean(Prefs.RunOnStartup.key, false),
                category = context.getString(R.string.prefs_category_startup),
                title = context.getString(R.string.prefs_run_on_startup_title),
                description = context.getString(R.string.prefs_run_on_startup_description),
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.StartupDelay.key,
                value = sharedPreferences.getInt(Prefs.StartupDelay.key, 0),
                category = context.getString(R.string.prefs_category_startup),
                title = context.getString(R.string.prefs_startup_delay_title),
                description = context.getString(R.string.prefs_startup_delay_description),
                type = SettingItemType.Slider,
                values = (0..10).toList(),
            ),
            AppSetting(
                key = "",
                value = Unit,
                category = context.getString(R.string.prefs_category_startup),
                title = context.getString(R.string.prefs_manage_parameters_title),
                description = context.getString(R.string.prefs_manage_parameters_description),
                type = SettingItemType.Text,
            )
        )
    }
}

const val CONTRAST_LEVEL_NORMAL = 1
const val CONTRAST_LEVEL_MEDIUM = 2
const val CONTRAST_LEVEL_HEIGH = 3

package com.androidvip.sysctlgui.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.data.Prefs
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.design.utils.isLandscape
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.enums.SettingItemType
import com.androidvip.sysctlgui.domain.models.AppSetting
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import com.androidvip.sysctlgui.ui.settings.components.HeaderComponent
import com.androidvip.sysctlgui.ui.settings.components.SliderSettingComponent
import com.androidvip.sysctlgui.ui.settings.components.SwitchSettingComponent
import com.androidvip.sysctlgui.ui.settings.components.TextSettingComponent
import com.androidvip.sysctlgui.ui.settings.model.SettingsViewEffect
import com.androidvip.sysctlgui.ui.settings.model.SettingsViewEvent
import org.koin.androidx.compose.koinViewModel

internal const val DISABLED_ALPHA = 0.38f

@Composable
internal fun SettingsScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateToUserParams: () -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    showTopBar = true,
                    showNavBar = true,
                    showSearchAction = false
                )
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsViewEffect.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
            }
        }
    }

    SettingsScreenContent(
        settings = state.value.settings,
        onNavigateToUserParams = onNavigateToUserParams,
        onValueChanged = { appSetting, newValue ->
            viewModel.onEvent(SettingsViewEvent.SettingValueChanged(appSetting, newValue))
        }
    )
}

@Composable
private fun SettingsScreenContent(
    settings: List<AppSetting<*>> = emptyList(),
    onNavigateToUserParams: () -> Unit,
    onValueChanged: (AppSetting<*>, Any) -> Unit
) {
    val groupedSettings = settings.groupBy { it.category }
    val columns = if (isLandscape()) 2 else 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxWidth()
    ) {
        groupedSettings.forEach { (category, categorySettings) ->
            item(span = { GridItemSpan(columns) }) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = if (columns > 1) 16.dp else 56.dp,
                            end = 16.dp,
                        )
                )
            }

            items(
                items = categorySettings,
                key = { setting -> setting.key }
            ) { appSetting ->
                val itemModifier = if (columns > 1) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                } else {
                    Modifier.fillMaxWidth()
                }

                when (appSetting.type) {
                    SettingItemType.Text -> {
                        HeaderComponent(
                            modifier = itemModifier,
                            appSetting = appSetting,
                            onClick = onNavigateToUserParams
                        )
                    }
                    SettingItemType.List -> {
                        TextSettingComponent(
                            modifier = itemModifier,
                            appSetting = appSetting,
                            onValueChange = { newValue ->
                                onValueChanged(appSetting, newValue)
                            }
                        )
                    }
                    SettingItemType.Switch -> {
                        SwitchSettingComponent(
                            modifier = itemModifier,
                            appSetting = appSetting,
                            onValueChange = { newValue ->
                                onValueChanged(appSetting, newValue)
                            }
                        )
                    }
                    SettingItemType.Slider -> {
                        SliderSettingComponent(
                            modifier = itemModifier,
                            appSetting = appSetting,
                            onValueChange = { newValue ->
                                onValueChanged(appSetting, newValue)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
internal fun SettingsScreenPreview() {
    SysctlGuiTheme {
        val settings = listOf(
            ///////////  GENERAL SETTINGS  ////////////
            AppSetting(
                key = Prefs.ListFoldersFirst.key,
                value = true,
                category = "General",
                title = "List folders first",
                description = "List folders first when using the kernel parameter browser option",
                type = SettingItemType.Switch,
            ),
            AppSetting(
                key = Prefs.GuessInputType.key,
                value = true,
                category = "General",
                title = "Guess input type",
                description = "Description",
                type = SettingItemType.Switch,
            ),

            ///////////  THEME SETTINGS  ////////////

            AppSetting(
                key = Prefs.ContrastLevel.key,
                value = 1,
                category = "Theme",
                title = "Contrast level",
                description = "Contrast level for the theme colors",
                type = SettingItemType.Slider,
                values = listOf(1, 2, 3),
            ),

            ///////////  COMMIT SETTINGS  ////////////

            AppSetting(
                key = Prefs.CommitMode.key,
                value = "sysctl",
                category = "Operations",
                title = "Commit mode",
                description = "Command used when applying the parameter value",
                type = SettingItemType.List,
                values = listOf(
                    CommitMode.SYSCTL.name.lowercase(),
                    CommitMode.ECHO.name.lowercase(),
                )
            ),

            ///////////  STARTUP SETTINGS  ////////////
            AppSetting(
                key = "",
                value = Unit,
                category = "Startup",
                title = "Manage parameters",
                description = "Manage the parameters that will be applied at startup",
                type = SettingItemType.Text,
            )
        )
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            SettingsScreenContent(
                settings = settings,
                onNavigateToUserParams = {},
                onValueChanged = { _, _ -> }
            )
        }
    }
}
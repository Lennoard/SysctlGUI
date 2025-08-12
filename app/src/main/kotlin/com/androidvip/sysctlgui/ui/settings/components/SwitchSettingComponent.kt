package com.androidvip.sysctlgui.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.enums.SettingItemType
import com.androidvip.sysctlgui.domain.models.AppSetting

@Composable
fun SwitchSettingComponent(
    modifier: Modifier = Modifier,
    appSetting: AppSetting<*>,
    onValueChange: (newValue: Boolean) -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    var checked by remember { mutableStateOf(appSetting.value as? Boolean) }

    Row(
        modifier = modifier
            .padding(all = 16.dp)
            .clickable(enabled = appSetting.enabled) {
                onValueChange(!(checked ?: false))
                checked = !(checked ?: false)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(24.dp)
        ) {
            icon?.invoke()
        }

        SettingsComponentColumn(
            title = appSetting.title,
            description = appSetting.description,
            enabled = appSetting.enabled,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )

        Switch(
            checked = checked == true,
            onCheckedChange = {
                onValueChange(it)
                checked = it
            },
            enabled = appSetting.enabled,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
@PreviewLightDark
fun SwitchSettingComponentPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            SwitchSettingComponent(
                appSetting = AppSetting(
                    key = "key",
                    title = "Title",
                    description = "Description",
                    value = true,
                    category = "",
                    type = SettingItemType.Switch
                ),
                onValueChange = {}
            )
        }
    }
}
package com.androidvip.sysctlgui.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.enums.SettingItemType
import com.androidvip.sysctlgui.domain.models.AppSetting

@Composable
fun TextSettingComponent(
    modifier: Modifier = Modifier,
    appSetting: AppSetting<*>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clickable(enabled = appSetting.enabled && appSetting.type == SettingItemType.List) {
                expanded = true
            }
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            if (appSetting.iconResource != null) {
                Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(
                        painter = painterResource(appSetting.iconResource!!),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }

            SettingsComponentColumn(
                title = appSetting.title,
                description = appSetting.description,
                enabled = appSetting.enabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DropdownMenu(
            expanded = expanded,
            offset = DpOffset(16.dp, (-32).dp),
            onDismissRequest = { expanded = false }
        ) {
            appSetting.values?.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item as String,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onValueChange(item as String)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun TextSettingComponentPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            TextSettingComponent(
                appSetting = AppSetting(
                    key = "key",
                    title = "Title",
                    description = "Description",
                    value = "sysctl",
                    category = "",
                    iconResource = R.drawable.ic_history,
                    values = listOf("sysctl", "echo"),
                    type = SettingItemType.List
                ),
                onValueChange = {}
            )
        }
    }
}

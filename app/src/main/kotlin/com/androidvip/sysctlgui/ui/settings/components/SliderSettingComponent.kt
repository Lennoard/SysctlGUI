package com.androidvip.sysctlgui.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
fun SliderSettingComponent(
    modifier: Modifier = Modifier,
    appSetting: AppSetting<*>,
    onValueChange: (Int) -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(all = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(24.dp)
        ) {
            icon?.invoke()
        }

        val values = appSetting.values?.filterIsInstance<Int>() ?: emptyList()
        val minValue = values.min().toFloat()
        val maxValue = values.max().toFloat()
        var value by remember {
            mutableFloatStateOf((appSetting.value as? Int)?.toFloat() ?: minValue)
        }

        SettingsComponentColumn(
            title = appSetting.title,
            description = appSetting.description + " (${value.toInt()})",
            enabled = appSetting.enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                modifier = Modifier.padding(top = 4.dp),
                value = value,
                enabled = appSetting.enabled,
                onValueChange = { value = it.toInt().toFloat(); onValueChange(it.toInt()) },
                valueRange = minValue..maxValue,
            )
        }
    }
}

@Composable
@PreviewLightDark
fun SliderSettingComponentPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            SliderSettingComponent(
                appSetting = AppSetting(
                    key = "key",
                    title = "Title",
                    description = "Description",
                    value = 0,
                    category = "",
                    type = SettingItemType.Slider,
                    values = (0..10).toList(),
                ),
                onValueChange = {}
            )
        }
    }
}
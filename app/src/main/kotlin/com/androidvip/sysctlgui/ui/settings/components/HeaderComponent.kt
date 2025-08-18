package com.androidvip.sysctlgui.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.domain.models.AppSetting

@Composable
fun HeaderComponent(
    modifier: Modifier = Modifier,
    appSetting: AppSetting<*>,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
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
    }
}

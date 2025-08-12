package com.androidvip.sysctlgui.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.ui.settings.DISABLED_ALPHA

@Composable
internal fun SettingsComponentColumn(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
                .copy(alpha = if (enabled) 1f else DISABLED_ALPHA)
        )
        description?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
                    .copy(alpha = if (enabled) 0.8f else DISABLED_ALPHA)
            )
        }
        bottomContent?.let {
            bottomContent()
        }
    }
}

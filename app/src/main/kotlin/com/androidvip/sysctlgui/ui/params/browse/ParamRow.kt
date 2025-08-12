package com.androidvip.sysctlgui.ui.params.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.models.UiKernelParam

@Composable
fun ParamRow(
    modifier: Modifier = Modifier,
    param: UiKernelParam,
    onParamClicked: (UiKernelParam) -> Unit,
    showFullName: Boolean = false
) {
    val rowDescription = "Parameter: ${param.name}"
    val rowState = if (param.isFavorite) "Marked as favorite" else ""

    Row(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clickable { onParamClicked(param) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .semantics(mergeDescendants = showFullName) {
                    this.contentDescription = rowDescription
                    this.stateDescription = rowState
                }
                .padding(16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (showFullName) param.name else param.lastNameSegment,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            if (param.value.isNotBlank()) {
                Text(
                    text = param.value,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (param.isFavorite) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(18.dp)
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun ParamRowPreview() {
    val param = UiKernelParam(
        name = "vm.swappiness",
        path = "/proc/sys/vm/swappiness",
        value = "0"
    )

    SysctlGuiTheme(contrastLevel = 1) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ParamRow(param = param, onParamClicked = {}, showFullName = true)
            ParamRow(param = param.copy(isFavorite = true), onParamClicked = {},)
        }
    }
}
package com.androidvip.sysctlgui.ui.params.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.models.UiKernelParam

@Composable
fun ParamFileRow(
    modifier: Modifier = Modifier,
    param: UiKernelParam,
    onParamClicked: (UiKernelParam) -> Unit,
    showFavoriteIcon: Boolean = true,
) {
    Box(modifier = Modifier.clickable { onParamClicked(param) }) {
        val rowDescription = if (param.isDirectory) {
            stringResource(R.string.acessibility_directory_description_format, param.name)
        } else {
            stringResource(R.string.acessibility_param_description_format, param.name)
        }
        Row(
            modifier = modifier
                .semantics(mergeDescendants = true) { contentDescription = rowDescription }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParamIcon(param = param)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = param.lastNameSegment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (param.isDirectory) FontWeight.Bold else FontWeight.Normal
                )

                if (param.value.isNotBlank() && !param.isDirectory) {
                    Text(
                        text = param.value,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TrailingIcon(param = param, showFavoriteIcon = showFavoriteIcon)
        }
    }
}


@Composable
private fun ParamIcon(param: UiKernelParam) {
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val containerColor by remember(param.isDirectory) {
        derivedStateOf {
            if (param.isDirectory) primaryContainerColor else secondaryContainerColor
        }
    }

    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
    val iconColor by remember(param.isDirectory) {
        derivedStateOf {
            if (param.isDirectory) onPrimaryContainerColor else onSecondaryContainerColor
        }
    }

    val iconId = if (param.isDirectory) R.drawable.ic_folder else R.drawable.ic_file

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = stringResource(R.string.acessibility_param_icon_description),
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
    }
}

@Composable
private fun TrailingIcon(param: UiKernelParam, showFavoriteIcon: Boolean) {
    if (param.isDirectory) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(R.string.acessibility_davegate_to_directory_description),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else if (param.isFavorite && showFavoriteIcon) {
        Icon(
            imageVector = Icons.Rounded.Favorite,
            contentDescription = stringResource(R.string.favorites),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
@PreviewLightDark
@PreviewDynamicColors
private fun ParamFileRowPreview() {
    val param = UiKernelParam(
        name = "vm.swappiness",
        path = "/proc/sys/vm/swappiness",
        value = "0"
    )

    SysctlGuiTheme(dynamicColor = true) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ParamFileRow(param = param.copy(path = "C://"), onParamClicked = {})
            ParamFileRow(param = param.copy(path = "/home"), onParamClicked = {})
            ParamFileRow(param = param, onParamClicked = {})
            ParamFileRow(param = param.copy(isFavorite = true), onParamClicked = {})
        }
    }
}

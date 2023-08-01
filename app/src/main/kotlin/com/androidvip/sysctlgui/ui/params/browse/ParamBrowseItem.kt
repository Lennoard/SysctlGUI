package com.androidvip.sysctlgui.ui.params.browse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.design.theme.md_theme_light_background
import java.io.File

@Composable
fun ParamBrowseItem(
    onParamClick: (KernelParam) -> Unit,
    onDirectoryChanged: (File) -> Unit,
    param: KernelParam,
    paramFile: File
) {
    val isDir = paramFile.isDirectory
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val tintColor = if (isDir) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .clickable {
                if (isDir) onDirectoryChanged(paramFile) else onParamClick(param)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(42.dp), onDraw = {
                    drawCircle(color = if (isDir) surfaceColor else outlineColor)
                })

                val iconResource = if (isDir) {
                    R.drawable.ic_folder_outline
                } else {
                    R.drawable.ic_file_outline
                }
                Icon(
                    painter = painterResource(id = iconResource),
                    tint = tintColor,
                    contentDescription = ""
                )
            }
            Text(
                text = param.shortName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                style = if (isDir) {
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }
    }
}

@Preview
@Composable
fun ParamItemPreview() {
    val param = KernelParam(name = "test", value = "success")
    Box(modifier = Modifier.background(md_theme_light_background)) {
        ParamBrowseItem(
            onParamClick = {},
            onDirectoryChanged = {},
            param = param,
            paramFile = File("/")
        )
    }
}

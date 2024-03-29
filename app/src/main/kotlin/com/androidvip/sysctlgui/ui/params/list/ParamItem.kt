package com.androidvip.sysctlgui.ui.params.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.design.theme.md_theme_light_background

@Composable
fun ParamItem(onParamClick: (KernelParam) -> Unit, param: KernelParam) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onParamClick(param) }
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            text = param.shortName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            text = param.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview
@Composable
fun ParamItemPreview() {
    val param = KernelParam(name = "test", value = "success")
    Box(modifier = Modifier.background(md_theme_light_background)) {
        ParamItem(onParamClick = {}, param = param)
    }
}

package com.androidvip.sysctlgui.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String = stringResource(R.string.app_name),
    showBack: Boolean = false,
    showSearch: Boolean = false,
    onSearchPressed: () -> Unit,
    onBackPressed: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            AnimatedVisibility(visible = showBack) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.go_back)
                    )
                }
            }
        },
        title = {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = {
            AnimatedVisibility(
                visible = showSearch,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                IconButton(onClick = onSearchPressed) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
        }
    )
}

@Composable
@PreviewLightDark
private fun MainTopBarPreview() {
    SysctlGuiTheme {
        MainTopBar(
            title = "Sysctl GUI",
            showBack = false,
            showSearch = true,
            onSearchPressed = {},
            onBackPressed = {}
        )
    }
}
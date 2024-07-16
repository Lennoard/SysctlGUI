package com.androidvip.sysctlgui.ui.params.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.design.theme.md_theme_light_background
import com.androidvip.sysctlgui.ui.params.EmptyParamsWarning
import com.androidvip.sysctlgui.ui.params.list.ParamItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserParamsScreen(
    topBarTitle: String,
    params: List<KernelParam>,
    searchViewVisible: Boolean,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSearchPressed: () -> Unit,
    onSearchClose: () -> Unit,
    onDelete: (KernelParam) -> Unit,
    onParamClicked: (KernelParam) -> Unit,
    onBackPressed: () -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            if (searchViewVisible) {
                ParamSearch(
                    onSearch = onSearch,
                    onClose = onSearchClose,
                    onQueryChanged = onQueryChanged
                )
            } else {
                TopAppBar(
                    title = { Text(text = topBarTitle) },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(id = R.string.restore_param),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchPressed) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(id = R.string.search),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }
    ) { contentPadding ->
        if (params.isEmpty()) {
            Box(modifier = Modifier.padding(top = 64.dp)) { EmptyParamsWarning() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                state = listState
            ) {
                items(
                    items = params,
                    key = { param -> param.id },
                    itemContent = { param ->
                        SwipeToDismissContent(
                            onParamClick = onParamClicked,
                            onDelete = onDelete,
                            param = param
                        )
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SwipeToDismissContent(
    onParamClick: (KernelParam) -> Unit,
    onDelete: (KernelParam) -> Unit,
    param: KernelParam
) {
    val currentParam by rememberUpdatedState(newValue = param)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            fun getResultFromValueChange(): Boolean {
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    onDelete(currentParam)
                    return true
                }
                return false
            }
            getResultFromValueChange()
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    modifier = Modifier.padding(end = 16.dp),
                    painter = painterResource(id = R.drawable.ic_delete_sweep),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ParamItem(
                onParamClick = onParamClick,
                param = param
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamSearch(onSearch: (String) -> Unit, onClose: () -> Unit, onQueryChanged: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        query = searchText,
        onQueryChange = { searchText = it; onQueryChanged(it) },
        onSearch = onSearch,
        active = false,
        shape = RoundedCornerShape(0.dp),
        onActiveChange = { },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(id = R.string.search),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = android.R.string.cancel),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) {
    }
}

@Preview
@Composable
private fun UserParamsScreenPreview() {
    val params = buildList {
        repeat(15) { n ->
            add(
                KernelParam(
                    id = n,
                    favorite = n % 3 == 0,
                    name = buildString { (0..n).forEach { append((it * 4).toChar()) } },
                    value = "${n * 31}"
                )
            )
        }
    }
    Box(modifier = Modifier.background(md_theme_light_background)) {
        UserParamsScreen(
            topBarTitle = "Favorites",
            params = params,
            searchViewVisible = false,
            onQueryChanged = {},
            onSearch = {},
            onSearchPressed = {},
            onParamClicked = {},
            onDelete = {},
            onSearchClose = {},
            onBackPressed = {}
        )
    }
}

package com.androidvip.sysctlgui.ui.params.edit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditParamScreen(viewModel: EditParamViewModel) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val expandedFabState = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.edit_params)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(EditParamViewEvent.BackPressed) }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.restore_param),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButtonColumn(
                onReset = { viewModel.onEvent(EditParamViewEvent.ResetPressed) },
                onApply = { viewModel.onEvent(EditParamViewEvent.ApplyPressed) },
                hasApplied = state.hasApplied,
                expanded = expandedFabState.value
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            state = listState
        ) {
            item { ParamTexts(param = state.param) }
            item {
                ParamValues(
                    param = state.param,
                    appliedValue = state.appliedValue,
                    keyboardType = state.keyboardType,
                    singleLine = state.singleLine
                )
            }
            item {
                ParamActions(
                    onFavoriteClicked = { viewModel.onEvent(EditParamViewEvent.FavoritePressed) },
                    onTaskerClicked = { viewModel.onEvent(EditParamViewEvent.TaskerPressed) },
                    param = state.param,
                    taskerAvailable = state.taskerAvailable
                )
            }
            item { ParamDocs(info = state.paramInfo) }
        }
    }
}

@Composable
private fun FloatingActionButtonColumn(
    onReset: () -> Unit,
    onApply: () -> Unit,
    hasApplied: Boolean,
    expanded: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        if (hasApplied) {
            SmallFloatingActionButton(
                onClick = onReset,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(id = R.string.restore_param),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = stringResource(id = R.string.apply_param),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(id = R.string.apply_param),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            onClick = onApply,
            expanded = expanded,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun ParamTexts(param: KernelParam) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.param),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            ParamRow(iconRes = R.drawable.ic_config, text = param.configName)
            ParamRow(iconRes = R.drawable.ic_name, text = param.shortName)
            ParamRow(iconRes = R.drawable.ic_folder_outline, text = param.path)
        }
    }
}

@Composable
private fun ParamRow(@DrawableRes iconRes: Int, text: String) {
    Row(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ParamValues(
    param: KernelParam,
    appliedValue: String,
    keyboardType: KeyboardType,
    singleLine: Boolean
) {
    var typedValue by rememberSaveable { mutableStateOf(param.value) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(id = R.string.value),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = R.string.current_value),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            maxLines = 3,
            value = typedValue,
            onValueChange = { typedValue = it }
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = R.string.last_applied_value),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        SelectionContainer {
            Text(
                text = appliedValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ParamActions(
    onFavoriteClicked: () -> Unit,
    onTaskerClicked: () -> Unit,
    param: KernelParam,
    taskerAvailable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LargeFloatingActionButton(
                modifier = Modifier.size(74.dp),
                onClick = onFavoriteClicked,
                containerColor = if (param.favorite) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                contentColor = if (param.favorite) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.outline
                },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.set_favorite)
                )
            }

            Text(
                modifier = Modifier
                    .widthIn(max = 82.dp)
                    .padding(top = 2.dp),
                text = if (param.favorite) {
                    stringResource(id = R.string.remove_from_favorites)
                } else {
                    stringResource(id = R.string.set_favorite)
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (taskerAvailable) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LargeFloatingActionButton(
                    modifier = Modifier.size(74.dp),
                    onClick = onTaskerClicked,
                    containerColor = if (param.taskerParam) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    contentColor = if (param.taskerParam) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_tasker),
                        contentDescription = stringResource(id = R.string.set_favorite),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    modifier = Modifier
                        .widthIn(max = 82.dp)
                        .padding(top = 2.dp),
                    text = if (param.taskerParam) {
                        stringResource(id = R.string.remove_from_tasker_list)
                    } else {
                        stringResource(id = R.string.add_to_tasker_list)
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ParamDocs(info: String?) {
    Text(
        modifier = Modifier.padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp),
        text = stringResource(id = R.string.information),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )

    if (info != null) {
        Text(
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            text = info,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = stringResource(android.R.string.dialog_alert_title),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(id = R.string.no_info_available),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

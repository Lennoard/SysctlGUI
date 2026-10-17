package com.androidvip.sysctlgui.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.domain.models.ParamChange
import com.androidvip.sysctlgui.design.utils.isLandscape
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ParamChangeHistoryScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    viewModel: ParamChangeHistoryViewModel = koinViewModel(),
    showSnackbar: (String, String?, () -> Unit) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var rollbackConfirmation by remember { mutableStateOf<ParamChange?>(null) }
    var deleteChangeConfirmation by remember { mutableStateOf<ParamChange?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var deleteAllConfirmation by remember { mutableStateOf(false) }
    val title = stringResource(R.string.history)
    val locales = LocalConfiguration.current.locales
    val rollbackSuccessMessageFormat = stringResource(R.string.change_history_rollback_success)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ParamChangeHistoryEffect.RollbackSuccess -> showSnackbar(
                    String.format(Locale.getDefault(), rollbackSuccessMessageFormat, effect.paramName),
                    null
                ) {}

                is ParamChangeHistoryEffect.ShowError -> errorMessage = effect.message
                is ParamChangeHistoryEffect.ShowMessage -> showSnackbar(effect.message, null) {}
            }
        }
    }

    LaunchedEffect(title, state.changes.isNotEmpty()) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    topBarTitle = title,
                    showTopBar = true,
                    showNavBar = true,
                    showBackButton = false,
                    showSearchAction = false,
                    deleteAllHistoryAction = if (state.changes.isNotEmpty()) {
                        { deleteAllConfirmation = true }
                    } else {
                        null
                    }
                )
            )
        )
    }

    when {
        state.loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.changes.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.change_history_empty),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.FULL) }
            val changesByDate = state.changes.groupBy { change ->
                dateFormatter.format(Date(change.changedAt))
            }
            if (isLandscape()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    changesByDate.forEach { (date, changes) ->
                        val dateText = date.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                        stickyHeader(key = "date-$date") {
                            ChangeHistoryDateHeader(dateText)
                        }
                        items(changes, key = { "change-${it.id}" }) { change ->
                            ParamChangeCard(
                                change = change,
                                rollbackEnabled = !state.rollbackInProgress,
                                onRollback = { rollbackConfirmation = change },
                                onDelete = {
                                    viewModel.onEvent(ParamChangeHistoryEvent.DeleteConfirmed(change.id))
                                },
                                onDeleteRequested = { deleteChangeConfirmation = change }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    changesByDate.forEach { (date, changes) ->
                        val dateText = date.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                        stickyHeader(key = "date-$date") {
                            ChangeHistoryDateHeader(dateText)
                        }
                        items(changes, key = ParamChange::id) { change ->
                            ParamChangeCard(
                                change = change,
                                rollbackEnabled = !state.rollbackInProgress,
                                onRollback = { rollbackConfirmation = change },
                                onDelete = {
                                    viewModel.onEvent(ParamChangeHistoryEvent.DeleteConfirmed(change.id))
                                },
                                onDeleteRequested = { deleteChangeConfirmation = change }
                            )
                        }
                    }
                }
            }
        }
    }

    rollbackConfirmation?.let { change ->
        AlertDialog(
            onDismissRequest = { rollbackConfirmation = null },
            title = { Text(stringResource(R.string.change_history_rollback_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.change_history_rollback_confirmation,
                        change.paramName,
                        change.previousValue.truncateForDisplay()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !state.rollbackInProgress,
                    onClick = {
                        rollbackConfirmation = null
                        viewModel.onEvent(ParamChangeHistoryEvent.RollbackConfirmed(change))
                    }
                ) {
                    Text(stringResource(R.string.change_history_rollback))
                }
            },
            dismissButton = {
                TextButton(onClick = { rollbackConfirmation = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    deleteChangeConfirmation?.let { change ->
        AlertDialog(
            onDismissRequest = { deleteChangeConfirmation = null },
            title = { Text(stringResource(R.string.change_history_delete_title)) },
            text = { Text(stringResource(R.string.change_history_delete_confirmation, change.paramName)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteChangeConfirmation = null
                    viewModel.onEvent(ParamChangeHistoryEvent.DeleteConfirmed(change.id))
                }) {
                    Text(stringResource(R.string.change_history_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteChangeConfirmation = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    if (deleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { deleteAllConfirmation = false },
            title = { Text(stringResource(R.string.change_history_delete_all_title)) },
            text = { Text(stringResource(R.string.change_history_delete_all_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteAllConfirmation = false
                    viewModel.onEvent(ParamChangeHistoryEvent.ClearHistoryConfirmed)
                }) {
                    Text(stringResource(R.string.change_history_delete_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteAllConfirmation = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}

@Composable
private fun ChangeHistoryDateHeader(date: String) {
    Text(
        text = date,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ParamChangeCard(
    change: ParamChange,
    rollbackEnabled: Boolean,
    onRollback: () -> Unit,
    onDelete: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    var visible by remember(change.id) { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState()
    val swipeDescription = stringResource(R.string.change_history_delete)

    LaunchedEffect(visible) {
        if (!visible) {
            delay(DISMISS_ANIMATION_DURATION_MS.milliseconds)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically() + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            onDismiss = { visible = false },
            backgroundContent = {
                val backgroundColor =
                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_sweep),
                        contentDescription = swipeDescription,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onDeleteRequested
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = change.paramName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DateFormat.getDateTimeInstance().format(Date(change.changedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(
                                R.string.change_history_value_transition,
                                change.previousValue.truncateForDisplay(),
                                change.newValue.truncateForDisplay()
                            ),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(enabled = rollbackEnabled, onClick = onRollback) {
                            Text(stringResource(R.string.change_history_rollback))
                        }
                    }
                }
            }
        }
    }
}

private fun String.truncateForDisplay(maxLength: Int = MAX_DISPLAYED_VALUE_LENGTH): String =
    if (length > maxLength) take(maxLength) + "…" else this

private const val MAX_DISPLAYED_VALUE_LENGTH = 50
private const val DISMISS_ANIMATION_DURATION_MS = 300

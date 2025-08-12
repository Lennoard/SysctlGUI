package com.androidvip.sysctlgui.ui.presets

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.ui.components.ErrorContainer
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PresetsScreen(
    viewModel: PresetsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToImport: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            viewModel.onEvent(PresetsViewEvent.PresetFilePicked(uri))
        }
    )
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            viewModel.onEvent(PresetsViewEvent.BackUpFileCreated(uri))
        }
    )
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    showTopBar = true,
                    showNavBar = true,
                    showBackButton = false,
                    showSearchAction = false
                )
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PresetsViewEffect.ShowError -> {
                    errorMessage = effect.message
                    showError = true
                }
                PresetsViewEffect.ShowImportScreen -> onNavigateToImport()
                is PresetsViewEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                PresetsViewEffect.GoBack -> onNavigateBack()
            }
        }
    }

    AnimatedVisibility(visible = state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    PresetsScreenContent(
        onImportPressed = { pickFileLauncher.launch(arrayOf("*/*")) },
        onExportPressed = {
            val defaultFileName = "backup.conf"
            createFileLauncher.launch(defaultFileName)
        },
        onErrorAnimationEnd = { showError = false },
        showError = showError,
        errorMessage = errorMessage
    )
}

@Composable
private fun PresetsScreenContent(
    onImportPressed: () -> Unit,
    onExportPressed: () -> Unit,
    onErrorAnimationEnd: () -> Unit,
    showError: Boolean,
    errorMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ImportCards(
            onClick = onImportPressed,
            title = "Import",
            description = "Import presets from a file",
            iconRes = R.drawable.ic_import
        )
        ImportCards(
            onClick = onExportPressed,
            title = "Export",
            description = "Export presets to a file",
            iconRes = R.drawable.ic_export
        )

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = showError && errorMessage.isNotEmpty(),
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically{ it / 2 } + fadeOut(),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            ErrorContainer(message = errorMessage, onAnimationEnd = onErrorAnimationEnd)
        }
    }
}

@Composable
private fun ImportCards(onClick: () -> Unit, title: String, description: String, iconRes: Int) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$title - $description"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
@PreviewLightDark
@PreviewDynamicColors
private fun PresetsScreenPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            PresetsScreenContent(
                onImportPressed = {},
                onExportPressed = {},
                onErrorAnimationEnd = {},
                showError = true,
                errorMessage = "Error message"
            )
        }
    }
}
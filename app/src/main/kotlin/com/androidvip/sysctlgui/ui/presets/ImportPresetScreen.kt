package com.androidvip.sysctlgui.ui.presets

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.design.utils.isLandscape
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import org.koin.androidx.compose.koinViewModel

private const val SUCCESS_ANIMATION_DURATION = 4000

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ImportPresetScreen(
    viewModel: PresetsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    topBarTitle = context.getString(R.string.applying_preset),
                    showTopBar = true,
                    showNavBar = false,
                    showBackButton = true,
                    showSearchAction = false
                )
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PresetsViewEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is PresetsViewEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                PresetsViewEffect.GoBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = state.incomingPresetsScreenState,
            transitionSpec = {
                val isLoadingInitialState = initialState == IncomingPresetsScreenState.Loading
                val isSuccessTargetState = targetState == IncomingPresetsScreenState.Success
                if (isLoadingInitialState && isSuccessTargetState) {
                    val enterTransition = fadeIn() + scaleIn(initialScale = 0.8f)
                    val exitTransition = fadeOut() + scaleOut(targetScale = 0.9f)
                    enterTransition togetherWith exitTransition
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            }
        ) { targetState ->
            when (targetState) {
                IncomingPresetsScreenState.Idle -> {
                    if (isLandscape()) {
                        IncomingPresetsLandscapeContent(
                            paramsToImport = state.paramsToImport,
                            onImportPressed = {
                                viewModel.onEvent(PresetsViewEvent.ConfirmImportPressed)
                            },
                            onCancelPressed = {
                                viewModel.onEvent(PresetsViewEvent.CancelImportPressed)
                            }
                        )
                    } else {
                        IncomingPresetsContent(
                            paramsToImport = state.paramsToImport,
                            onImportPressed = {
                                viewModel.onEvent(PresetsViewEvent.ConfirmImportPressed)
                            },
                            onCancelPressed = {
                                viewModel.onEvent(PresetsViewEvent.CancelImportPressed)
                            }
                        )
                    }
                }

                IncomingPresetsScreenState.Loading -> {
                    LoadingIndicator()
                }

                IncomingPresetsScreenState.Success -> {
                    SuccessIndicator(
                        onAnimationEnd = {
                            viewModel.onEvent(PresetsViewEvent.CancelImportPressed)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomingPresetsContent(
    paramsToImport: List<KernelParam>,
    onImportPressed: () -> Unit,
    onCancelPressed: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.parameters_found_format, paramsToImport.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp)
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(
                items = paramsToImport,
                key = { index, item -> item.name }
            ) { index, item ->
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        )
                    }

                    val text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append(item.name)
                        }
                        append("=")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            append(item.value)
                        }
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCancelPressed
            ) {
                Text(text = stringResource(android.R.string.cancel))
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onImportPressed,
                enabled = paramsToImport.isNotEmpty(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(R.string.import_text))
            }
        }
    }
}

@Composable
private fun IncomingPresetsLandscapeContent(
    paramsToImport: List<KernelParam>,
    onImportPressed: () -> Unit,
    onCancelPressed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(
                items = paramsToImport,
                key = { index, item -> item.name }
            ) { index, item ->
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        )
                    }

                    val text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append(item.name)
                        }
                        append("=")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            append(item.value)
                        }
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.parameters_found_format, paramsToImport.size),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onCancelPressed
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onImportPressed,
                    enabled = paramsToImport.isNotEmpty(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = stringResource(R.string.import_text))
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.loading_preset),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessIndicator(onAnimationEnd: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }
    val progressTarget = if (animationStarted) 0f else 1f

    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = SUCCESS_ANIMATION_DURATION, easing = LinearEasing),
        finishedListener = { value ->
            if (value == 0f) {
                onAnimationEnd()
            }
        }
    )

    LaunchedEffect(Unit) { animationStarted = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = stringResource(R.string.success),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(128.dp)
        )

        Text(
            text = stringResource(R.string.presets_import_success_message),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            progress = { progress }
        )
    }
}

@Composable
@PreviewLightDark
@PreviewDynamicColors
private fun IncomingPresetsScreenPreview() {
    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            IncomingPresetsContent(
                paramsToImport = buildList {
                    repeat(16) {
                        add(
                            KernelParam(
                                name = "vm.swappiness.$it",
                                value = "value$it",
                                path = ""
                            )
                        )
                    }
                },
                onImportPressed = {},
                onCancelPressed = {},
            )
        }
    }
}

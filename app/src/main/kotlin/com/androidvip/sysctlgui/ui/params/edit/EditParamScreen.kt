package com.androidvip.sysctlgui.ui.params.edit

import android.content.ClipData
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.ui.components.ErrorContainer
import com.androidvip.sysctlgui.ui.components.SingleChoiceDialog
import com.androidvip.sysctlgui.ui.main.MainViewEffect
import com.androidvip.sysctlgui.ui.main.MainViewEvent
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.main.MainViewState
import com.androidvip.sysctlgui.utils.Consts
import com.androidvip.sysctlgui.utils.browse
import com.androidvip.sysctlgui.utils.performHapticFeedbackForToggle
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditParamScreen(
    viewModel: EditParamViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val taskerListOptions = listOf("Primary", "Secondary")
    var showSelectTaskerListDialog by rememberSaveable { mutableStateOf(false) }
    var selectedOptionIndex by rememberSaveable {
        mutableIntStateOf(Consts.LIST_NUMBER_PRIMARY_TASKER)
    }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel.onEvent(
            MainViewEvent.OnSateChangeRequested(
                MainViewState(
                    topBarTitle = "Edit kernel parameter",
                    showTopBar = true,
                    showNavBar = false,
                    showBackButton = true,
                    showSearchAction = false
                )
            )
        )

        mainViewModel.effect.collect { effect ->
            if (effect is MainViewEffect.ActUponSckbarActionPerformed) {
                viewModel.onEvent(EditParamViewEvent.UndoRequested)
            }
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EditParamViewEffect.GoBack -> onNavigateBack()

                is EditParamViewEffect.ShowError -> {
                    errorMessage = effect.message
                    showError = true
                }

                is EditParamViewEffect.OpenBrowser -> context.browse(effect.url)

                is EditParamViewEffect.ShowApplySuccess -> {
                    mainViewModel.onEvent(
                        MainViewEvent.ShowSnackbarRequested(
                            message = "Value applied successfully",
                            actionLabel = "Undo"
                        )
                    )
                }
            }
        }
    }

    EditParamContent(
        state = state.value,
        showError = showError,
        errorMessage = errorMessage,
        onValueApply = {
            viewModel.onEvent(EditParamViewEvent.ApplyPressed(it))
        },
        onTaskerClicked = {
            showSelectTaskerListDialog = it
            viewModel.onEvent(EditParamViewEvent.TaskerTogglePressed(it, selectedOptionIndex))
        },
        onDocsReadMorePressed = {
            viewModel.onEvent(EditParamViewEvent.DocumentationReadMoreClicked)
        },
        onFavoriteToggle = {
            viewModel.onEvent(EditParamViewEvent.FavoriteTogglePressed(it))
        },
        onErrorAnimationEnd = { showError = false },
        taskerListNameResolver = { listId -> taskerListOptions.getOrNull(listId).orEmpty() }
    )

    SingleChoiceDialog(
        showDialog = showSelectTaskerListDialog,
        title = "Choose a Tasker list",
        options = taskerListOptions,
        initialSelectedOptionIndex = selectedOptionIndex,
        onDismissRequest = { showSelectTaskerListDialog = false },
        onOptionSelected = {
            selectedOptionIndex = it
            viewModel.onEvent(EditParamViewEvent.TaskerTogglePressed(true, it))
        }
    )
}

@Composable
private fun EditParamContent(
    state: EditParamViewState,
    showError: Boolean,
    errorMessage: String,
    onDocsReadMorePressed: () -> Unit,
    onValueApply: (String) -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onTaskerClicked: (Boolean) -> Unit,
    onErrorAnimationEnd: () -> Unit,
    taskerListNameResolver: (Int) -> String = { "List #$it" },
) {
    val param = state.kernelParam
    val view = LocalView.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current
    val scrollState = rememberScrollState()

    val copyParamContentToClipboard = {
        val clipData = ClipData.newPlainText(
            "Kernel Parameter",
            "${param.lastNameSegment}=${param.value} (${param.path})"
        )
        val clipEntry = ClipEntry(clipData)
        coroutineScope.launch {
            clipboardManager.setClipEntry(clipEntry)
        }
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = param.lastNameSegment,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .combinedClickable(
                        enabled = true,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            Toast.makeText(context, "Long press to copy", Toast.LENGTH_SHORT).show()
                        },
                        onLongClick = copyParamContentToClipboard
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 64.dp),
                maxLines = 3,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = if (param.isTaskerParam) 0.dp else 24.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = param.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = param.path,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (state.taskerAvailable) {
                    TaskerButton(
                        isTaskerParam = param.isTaskerParam,
                        onToggle = { newState ->
                            performHapticFeedbackForToggle(newState, view)
                            onTaskerClicked(newState)
                        },
                        modifier = Modifier.scale(0.85f)
                    )
                }

                FavoriteButton(
                    isFavorite = param.isFavorite,
                    onFavoriteClick = { newState ->
                        performHapticFeedbackForToggle(newState, view)
                        onFavoriteToggle(newState)
                    },
                    modifier = Modifier.scale(0.85f)
                )
            }

            AnimatedVisibility(visible = param.isTaskerParam && state.taskerAvailable) {
                val listName = taskerListNameResolver(param.taskerList)
                AssistChip(
                    onClick = { onTaskerClicked(true) },
                    modifier = Modifier.padding(16.dp),
                    label = { Text(text = "Tasker list: $listName") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_tasker),
                            contentDescription = "Tasker list",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                )
            }
        }

        ParamValueContent(
            modifier = Modifier.padding(16.dp),
            param = param,
            keyboardType = state.keyboardType,
            onValueApply = onValueApply
        )

        AnimatedVisibility(
            visible = showError && errorMessage.isNotEmpty(),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            ErrorContainer(message = errorMessage, onAnimationEnd = onErrorAnimationEnd)
        }

        ParamDocs(
            modifier = Modifier.padding(16.dp),
            documentation = state.documentation,
            onReadMorePressed = onDocsReadMorePressed
        )
    }
}

@Composable
private fun ParamValueContent(
    modifier: Modifier = Modifier,
    param: UiKernelParam,
    keyboardType: KeyboardType,
    onValueApply: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedValue by remember(param.value) { mutableStateOf(param.value) }
    val view = LocalView.current

    BackHandler(
        enabled = isEditing,
        onBack = { isEditing = false }
    )

    HorizontalDivider()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Parameter value",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            EditableParamValue(
                isEditing = isEditing,
                paramValue = param.value,
                editedValue = editedValue,
                keyboardType = keyboardType,
                onEditorValueChange = { editedValue = it },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
        }

        IconButton(
            onClick = {
                if (isEditing) {
                    onValueApply(editedValue)
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                }
                isEditing = !isEditing
            }
        ) {
            AnimatedContent(
                targetState = isEditing,
                label = "EditButtonAnimation",
            ) { editingActive ->
                if (editingActive) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = "Apply",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EditableParamValue(
    isEditing: Boolean,
    paramValue: String,
    editedValue: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onEditorValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = isEditing,
            label = "EditableValueAnimation",
            transitionSpec = {
                if (targetState) {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                } else {
                    slideInVertically { -it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut()
                }.using(
                    SizeTransform(clip = true)
                )
            }
        ) { editingActive ->
            if (editingActive) {
                OutlinedTextField(
                    value = editedValue,
                    onValueChange = onEditorValueChange,
                    label = { Text("New value") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = paramValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ParamDocs(
    modifier: Modifier = Modifier,
    documentation: ParamDocumentation?,
    onReadMorePressed: () -> Unit,
) {
    HorizontalDivider()

    Column(modifier = modifier) {
        Text(
            text = "Documentation",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        AnimatedContent(targetState = documentation != null) { documentationAvailable ->
            if (documentationAvailable && documentation != null) {
                DocumentationContent(
                    documentation = documentation,
                    onReadMorePressed = onReadMorePressed
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
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = stringResource(android.R.string.dialog_alert_title),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "No documentation available",
                            style = MaterialTheme.typography.bodyLarge.copy(),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentationContent(
    documentation: ParamDocumentation,
    onReadMorePressed: () -> Unit
) {
    Column {
        val documentationText = if (!documentation.documentationHtml.isNullOrEmpty()) {
            AnnotatedString.fromHtml(
                htmlString = documentation.documentationHtml.orEmpty(),
                linkStyles = TextLinkStyles(
                    style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    ),
                    pressedStyle = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.tertiary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                )
            )
        } else {
            AnnotatedString(documentation.documentationText)
        }

        SelectionContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                text = documentationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(
            onClick = onReadMorePressed,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.End)
        ) {
            Text(text = "Read more")
        }
    }
}

@Composable
@PreviewLightDark
@PreviewDynamicColors
private fun EditParamContentPreview() {

    @Language("html")
    val htmlDocs = """
        <p>Correctable <a href="../">memory errors</a> are very common on servers.
        Soft-offline is kernel’s solution for memory pages having
        (excessive) corrected memory errors.</p>

        <p>For different types_of page, soft-offline has different behaviors / costs.</p>
        <ul>
            <li>For a raw error page, <code>soft-offline</code> migrates the in-use page’s content to a new raw page.</li>
            <li>For a page that is part of a transparent <b>hugepage</b>, <code>soft-offline</code> splits the transparent hugepage into raw pages, then migrates only the raw error page. As a result, user is transparently backed by 1 less hugepage, <u>impacting memory access performance</u>.</li>
            <li>For a page that is part of a HugeTLB <b>hugepage</b>, <code>soft-offline</code> first migrates the entire HugeTLB hugepage, during which a free hugepage will be consumed as migration target. Then the original hugepage is dissolved into raw pages without compensation, reducing the capacity of the HugeTLB pool by 1.</li>
            <li>It is user’s call to choose between reliability <i>(staying away from fragile physical memory)</i> vs performance / capacity implications in transparent and HugeTLB cases.</li>
        </ul>
    """.trimIndent()
        .replace(
            "<code>",
            "<font face=\"monospace\" color=\"#222\"><b><span style=\"background-color: #DCDCF5\">"
        )
        .replace("</code>", "</span></b></font>")

    var showError by remember { mutableStateOf(true) }

    SysctlGuiTheme(dynamicColor = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {

            val state = EditParamViewState(
                kernelParam = UiKernelParam(
                    name = "vm.enable_soft_offline",
                    path = "/proc/sys/vm/enable_soft_offline",
                    value = "1",
                    taskerList = 1,
                    isTaskerParam = false,
                    isFavorite = false
                ),
                taskerAvailable = true,
                keyboardType = KeyboardType.Number,
                documentation = ParamDocumentation(
                    title = "vm.enable_soft_offline",
                    documentationText = "",
                    documentationHtml = htmlDocs,
                    url = "url"
                ),
            )
            EditParamContent(
                state = state,
                showError = showError,
                errorMessage = "Sysctl command for 'wm.swappiness' executed, " +
                        "but output did not confirm the change. Output: 'Access denied'. " +
                        "Try using '${CommitMode.ECHO}' mode.",
                onValueApply = {},
                onTaskerClicked = {},
                onDocsReadMorePressed = {},
                onFavoriteToggle = {},
                onErrorAnimationEnd = { showError = false }
            )
        }
    }
}

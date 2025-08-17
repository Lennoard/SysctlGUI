package com.androidvip.sysctlgui.ui.params.edit

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.enums.CommitMode
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.models.UiKernelParam
import com.androidvip.sysctlgui.ui.components.ErrorContainer
import com.androidvip.sysctlgui.utils.performHapticFeedbackForToggle
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language

@Composable
internal fun EditParamLandscapeContent(
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
    val copyParamContentToClipboard = {
        val clipData = ClipData.newPlainText(
            context.getString(R.string.kernel_params),
            "${param.lastNameSegment}=${param.value} (${param.path})"
        )
        val clipEntry = ClipEntry(clipData)
        coroutineScope.launch {
            clipboardManager.setClipEntry(clipEntry)
        }
        Toast.makeText(
            context,
            context.getString(R.string.copied_to_clipboard),
            Toast.LENGTH_SHORT
        ).show()
    }

    Row {
        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = param.lastNameSegment,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .combinedClickable(
                        enabled = true,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            Toast.makeText(
                                context,
                                context.getString(R.string.long_press_to_copy),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onLongClick = copyParamContentToClipboard
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp),
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
                    label = { Text(text = stringResource(R.string.tasker_list_format, listName)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_tasker),
                            contentDescription = stringResource(R.string.tasker_list),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
        ) {
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
}

@Composable
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
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
            EditParamLandscapeContent(
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

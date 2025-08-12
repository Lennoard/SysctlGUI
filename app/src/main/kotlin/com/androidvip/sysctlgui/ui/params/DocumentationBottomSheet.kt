package com.androidvip.sysctlgui.ui.params

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.domain.models.ParamDocumentation
import com.androidvip.sysctlgui.utils.browse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import kotlin.text.append

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentationBottomSheet(
    documentation: ParamDocumentation,
    sheetState: SheetState
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } },
        sheetState = sheetState,
    ) {
        DocumentationBottomSheetContent(
            documentation = documentation,
            sheetState = sheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentationBottomSheetContent(
    documentation: ParamDocumentation,
    sheetState: SheetState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    Column(modifier = Modifier.padding(24.dp)) {
        val context = LocalContext.current
        Text(
            text = documentation.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

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
        Text(
            text = documentationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (documentation.url != null) {
            TextButton(
                onClick = {
                    context.browse(documentation.url.orEmpty())
                    coroutineScope.launch { sheetState.hide() }
                },
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .padding(top = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_browser),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Read more")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@PreviewLightDark
private fun DocumentationBottomSheetPreview() {

    @Language("HTML")
    val htmlDocs = """
        <p>
            When <b>BPF JIT compiler</b> is enabled, then compiled images are unknown
            addresses to the kernel, meaning they neither <a href="../">show up in traces</a> nor
            in <i>/proc/kallsyms</i>. This enables export of these addresses, which can
            be used for debugging/tracing. <u>If bpf_jit_harden</u> is enabled, this
            feature is disabled.
        </p>
        <p>Values :</p>
        <ul>
            <li>0 - disable JIT kallsyms export (default value)</li>
            <li>1 - enable JIT kallsyms export for privileged users only</li>
        </ul>
    """.trimIndent()

    val documentation = ParamDocumentation(
        title = "/proc/sys/fs",
        url = "https://docs.kernel.org/admin-guide/sysctl/fs.html",
        documentationText = """
            The files in this directory can be used to tune and monitor miscellaneous and general
            things in the operation of the Linux kernel. It is advisable to read both
            documentation and source before actually making adjustments.
        """.trimIndent(),
        documentationHtml = htmlDocs
    )

    SysctlGuiTheme(dynamicColor = true) {
        val state = rememberModalBottomSheetState()

        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            DocumentationBottomSheetContent(documentation = documentation, state)
        }
    }
}

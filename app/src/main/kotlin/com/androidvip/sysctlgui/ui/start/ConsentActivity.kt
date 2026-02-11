package com.androidvip.sysctlgui.ui.start

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.design.utils.AnimatedStrikeThroughIcon
import com.androidvip.sysctlgui.design.utils.StatusBarProtection
import com.androidvip.sysctlgui.design.utils.isLandscape
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import org.koin.android.ext.android.inject

class ConsentActivity : ComponentActivity() {
    private val prefs by inject<AppPrefs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            SysctlGuiTheme {
                ScreenContent(
                    onClosePressed = { finish() },
                    onContinuePressed = { granted ->
                        if (granted) startMainActivity() else finish()
                    }
                )
                StatusBarProtection()
            }
        }
    }

    private fun startMainActivity() {
        prefs.consentGranted = true
        val mainIntent = Intent(this, StartActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtras(intent.extras ?: Bundle())
        }
        startActivity(mainIntent)
        finish()
    }

    @Composable
    private fun ScreenContent(
        onContinuePressed: (granted: Boolean) -> Unit = {},
        onClosePressed: () -> Unit = {}
    ) {
        var consentGranted by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            if (isLandscape()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .displayCutoutPadding()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ConsentInfo(consentGranted)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RevertInfoCard()
                        ConsentActions(
                            onContinuePressed = { onContinuePressed(consentGranted) },
                            onConsentChanged = { consentGranted = it },
                            onClosePressed = onClosePressed
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .systemBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.size(24.dp))
                    ConsentInfo(consentGranted)
                    Spacer(modifier = Modifier.size(24.dp))
                    RevertInfoCard()
                    ConsentActions(
                        onContinuePressed = { onContinuePressed(consentGranted) },
                        onConsentChanged = { consentGranted = it },
                        onClosePressed = onClosePressed
                    )
                }
            }
        }
    }

    @Composable
    private fun RevertInfoCard() {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_restore_settings),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = stringResource(R.string.consent_revert_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    @Composable
    private fun ConsentInfo(consentGranted: Boolean) {
        AnimatedStrikeThroughIcon(
            icon = ImageVector.vectorResource(R.drawable.ic_edit),
            modifier = Modifier.size(92.dp),
            isOff = !consentGranted,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.consent_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = stringResource(R.string.consent_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )
    }

    @Composable
    private fun ConsentActions(
        modifier: Modifier = Modifier,
        onContinuePressed: () -> Unit,
        onClosePressed: () -> Unit,
        onConsentChanged: (granted: Boolean) -> Unit
    ) {
        var checked by remember { mutableStateOf(false) }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { checked = !checked; onConsentChanged(checked) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it; onConsentChanged(it) }
            )
            Text(
                text = stringResource(R.string.consent_checkbox),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Button(
            onClick = onContinuePressed,
            enabled = checked,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.continue_),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        FilledTonalButton(onClick = onClosePressed) {
            Text(
                text = stringResource(R.string.exit),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    @Composable
    @PreviewLightDark
    @Preview(device = "spec:parent=pixel_5,orientation=landscape", showSystemUi = true)
    private fun Preview() {
        SysctlGuiTheme {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                ScreenContent()
            }
            StatusBarProtection()
        }
    }
}

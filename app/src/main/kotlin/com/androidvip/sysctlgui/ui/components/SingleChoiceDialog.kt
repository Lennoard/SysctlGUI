package com.androidvip.sysctlgui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.R as AndroidResources

@Composable
fun SingleChoiceDialog(
    showDialog: Boolean,
    title: String,
    options: List<String>,
    initialSelectedOptionIndex: Int,
    onDismissRequest: () -> Unit,
    onOptionSelected: (Int) -> Unit
) {
    if (showDialog) {
        var selectedOptionIndex by remember { mutableIntStateOf(initialSelectedOptionIndex) }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title) },
            text = {
                Column {
                    options.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOptionIndex = index }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (index == selectedOptionIndex),
                                onClick = { selectedOptionIndex = index }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onOptionSelected(selectedOptionIndex)
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(AndroidResources.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(AndroidResources.string.cancel))
                }
            }
        )
    }
}

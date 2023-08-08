package com.androidvip.sysctlgui.ui.params.edit

import androidx.compose.ui.text.input.KeyboardType
import com.androidvip.sysctlgui.data.models.KernelParam

data class EditParamViewState(
    val param: KernelParam = KernelParam(),
    val appliedValue: String = "", // Backup,
    val typedValue: String = "",
    val hasApplied: Boolean = false,
    val paramInfo: String? = null,
    val taskerAvailable: Boolean = false,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val singleLine: Boolean = true
)

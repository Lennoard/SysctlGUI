package com.androidvip.sysctlgui.ui.params.edit

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.text.input.KeyboardType
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.UpdateUserParamUseCase
import com.androidvip.sysctlgui.readLines
import com.androidvip.sysctlgui.utils.BaseViewModel
import java.io.InputStream

class EditParamViewModel(
    private val prefs: AppPrefs,
    private val applyParams: ApplyParamsUseCase,
    private val updateUserParam: UpdateUserParamUseCase
) : BaseViewModel<EditParamViewEvent, EditParamViewState, EditParamViewEffect>() {
    override fun createInitialState(): EditParamViewState = EditParamViewState()

    override fun onEvent(event: EditParamViewEvent) {
        when (event) {
            EditParamViewEvent.ApplyPressed -> TODO()
            EditParamViewEvent.BackPressed -> TODO()
            EditParamViewEvent.FavoritePressed -> TODO()
            is EditParamViewEvent.ParamValueInputChanged -> {
                setState { copy(typedValue = event.newValue) }
            }
            is EditParamViewEvent.ReceivedParam -> setInitialState(event.param, event.context)
            EditParamViewEvent.ResetPressed -> TODO()
            EditParamViewEvent.TaskerPressed -> TODO()
        }
    }

    private fun setInitialState(param: KernelParam, context: Context) {
        val keyboardType = getKeyboardTypeForValue(param.value)
        val singleLine = keyboardType != KeyboardType.Text ||
            param.value.length <= PARAM_LENGTH_INPUT_THRESHOLD

        setState {
            copy(
                param = param,
                appliedValue = param.value,
                typedValue = param.value,
                paramInfo = findParamInfo(param, context),
                taskerAvailable = isTaskerInstalled(context),
                keyboardType = keyboardType,
                singleLine = singleLine
            )
        }
    }

    private fun getKeyboardTypeForValue(paramValue: String): KeyboardType {
        if (!prefs.guessInputType) return KeyboardType.Text

        val intValue = paramValue.toIntOrNull()
        if (intValue != null) return KeyboardType.Number

        val decimalValue = paramValue.toDoubleOrNull()
        if (decimalValue != null) return KeyboardType.Decimal

        return KeyboardType.Text
    }

    private fun findParamInfo(param: KernelParam, context: Context): String? = with(context) {
        val paramName = param.shortName
        val resId = resources.getIdentifier(
            paramName.replace("-", "_"),
            "string",
            packageName
        )
        val stringRes = if (resId != 0) runCatching { getString(resId) }.getOrNull() else null

        // Prefer the documented string resource
        if (stringRes != null) return stringRes

        if (!param.path.startsWith("/")) return null

        val subdirs = param.path.split("/")
        if (subdirs.isEmpty() || subdirs.size < SUBDIR_THRESHOLD) return null

        // Finding param info within subdir whole documentation string

        val rawInputStream: InputStream? = when (subdirs[3]) { // /proc/sys/[?]
            "abi" -> resources.openRawResource(R.raw.abi)
            "fs" -> resources.openRawResource(R.raw.fs)
            "kernel" -> resources.openRawResource(R.raw.kernel)
            "net" -> resources.openRawResource(R.raw.net)
            "vm" -> resources.openRawResource(R.raw.vm)
            else -> null
        }

        val documentation = buildString {
            rawInputStream.readLines {
                append(it)
                append("\n")
            }
        }
        if (documentation.isEmpty()) return null

        /*
        Trying to match:

        ===============

        paramName

        the               <==
        actual            <==
        documentation     <==

        ===============
         */
        val info: String? = runCatching {
            documentation
                .split("=+".toRegex())
                .last { it.contains("$paramName\n") }
                .split("$paramName\n")
                .last()
        }.getOrNull()

        return if (!info.isNullOrEmpty()) info else null
    }

    private fun isTaskerInstalled(context: Context): Boolean {
        val packageManager = context.packageManager

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    TASKER_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(TASKER_PACKAGE_NAME, 0)
            }
            true
        }.getOrDefault(false)
    }

    companion object {
        private const val PARAM_LENGTH_INPUT_THRESHOLD = 12
        private const val SUBDIR_THRESHOLD = 4
        private const val TASKER_PACKAGE_NAME = "net.dinglisch.android.taskerm"
    }
}

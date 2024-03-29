package com.androidvip.sysctlgui.ui.params.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.utils.ComposeTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditKernelParamActivity : ComponentActivity() {
    private val viewModel by viewModel<EditParamViewModel>()
    private val isEditingSavedParam: Boolean
        get() = intent.getBooleanExtra(EXTRA_EDIT_SAVED_PARAM, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTheme {
                EditParamScreen(viewModel = viewModel)
            }
        }

        lifecycleScope.launch {
            viewModel.effect.collect(::handleViewEffect)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent ?: return)
    }

    private fun handleIntent(intent: Intent) {
        val param = intent.getParcelableExtra(EXTRA_PARAM) as? KernelParam
        if (param != null) {
            viewModel.onEvent(EditParamViewEvent.ReceivedParam(param, this))
        } else {
            finishWithInvalidParamError()
        }
    }

    private fun finishWithInvalidParamError() {
        toast(R.string.unexpected_error)
        finish()
    }

    private fun handleViewEffect(effect: EditParamViewEffect) {
        when (effect) {
            EditParamViewEffect.NavigateBack -> onBackPressedDispatcher.onBackPressed()
            EditParamViewEffect.ShowTaskerListSelection -> {
                selectTaskerListAsDialog { listId ->
                    viewModel.onEvent(EditParamViewEvent.TaskerListSelected(listId))
                }
            }

            is EditParamViewEffect.ShowApplyError -> doAfterParamNotApplied(effect.messageRes)
            is EditParamViewEffect.ShowApplySuccess -> doAfterParamApplied()
        }
    }

    private fun selectTaskerListAsDialog(block: (Int) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.select_tasker_list)
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .setSingleChoiceItems(R.array.tasker_lists, -1) { dialog, which ->
                block(which)
                dialog.dismiss()
            }.also {
                if (!isFinishing) {
                    it.show()
                }
            }
    }

    private fun doAfterParamApplied() {
        if (isEditingSavedParam) {
            setResult(Activity.RESULT_OK)
            toast(R.string.done)
            finish()
        }
    }

    private fun doAfterParamNotApplied(@StringRes messageRes: Int) {
        toast(messageRes)
        if (isEditingSavedParam) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    companion object {
        const val EXTRA_EDIT_SAVED_PARAM = "edit_saved_param"
        const val EXTRA_PARAM = "param"

        fun getIntent(context: Context, param: KernelParam): Intent {
            return Intent(context, EditKernelParamActivity::class.java).apply {
                putExtra(EXTRA_PARAM, param)
            }
        }
    }
}

package com.androidvip.sysctlgui.ui.params.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.UpdateUserParamUseCase
import com.androidvip.sysctlgui.showAsLight
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditKernelParamActivity : AppCompatActivity() {
    private val viewModel by viewModel<EditParamViewModel>()
    private val applyParamsUseCase: ApplyParamsUseCase by inject()
    private val updateUserParamUseCase: UpdateUserParamUseCase by inject()

    private lateinit var kernelParameter: KernelParam

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.effect.collect(::handleViewEffect)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent ?: return)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_favorite -> {
                if (kernelParameter.favorite) {
                    kernelParameter.favorite = false
                    item.setIcon(R.drawable.ic_favorite_unselected)
                } else {
                    kernelParameter.favorite = true
                    item.setIcon(R.drawable.ic_favorite_selected)
                }

                lifecycleScope.launch {
                    updateUserParamUseCase(kernelParameter)
                }
                return true
            }

            R.id.action_tasker -> {
                selectTaskerListAsDialog { taskerList ->
                    if (kernelParameter.taskerParam) {
                        kernelParameter.taskerParam = false
                        item.setIcon(R.drawable.ic_action_tasker_add)
                        toast(getString(R.string.removed_from_tasker_list, taskerList))
                    } else {
                        kernelParameter.favorite = true
                        item.setIcon(R.drawable.ic_action_tasker_remove)
                        toast(getString(R.string.added_to_tasker_list, taskerList))
                    }

                    kernelParameter.taskerList = taskerList
                    lifecycleScope.launch {
                        updateUserParamUseCase(kernelParameter)
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent(intent: Intent) {
        val extraParam = RemovableParamAdapter.EXTRA_PARAM
        val param = intent.getParcelableExtra(extraParam) as? KernelParam
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

    private suspend fun applyParam() {
        val isEditingSavedParam = intent.getBooleanExtra(
            RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM,
            false
        )

        val newValue = "binding.editParamInput.text.toString()"
        kernelParameter.value = newValue

        val result = runCatching { applyParamsUseCase(kernelParameter) }
        val feedback = if (result.isSuccess) {
            setResult(Activity.RESULT_OK)
            updateUserParamUseCase(kernelParameter)
            getString(R.string.done)
        } else {
            setResult(Activity.RESULT_CANCELED)
            getString(R.string.apply_failure_format, result.exceptionOrNull()?.message.orEmpty())
        }

        if (isEditingSavedParam) {
            toast(feedback)
            finish()
        } else {
            Snackbar.make(
                View(this),
                feedback,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.undo) {
                lifecycleScope.launchWhenResumed {
                    updateUserParamUseCase(kernelParameter)
                }
            }.showAsLight()
        }
    }

    private fun isTaskerInstalled(): Boolean {
        return runCatching {
            packageManager.getPackageInfo("net.dinglisch.android.taskerm", 0)
            true
        }.getOrDefault(false)
    }

    companion object {
        const val NAME_TRANSITION_NAME = "transition_title"
        const val VALUE_TRANSITION_NAME = "transition_value"
    }
}

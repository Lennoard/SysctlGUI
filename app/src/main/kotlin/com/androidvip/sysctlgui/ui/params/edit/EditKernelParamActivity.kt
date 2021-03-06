package com.androidvip.sysctlgui.ui.params.edit

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.databinding.ActivityEditKernelParamBinding
import com.androidvip.sysctlgui.ui.settings.RemovableParamAdapter
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.utils.ApplyResult
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.InputStream

class EditKernelParamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditKernelParamBinding
    private val prefs: SharedPreferences by inject()
    private val repository: ParamRepository by inject()

    private lateinit var kernelParameter: KernelParam

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditKernelParamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extraParam = RemovableParamAdapter.EXTRA_PARAM
        (intent.getParcelableExtra(extraParam) as? KernelParam)?.also {
            kernelParameter = it

            if (!kernelParameter.hasValidPath() || !kernelParameter.hasValidName()) {
                showInvalidParamError()
            } else {
                defineInputTypeForValue(kernelParameter.value)
                binding.editParamInput.setText(kernelParameter.value)
                Handler().postDelayed({ updateTextUi(kernelParameter) }, 100)

                binding.editParamApply.setOnClickListener {
                    lifecycleScope.launch {
                        applyParam(kernelParameter)
                    }
                }
            }
        } ?: run {
            showInvalidParamError()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_params, menu)
        menu?.findItem(R.id.action_favorite)?.let {
            if (kernelParameter.favorite) {
                it.setIcon(R.drawable.ic_favorite_selected)
            } else {
                it.setIcon(R.drawable.ic_favorite_unselected)
            }
        }

        menu?.findItem(R.id.action_tasker)?.let {
            if (isTaskerInstalled()) {
                it.isVisible = true
                if (kernelParameter.taskerParam) {
                    it.setIcon(R.drawable.ic_action_tasker_remove)
                } else {
                    it.setIcon(R.drawable.ic_action_tasker_add)
                }
            } else {
                it.isVisible = false
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_favorite -> {
                if (kernelParameter.favorite) {
                    kernelParameter.favorite = false
                    item.setIcon(R.drawable.ic_favorite_unselected)
                } else {
                    kernelParameter.favorite = true
                    item.setIcon(R.drawable.ic_favorite_selected)
                }

                lifecycleScope.launch {
                    repository.update(kernelParameter, ParamRepository.SOURCE_ROOM)
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
                        repository.update(kernelParameter, ParamRepository.SOURCE_ROOM)
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTextUi(KernelParam: KernelParam) {
        val paramName = KernelParam.name.split(".").last()
        binding.editParamName.text = paramName

        YoYo.with(Techniques.SlideInLeft)
            .duration(600)
            .interpolate(
                AnimationUtils.loadInterpolator(
                    this,
                    android.R.anim.accelerate_decelerate_interpolator
                )
            )
            .playOn(binding.editParamName)

        Handler().postDelayed({
            YoYo.with(Techniques.SlideInLeft)
                .duration(600)
                .interpolate(
                    AnimationUtils.loadInterpolator(
                        this,
                        android.R.anim.accelerate_decelerate_interpolator
                    )
                )
                .playOn(binding.editParamSub)

            binding.editParamSub.text = KernelParam.name.removeSuffix(paramName).removeSuffix(".")
        }, 100)

        Handler().postDelayed({
            binding.editParamInfo.text = findInfoForParam(KernelParam)
            YoYo.with(Techniques.ZoomIn)
                .duration(260)
                .interpolate(
                    AnimationUtils.loadInterpolator(
                        this,
                        android.R.anim.accelerate_decelerate_interpolator
                    )
                )
                .playOn(binding.editParamInfo)

            binding.editParamApply.show()
        }, 300)
    }

    private fun showInvalidParamError() {
        binding.editParamErrorText.show()
        binding.editParamScroll.goAway()
        binding.editParamApply.hide()
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

    private fun defineInputTypeForValue(paramValue: String) {
        if (!prefs.getBoolean(Prefs.GUESS_INPUT_TYPE, true)) return

        if (paramValue.length > 12) {
            binding.editParamInput.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            binding.editParamInput.setLines(3)
        } else {
            try {
                paramValue.toInt()
                binding.editParamInput.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            } catch (e: Exception) {
                try {
                    paramValue.toDouble()
                    binding.editParamInput.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                } catch (e: Exception) {
                    binding.editParamInput.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                }
            }
        }
    }

    private fun findInfoForParam(KernelParam: KernelParam): String {
        val paramName = KernelParam.name.split(".").last()
        val resId = resources.getIdentifier(
            paramName.replace("-", "_"),
            "string",
            packageName
        )
        val stringRes: String? = if (resId != 0) {
            runCatching {
                getString(resId)
            }.getOrNull()
        } else null

        // Prefer the documented string resource
        if (stringRes != null) return stringRes

        if (!KernelParam.path.startsWith("/")) {
            return stringRes ?: getString(R.string.no_info_available)
        }

        val subdirs = KernelParam.path.split("/")
        if (subdirs.isEmpty() || subdirs.size < 4) {
            return stringRes ?: getString(R.string.no_info_available)
        }

        val rawInputStream: InputStream? = when (subdirs[3]) {
            "abi" -> resources.openRawResource(R.raw.abi)
            "fs" -> resources.openRawResource(R.raw.fs)
            "kernel" -> resources.openRawResource(R.raw.kernel)
            "net" -> resources.openRawResource(R.raw.net)
            "vm" -> resources.openRawResource(R.raw.vm)
            else -> null
        }

        val documentationBuilder = StringBuilder()
        rawInputStream.readLines {
            documentationBuilder.append(it).append("\n")
        }

        val documentation = documentationBuilder.toString()
        if (documentation.isEmpty()) {
            return stringRes ?: getString(R.string.no_info_available)
        }

        val info: String? = runCatching {
            documentation.split("=+".toRegex()).last {
                it.contains("$paramName\n")
            }.split("$paramName\n").last()
        }.getOrNull()

        return if (info.isNullOrEmpty()) getString(R.string.no_info_available) else info
    }

    private suspend fun applyParam(kernelParam: KernelParam) {
        val isEditingSavedParam = intent.getBooleanExtra(
            RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM,
            false
        )

        val newValue = binding.editParamInput.text.toString()
        val newParam = kernelParam.copy().also {
            it.value = newValue
        }

        val feedback =
            when (val result = repository.update(newParam, ParamRepository.SOURCE_RUNTIME)) {
                is ApplyResult.Success -> {
                    setResult(Activity.RESULT_OK)
                    repository.update(newParam, ParamRepository.SOURCE_ROOM)
                    getString(R.string.done)
                }

                is ApplyResult.Failure -> {
                    setResult(Activity.RESULT_CANCELED)
                    getString(R.string.apply_failure_format, result.exception.message)
                }
            }

        if (isEditingSavedParam) {
            toast(feedback)
            finish()
        } else {
            Snackbar.make(binding.editParamApply, feedback, Snackbar.LENGTH_LONG).showAsLight()
        }
    }

    private fun isTaskerInstalled(): Boolean {
        return runCatching {
            packageManager.getPackageInfo("net.dinglisch.android.taskerm", 0)
            true
        }.getOrDefault(false)
    }
}

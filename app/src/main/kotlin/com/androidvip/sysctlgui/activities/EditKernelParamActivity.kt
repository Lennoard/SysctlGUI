package com.androidvip.sysctlgui.activities

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
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.adapters.KernelParamListAdapter
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter
import com.androidvip.sysctlgui.prefs.FavoritePrefs
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.prefs.TaskerPrefs
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_kernel_param.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream

class EditKernelParamActivity : AppCompatActivity() {
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val favoritePrefs by lazy { FavoritePrefs(applicationContext) }
    private val paramPrefs by lazy { Prefs(applicationContext) }
    private var taskerPrefs : TaskerPrefs? = null

    private var kernelParameter: KernelParameter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_kernel_param)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extraParam = KernelParamListAdapter.EXTRA_PARAM
        kernelParameter = intent.getSerializableExtra(extraParam) as KernelParameter?

        if (kernelParameter == null) {
            showInvalidParamError()
        } else {
            if (!kernelParameter!!.hasValidPath() || !kernelParameter!!.hasValidName()) {
                showInvalidParamError()
            } else {
                defineInputTypeForValue(kernelParameter!!.value)
                editParamInput.setText(kernelParameter!!.value)
                Handler().postDelayed({ updateTextUi(kernelParameter!!) }, 100)

                editParamApply.setOnClickListener {
                    GlobalScope.launch {
                        applyParam(kernelParameter!!)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_params, menu)
        menu?.findItem(R.id.action_favorite)?.let {
            kernelParameter?.let {param ->
                if (favoritePrefs.isFavorite(param)) {
                    it.setIcon(R.drawable.ic_favorite_selected)
                } else {
                    it.setIcon(R.drawable.ic_favorite_unselected)
                }
            }
        }

        menu?.findItem(R.id.action_tasker)?.let {
            if (isTaskerInstalled()) {
                it.isVisible = true
                kernelParameter?.let { param ->
                    if (taskerPrefs != null) {
                        if (taskerPrefs!!.isTaskerParam(param)) {
                            it.setIcon(R.drawable.ic_action_tasker_remove)
                        } else {
                            it.setIcon(R.drawable.ic_action_tasker_add)
                        }
                    }

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
                kernelParameter?.let {
                    return if (favoritePrefs.isFavorite(it)) {
                        favoritePrefs.removeParam(it)
                        item.setIcon(R.drawable.ic_favorite_unselected)
                        true
                    } else {
                        favoritePrefs.putParam(it)
                        item.setIcon(R.drawable.ic_favorite_selected)
                        true
                    }
                }
            }

            R.id.action_tasker -> {
                selectTaskerListAsDialog { taskerList ->
                    taskerPrefs = TaskerPrefs(applicationContext, taskerList)
                    kernelParameter?.let { param ->
                        taskerPrefs?.let {
                            if (it.isTaskerParam(param)) {
                                it.removeParam(param)
                                item.setIcon(R.drawable.ic_action_tasker_add)
                                toast(getString(R.string.removed_from_tasker_list, taskerList))
                            } else {
                                it.putParam(param)
                                item.setIcon(R.drawable.ic_action_tasker_remove)
                                toast(getString(R.string.added_to_tasker_list, taskerList))
                            }
                        }
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTextUi(kernelParameter: KernelParameter) {
        val paramName = kernelParameter.name.split(".").last()
        editParamName.text = paramName

        YoYo.with(Techniques.SlideInLeft)
            .duration(600)
            .interpolate(AnimationUtils.loadInterpolator(
                this,
                android.R.anim.accelerate_decelerate_interpolator)
            )
            .playOn(editParamName)

        Handler().postDelayed({
            YoYo.with(Techniques.SlideInLeft)
                .duration(600)
                .interpolate(AnimationUtils.loadInterpolator(
                    this,
                    android.R.anim.accelerate_decelerate_interpolator)
                )
                .playOn(editParamSub)

            editParamSub.text = kernelParameter.name.removeSuffix(paramName).removeSuffix(".")
        }, 100)

        Handler().postDelayed({
            editParamInfo.text = findInfoForParam(kernelParameter)
            YoYo.with(Techniques.ZoomIn)
                .duration(260)
                .interpolate(AnimationUtils.loadInterpolator(
                    this,
                    android.R.anim.accelerate_decelerate_interpolator)
                )
                .playOn(editParamInfo)

            editParamApply.show()
        }, 300)
    }

    private fun showInvalidParamError() {
        editParamErrorText.show()
        editParamScroll.goAway()
        editParamApply.hide()
    }

    private fun selectTaskerListAsDialog(block: (Int) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.select_tasker_list)
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .setSingleChoiceItems(R.array.tasker_lists, -1) { dialog, which ->
                block(which + 1)
                dialog.dismiss()
            }.show()
    }

    private fun defineInputTypeForValue(paramValue: String) {
        if (!prefs.getBoolean(Prefs.GUESS_INPUT_TYPE, true)) return

        if (paramValue.length > 12) {
            editParamInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            editParamInput.setLines(3)
        } else {
            try {
                paramValue.toInt()
                editParamInput.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            } catch (e: Exception) {
                try {
                    paramValue.toDouble()
                    editParamInput.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                } catch (e: Exception) {
                    editParamInput.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                }
            }
        }
    }

    private fun findInfoForParam(kernelParameter: KernelParameter): String {
        val paramName = kernelParameter.name.split(".").last()
        val resId = resources.getIdentifier(
            paramName.replace("-", "_"),
            "string",
            packageName
        )
        val stringRes : String? = if (resId != 0) {
            runCatching {
                getString(resId)
            }.getOrNull()
        } else null

        // Prefer the documented string resource
        if (stringRes != null) return stringRes

        if (!kernelParameter.path.startsWith("/")) {
            return stringRes ?: getString(R.string.no_info_available)
        }

        val subdirs = kernelParameter.path.split("/")
        if (subdirs.isEmpty() || subdirs.size < 4) {
            return stringRes ?: getString(R.string.no_info_available)
        }

        val rawInputStream : InputStream? = when(subdirs[3]) {
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

    private suspend fun applyParam(kernelParameter: KernelParameter) {
        val kernelParamUtils = KernelParamUtils(this.application)
        val useCustomApply = intent.getBooleanExtra(
            RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM,
            false
        )

        val newValue = editParamInput.text.toString()
        val newParam = kernelParameter.copy().also {
            it.value = newValue
        }

        kernelParamUtils.applyParam(newParam, useCustomApply, object : KernelParamUtils.KernelParamApply {
            override fun onEmptyValue() {
                Snackbar.make(editParamApply, R.string.error_empty_input_field, Snackbar.LENGTH_LONG).showAsLight()
            }

            override fun onFeedBack(feedback: String) {
                Snackbar.make(editParamApply, feedback, Snackbar.LENGTH_LONG).showAsLight()
            }

            override fun onSuccess() {
                kernelParameter.value = newValue
            }

            override suspend fun onCustomApply(kernelParam: KernelParameter) {
                val success = paramPrefs.putParam(kernelParam)

                runSafeOnUiThread {
                    if (success) {
                        this@EditKernelParamActivity.toast(R.string.done)
                        setResult(Activity.RESULT_OK)
                    } else {
                        this@EditKernelParamActivity.toast(R.string.failed)
                        setResult(Activity.RESULT_CANCELED)
                    }
                    finish()
                }
            }
        })
    }

    private fun isTaskerInstalled(): Boolean {
        return runCatching {
            packageManager.getPackageInfo("net.dinglisch.android.taskerm", 0)
            true
        }.getOrDefault(false)
    }
}

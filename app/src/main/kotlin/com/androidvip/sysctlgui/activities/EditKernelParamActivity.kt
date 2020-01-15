package com.androidvip.sysctlgui.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.adapters.KernelParamListAdapter
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_kernel_param.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditKernelParamActivity : AppCompatActivity() {
    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_kernel_param)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extraParam = KernelParamListAdapter.EXTRA_PARAM
        val kernelParameter: KernelParameter? = intent.getSerializableExtra(extraParam) as KernelParameter?
        val commitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")

        if (kernelParameter == null) {
            editParamErrorText.show()
            editParamScroll.goAway()
            editParamApply.hide()
        } else {
            if (!kernelParameter.hasValidPath() || !kernelParameter.hasValidName()) {
                editParamErrorText.show()
                editParamScroll.goAway()
                editParamApply.hide()
            } else {
                defineInputTypeForValue(kernelParameter.value)
                editParamInput.setText(kernelParameter.value)
                Handler().postDelayed({ updateTextUi(kernelParameter) }, 100)

                editParamApply.setOnClickListener { view ->
                    val newValue = editParamInput.text.toString()
                    if (!prefs.getBoolean(Prefs.ALLOW_BLANK, false) && newValue.isEmpty()) {
                        Snackbar.make(view,
                            R.string.error_empty_input_field, Snackbar.LENGTH_LONG).showAsLight()
                    } else {
                        kernelParameter.value = newValue
                        GlobalScope.launch(Dispatchers.IO) {
                            val result = commitChanges(kernelParameter)
                            var success = true
                            val feedback = if (commitMode == "sysctl") {
                                if (result == "error" || !result.contains(kernelParameter.name)) {
                                    success = false
                                    getString(R.string.failed)
                                } else {
                                    result
                                }
                            } else {
                                if (result == "error") {
                                    success = false
                                    getString(R.string.failed)
                                } else {
                                    getString(R.string.done)
                                }
                            }

                            if (success) {
                                Prefs.putParam(kernelParameter, this@EditKernelParamActivity)
                            }

                            runSafeOnUiThread {
                                Snackbar.make(view, feedback, Snackbar.LENGTH_LONG).showAsLight()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTextUi(kernelParameter: KernelParameter) {
        val paramName = kernelParameter.name.split(".").last()
        editParamName.text = paramName

        YoYo.with(Techniques.SlideInLeft)
            .duration(600)
            .playOn(editParamName)

        Handler().postDelayed({
            YoYo.with(Techniques.SlideInLeft)
                .duration(600)
                .playOn(editParamSub)

            editParamSub.text = kernelParameter.name.removeSuffix(paramName).removeSuffix(".")
            editParamInfo.text = findInfoForParam(paramName)
        }, 100)

        Handler().postDelayed({ editParamApply.show() }, 300)
    }

    private fun defineInputTypeForValue(paramValue: String) {
        if (!prefs.getBoolean(Prefs.GUESS_INPUT_TYPE, true)) return

        if (paramValue.length > 12) {
            editParamInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            editParamInput.setLines(3)
        } else {
            try {
                paramValue.toInt()
                editParamInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            } catch (e: Exception) {
                try {
                    paramValue.toDouble()
                    editParamInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                } catch (e: Exception) {
                    editParamInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                }
            }
        }
    }

    private fun findInfoForParam(paramName: String): String {
        val resId = resources.getIdentifier(paramName.replace("-", "_"), "string", packageName)
        return if (resId != 0) {
            runCatching {
                getString(resId)
            }.getOrDefault(getString(R.string.no_info_available))
        } else {
            getString(R.string.no_info_available)
        }
    }

    private suspend fun commitChanges(kernelParam: KernelParameter) = withContext(Dispatchers.Default) {
        val commandPrefix = if (prefs.getBoolean(Prefs.USE_BUSYBOX, false)) "busybox " else ""
        val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
            "sysctl" -> "${commandPrefix}sysctl -w ${kernelParam.name}=${kernelParam.value}"
            "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
            else -> "busybox sysctl -w ${kernelParam.name}=${kernelParam.value}"
        }

        RootUtils.executeWithOutput(command, "error")
    }
}

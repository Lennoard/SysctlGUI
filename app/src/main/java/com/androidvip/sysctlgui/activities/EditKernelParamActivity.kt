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
import java.lang.Exception

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
            editParamLayout.goAway()
            editParamApply.hide()
        } else {
            if (!kernelParameter.hasValidPath() || !kernelParameter.hasValidParam()) {
                editParamErrorText.show()
                editParamLayout.goAway()
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
                        GlobalScope.launch {
                            val result = commitChanges(kernelParameter)
                            withContext(Dispatchers.Main) {
                                val feedback = if (commitMode == "sysctl") {
                                    if (result == "error" || !result.contains(kernelParameter.param)) {
                                        getString(R.string.failed)
                                    } else {
                                        result
                                    }
                                } else {
                                    if (result == "error") {
                                        getString(R.string.failed)
                                    } else {
                                        getString(R.string.done)
                                    }
                                }
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
        val paramName = kernelParameter.param.split(".").last()
        editParamName.text = paramName
        editParamSub.text = kernelParameter.param.removeSuffix(paramName).removeSuffix(".")

        YoYo.with(Techniques.SlideInLeft)
            .duration(600)
            .playOn(editParamName)

        Handler().postDelayed({
            YoYo.with(Techniques.SlideInLeft)
                .duration(600)
                .playOn(editParamSub)
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

    private suspend fun commitChanges(kernelParam: KernelParameter) = withContext(Dispatchers.Main) {
        val command = when (prefs.getString(Prefs.COMMIT_MODE, "sysctl")) {
            "sysctl" -> "busybox sysctl -w ${kernelParam.param}=${kernelParam.value}"
            "echo" -> "echo '${kernelParam.value}' > ${kernelParam.path}"
            else -> "busybox sysctl -w ${kernelParam.param}=${kernelParam.value}"
        }
        RootUtils.executeWithOutput(command, "error")
    }
}

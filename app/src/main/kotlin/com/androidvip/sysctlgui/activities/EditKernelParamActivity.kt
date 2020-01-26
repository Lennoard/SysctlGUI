package com.androidvip.sysctlgui.activities

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.adapters.KernelParamListAdapter
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_kernel_param.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditKernelParamActivity : AppCompatActivity() {
    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_kernel_param)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extraParam = KernelParamListAdapter.EXTRA_PARAM
        val kernelParameter: KernelParameter? = intent.getSerializableExtra(extraParam) as KernelParameter?

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

                editParamApply.setOnClickListener {
                    applyParam(kernelParameter)
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

    private fun applyParam(kernelParameter: KernelParameter) {
        val newValue = editParamInput.text.toString()

        val newKernelParameter = kernelParameter.copy().apply {
            value = newValue
        }

        val useCustomApply = intent.getBooleanExtra(RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM, false)

        KernelParamUtils(this).applyParam(newKernelParameter, object : KernelParamUtils.KernelParamApply{
            override fun onEmptyValue() {
                GlobalScope.launch(Dispatchers.IO) {
                    runSafeOnUiThread {
                        Snackbar.make(editParamApply, R.string.error_empty_input_field, Snackbar.LENGTH_LONG).showAsLight()
                    }
                }
            }

            override fun onFeedBack(feedback: String) {
                GlobalScope.launch(Dispatchers.IO) {
                    runSafeOnUiThread {
                        Snackbar.make(editParamApply, feedback, Snackbar.LENGTH_LONG).showAsLight()
                    }
                }
            }

            override fun onCustomApply(kernelParam: KernelParameter) {
                val success = Prefs.putParam(kernelParam, this@EditKernelParamActivity)

                GlobalScope.launch(Dispatchers.IO) {
                    runSafeOnUiThread {
                        if (success) {
                            Toast.makeText(this@EditKernelParamActivity, R.string.done, Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                        } else {
                            Toast.makeText(this@EditKernelParamActivity, R.string.failed, Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_CANCELED)
                        }
                        finish()
                    }
                }
            }

            override fun onSuccess() {
                kernelParameter.value = newValue
            }
        }, useCustomApply)
    }
}

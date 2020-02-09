package com.androidvip.sysctlgui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.androidvip.sysctlgui.*
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    companion object {
        private const val OPEN_FILE_REQUEST_CODE: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mainParamsList.setOnClickListener {
            Intent(this, KernelParamsListActivity::class.java).apply {
                startActivity(this)
            }
        }

        mainParamBrowser.setOnClickListener {
            Intent(this, KernelParamBrowserActivity::class.java).apply {
                startActivity(this)
            }
        }

        mainReadFromFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
        }

        mainAppDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.action_exit -> {
                RootUtils.finishProcess()
                moveTaskToBack(true)
                finish()
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        RootUtils.finishProcess()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            OPEN_FILE_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) return

                data?.data?.let { uri ->
                    val fileExtension = uri.lastPathSegment

                    fileExtension?.let { extension ->
                        if (extension.endsWith(".json") or extension.endsWith(".conf")) {
                            GlobalScope.launch {
                                applyParamsFromUri(uri, extension)
                            }
                        } else {
                            toast(R.string.import_error_invalid_file_type)
                            return
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun applyParamsFromUri(uri: Uri, fileExtension: String) = withContext(Dispatchers.Default) {
        val context = this@MainActivity
        val successfulParams: MutableList<KernelParameter> = mutableListOf()

        fun showResultDialog(message: String, success: Boolean) {
            val dialog = AlertDialog.Builder(context)
                .setIcon(if (success) R.drawable.ic_check else R.drawable.ic_close)
                .setTitle(if (success) R.string.done else R.string.failed)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }

            if (!isFinishing) {
                dialog.show()
            }
        }

        try {
            val kernelParamUtils = KernelParamUtils(context)
            val params: MutableList<KernelParameter>? = when {
                fileExtension.endsWith(".json") -> kernelParamUtils.getParamsFromJsonUri(uri)
                fileExtension.endsWith(".conf") -> kernelParamUtils.getParamsFromConfUri(uri)
                else -> mutableListOf()
            }

            if (params.isNullOrEmpty()) {
                context.toast(R.string.no_parameters_found)
                return@withContext
            }

            params.forEach {
                // apply the param to check if valid
                KernelParamUtils(context).applyParam(it, false, object : KernelParamUtils.KernelParamApply {
                    override fun onEmptyValue() { }
                    override fun onFeedBack(feedback: String) { }

                    override fun onSuccess() {
                        successfulParams.add(it)
                    }

                    override suspend fun onCustomApply(kernelParam: KernelParameter) { }
                })
            }

            val oldParams = Prefs.removeAllParams(context)
            if (Prefs.putParams(successfulParams, context)) {
                runSafeOnUiThread {
                    showResultDialog("${getString(R.string.import_success_message, successfulParams.size)}\n\n $successfulParams", true)
                    context.toast(R.string.done, Toast.LENGTH_LONG)
                }
            } else {
                // Probably an IO error, revert back
                Prefs.putParams(oldParams, context)
                runSafeOnUiThread {
                    showResultDialog("${getString(R.string.restore_parameters)}\n\n $successfulParams", false)
                }
            }
        } catch (e: Exception) {
            runSafeOnUiThread {
                when (e) {
                    is JsonParseException,
                    is JsonSyntaxException -> showResultDialog(getString(R.string.import_error_invalid_json), false)
                    else -> showResultDialog(getString(R.string.import_error), false)
                }
            }
        }
    }
}

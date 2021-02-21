package com.androidvip.sysctlgui.ui

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
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.ui.parambrowser.KernelParamBrowserActivity
import com.androidvip.sysctlgui.ui.paramlist.KernelParamsListActivity
import com.androidvip.sysctlgui.ui.settings.SettingsActivity
import com.androidvip.sysctlgui.utils.KernelParamUtils
import com.androidvip.sysctlgui.utils.RootUtils
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()
    private val paramPrefs by lazy {
        Prefs(applicationContext)
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
            startActivityForResult(intent,
                OPEN_FILE_REQUEST_CODE
            )
        }

        mainFavorites.setOnClickListener {
            Intent(this, ManageFavoritesParamsActivity::class.java).apply {
                startActivity(this)
            }
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
                moveTaskToBack(true)
                finish()
            }
        }

        return true
    }

    override fun onDestroy() {
        RootUtils.finishProcess()
        coroutineContext[Job]?.cancelChildren()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            OPEN_FILE_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) return

                data?.data?.let { uri ->
                    val fileExtension = uri.lastPathSegment

                    fileExtension?.let { extension ->
                        if (extension.endsWith(".json") or extension.endsWith(".conf")) {
                            launch {
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
            val kernelParamUtils =
                KernelParamUtils(application)
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
                kernelParamUtils.applyParam(it, false, object : KernelParamUtils.KernelParamApply {
                    override fun onEmptyValue() { }
                    override fun onFeedBack(feedback: String) { }

                    override fun onSuccess() {
                        successfulParams.add(it)
                    }

                    override suspend fun onCustomApply(kernelParam: KernelParameter) { }
                })
            }

            val oldParams = paramPrefs.removeAllParams()
            if (paramPrefs.putParams(successfulParams)) {
                runSafeOnUiThread {
                    val msg = "${getString(R.string.import_success_message, successfulParams.size)}\n\n ${successfulParams.joinToString()}"
                    showResultDialog(msg, true)
                    context.toast(R.string.done, Toast.LENGTH_LONG)
                }
            } else {
                // Probably an IO error, revert back
                paramPrefs.putParams(oldParams)
                runSafeOnUiThread {
                    val msg = "${getString(R.string.restore_parameters)}\n\n ${successfulParams.joinToString()}"
                    showResultDialog(msg, false)
                }
            }
        } catch (e: Exception) {
            runSafeOnUiThread {
                when (e) {
                    is JsonParseException,
                    is JsonSyntaxException -> {
                        showResultDialog(getString(R.string.import_error_invalid_json), false)
                    }
                    else -> {
                        showResultDialog(getString(R.string.import_error), false)
                    }
                }
            }
        }
    }

    companion object {
        private const val OPEN_FILE_REQUEST_CODE: Int = 1
    }
}

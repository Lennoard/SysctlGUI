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
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.databinding.ActivityMainBinding
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.params.browse.KernelParamBrowserActivity
import com.androidvip.sysctlgui.ui.params.list.KernelParamListActivity
import com.androidvip.sysctlgui.ui.params.user.ManageFavoritesParamsActivity
import com.androidvip.sysctlgui.ui.settings.SettingsActivity
import com.androidvip.sysctlgui.utils.ApplyResult
import com.androidvip.sysctlgui.utils.KernelParamUtils
import com.androidvip.sysctlgui.utils.RootUtils
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val repository: ParamRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.content.mainParamsList.setOnClickListener {
            Intent(this, KernelParamListActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.content.mainParamBrowser.setOnClickListener {
            Intent(this, KernelParamBrowserActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.content.mainReadFromFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(
                intent,
                OPEN_FILE_REQUEST_CODE
            )
        }

        binding.content.mainFavorites.setOnClickListener {
            Intent(this, ManageFavoritesParamsActivity::class.java).apply {
                startActivity(this)
            }
        }

        binding.content.mainAppDescription.movementMethod = LinkMovementMethod.getInstance()
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
                            lifecycleScope.launch {
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

    private suspend fun applyParamsFromUri(uri: Uri, fileExtension: String) {
        val successfulParams: MutableList<KernelParam> = mutableListOf()

        fun showResultDialog(message: String, success: Boolean) {
            val dialog = AlertDialog.Builder(this)
                .setIcon(if (success) R.drawable.ic_check else R.drawable.ic_close)
                .setTitle(if (success) R.string.done else R.string.failed)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }

            if (!isFinishing) {
                dialog.show()
            }
        }

        try {
            val params: MutableList<KernelParam>? = when {
                fileExtension.endsWith(".json") -> {
                    KernelParamUtils.getParamsFromJsonUri(this, uri)
                }
                fileExtension.endsWith(".conf") -> {
                    KernelParamUtils.getParamsFromConfUri(this, uri)
                }
                else -> mutableListOf()
            }

            if (params.isNullOrEmpty()) {
                toast(R.string.no_parameters_found)
                return
            }

            params.forEach {
                // Apply the param to check if valid
                val result = repository.update(it, ParamRepository.SOURCE_RUNTIME)
                if (result == ApplyResult.Success) {
                    successfulParams.add(it)
                }
            }

            repository.clear(ParamRepository.SOURCE_ROOM)
            repository.addParams(successfulParams, ParamRepository.SOURCE_ROOM)
            val msg = "${
                getString(R.string.import_success_message, successfulParams.size)
            }\n\n ${successfulParams.joinToString()}"
            showResultDialog(msg, true)
            toast(R.string.done, Toast.LENGTH_LONG)
        } catch (e: Exception) {
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

    companion object {
        private const val OPEN_FILE_REQUEST_CODE: Int = 1
    }
}

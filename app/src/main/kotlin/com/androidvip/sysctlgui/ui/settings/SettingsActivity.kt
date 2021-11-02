package com.androidvip.sysctlgui.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import com.androidvip.sysctlgui.helpers.Actions
import com.androidvip.sysctlgui.runSafeOnUiThread
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.utils.KernelParamUtils
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {
    private val getUserParamsUseCase: GetUserParamsUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().also {
            it.replace(R.id.settingsFragmentHolder, SettingsFragment())
            it.commit()
        }

        if (intent.action == Actions.ExportParams.name) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "params.json")
            }
            startActivityForResult(
                intent,
                CREATE_FILE_REQUEST_CODE
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_FILE_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    finish()
                    return
                }

                data?.data?.let { uri ->
                    lifecycleScope.launch {
                        val message: String = if (exportParams(uri)) {
                            getString(R.string.done)
                        } else {
                            getString(R.string.failed)
                        }

                        runSafeOnUiThread {
                            toast(message, Toast.LENGTH_LONG)
                            // close the activity else it's there double
                            finish()
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun exportParams(uri: Uri): Boolean {
        val params = getUserParamsUseCase().getOrNull().orEmpty()
        return KernelParamUtils.writeParamsToUri(this, params, uri)
    }

    companion object {
        private const val CREATE_FILE_REQUEST_CODE: Int = 2
    }
}
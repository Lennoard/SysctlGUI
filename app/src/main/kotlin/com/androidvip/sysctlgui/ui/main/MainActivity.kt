package com.androidvip.sysctlgui.ui.main

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
import com.androidvip.sysctlgui.data.models.HomeItem
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.databinding.ActivityMainBinding
import com.androidvip.sysctlgui.domain.models.param.DomainKernelParam
import com.androidvip.sysctlgui.domain.usecase.AddUserParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ApplyParamsUseCase
import com.androidvip.sysctlgui.domain.usecase.ClearUserParamUseCase
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.params.browse.KernelParamBrowserActivity
import com.androidvip.sysctlgui.ui.params.list.KernelParamListActivity
import com.androidvip.sysctlgui.ui.params.user.ManageFavoritesParamsActivity
import com.androidvip.sysctlgui.ui.settings.SettingsActivity
import com.androidvip.sysctlgui.utils.KernelParamUtils
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), HomeItemAdapter.OnHomeItemClickedListener {
    private lateinit var binding: ActivityMainBinding
    private val rootUtils: RootUtils by inject()

    private val applyParamsUseCase: ApplyParamsUseCase by inject()
    private val clearUserParamUseCase: ClearUserParamUseCase by inject()
    private val addUserParamsUseCase: AddUserParamsUseCase by inject()

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val adapter = HomeItemAdapter(this)
        binding.content.recyclerView.adapter = adapter
        adapter.submitList(viewModel.getHomeItems())

        viewModel.viewEffect.observe(this) { viewEffect ->
            when (viewEffect) {
                is MainViewEffect.NavigateToKernelList -> startActivity(
                    Intent(this, KernelParamListActivity::class.java)
                )

                is MainViewEffect.NavigateToKernelBrowser -> startActivity(
                    Intent(this, KernelParamBrowserActivity::class.java)
                )

                is MainViewEffect.ImportParamsFromFile -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    startActivityForResult(
                        intent,
                        OPEN_FILE_REQUEST_CODE
                    )
                }

                is MainViewEffect.NavigateToFavorites -> startActivity(
                    Intent(this, ManageFavoritesParamsActivity::class.java)
                )

                is MainViewEffect.NavigateToSettings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
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

            R.id.action_settings -> viewModel.doWhenSettingsPressed()

            R.id.action_exit -> {
                moveTaskToBack(true)
                finish()
            }
        }

        return true
    }

    override fun onDestroy() {
        rootUtils.finishProcess()
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

    override fun onHomeItemClicked(item: HomeItem, position: Int) {
        when (position) {
            0 -> viewModel.doWhenListPressed()
            1 -> viewModel.doWhenBrowsePressed()
            2 -> viewModel.doWhenImportPressed()
            3 -> viewModel.doWhenFavoritesPressed()
        }
    }

    private suspend fun applyParamsFromUri(uri: Uri, fileExtension: String) {
        val successfulParams: MutableList<DomainKernelParam> = mutableListOf()

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
            val params: MutableList<DomainKernelParam>? = when {
                fileExtension.endsWith(".json") -> {
                    KernelParamUtils.getParamsFromJsonUri(this, uri)
                }
                fileExtension.endsWith(".conf") -> {
                    KernelParamUtils.getParamsFromConfUri(this, uri)
                }
                else -> mutableListOf()
            }?.toMutableList()

            if (params.isNullOrEmpty()) {
                toast(R.string.no_parameters_found)
                return
            }

            params.forEach {
                // Apply the param to check if valid
                val result = applyParamsUseCase.execute(it)
                if (result.isSuccess) {
                    successfulParams.add(it)
                }
            }

            clearUserParamUseCase.execute()
            addUserParamsUseCase.execute(successfulParams)
            val msg = "${
            getString(R.string.import_success_message, successfulParams.size)
            }\n\n ${successfulParams.joinToString()}"
            showResultDialog(msg, true)
            toast(R.string.done, Toast.LENGTH_LONG)
        } catch (e: Exception) {
            e.printStackTrace()
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

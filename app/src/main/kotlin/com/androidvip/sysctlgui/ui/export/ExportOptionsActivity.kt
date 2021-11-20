package com.androidvip.sysctlgui.ui.export

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.SettingsItem
import com.androidvip.sysctlgui.databinding.ActivityExportOptionsBinding
import com.androidvip.sysctlgui.helpers.OnSettingsItemClickedListener
import com.androidvip.sysctlgui.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExportOptionsActivity : AppCompatActivity(), OnSettingsItemClickedListener {
    private lateinit var binding: ActivityExportOptionsBinding
    private val viewModel: ExportOptionsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = ExportOptionsItemAdapter(this)
        binding.recyclerView.adapter = adapter
        adapter.submitList(viewModel.getBackOptionItems())

        observeUi()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return toast(R.string.error)

        when (requestCode) {
            RC_IMPORT_USER_PARAMS,
            RC_RESTORE_PARAMS -> {
                val uri = data?.data ?: return toast(R.string.import_error)
                val extension = uri.lastPathSegment.orEmpty()
                val stream = contentResolver.openInputStream(uri)
                    ?: return toast(R.string.import_error)
                viewModel.importParams(stream, extension)
            }

            RC_EXPORT_USER_PARAMS -> {
                val uri = data?.data ?: return toast(R.string.export_error)
                viewModel.exportParams(uri, this, false)
            }

            RC_BACKUP_PARAMS -> {
                val uri = data?.data ?: return toast(R.string.export_error)
                viewModel.exportParams(uri, this, true)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSettingsItemClicked(item: SettingsItem, position: Int) {
        when (position) {
            0 -> viewModel.doWhenImportUserParamsPressed()
            1 -> viewModel.doWhenExportUserParamsPressed()
            2 -> viewModel.doWhenBackupPressed()
            3 -> viewModel.doWhenRestorePressed()
        }
    }

    private fun observeUi() {
        viewModel.viewEffect.observe(this) {
            when (it) {
                is ExportOptionsViewEffect.ImportUserParams -> requestImportFile(RC_IMPORT_USER_PARAMS)
                is ExportOptionsViewEffect.ExportUserParams -> requestExportFile(RC_EXPORT_USER_PARAMS)
                is ExportOptionsViewEffect.RestoreRuntimeParams -> requestImportFile(RC_RESTORE_PARAMS)
                is ExportOptionsViewEffect.BackupRuntimeParams -> requestExportFile(RC_BACKUP_PARAMS)
                is ExportOptionsViewEffect.ShowImportError -> showErrorDialog(it.messageRes)
                is ExportOptionsViewEffect.ShowImportSuccess -> showSuccessDialog(
                    getString(R.string.import_success_message, it.paramCount)
                )
                is ExportOptionsViewEffect.ShowExportError -> showErrorDialog(it.messageRes)
                is ExportOptionsViewEffect.ShowExportSuccess -> showSuccessDialog(
                    getString(R.string.export_success_message)
                )
            }
        }

        viewModel.viewState.observe(this) {
            binding.progress.visibility = if (it.isLoading) View.VISIBLE else View.GONE
            binding.loadingText.visibility = if (it.isLoading) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (it.isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun requestImportFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, requestCode)
    }

    private fun requestExportFile(requestCode: Int) {
        val extension = if (requestCode == RC_BACKUP_PARAMS) "conf" else "json"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "params.$extension")
        }
        startActivityForResult(intent, requestCode)
    }

    private fun showErrorDialog(@StringRes messageRes: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setMessage(messageRes)
            .setIcon(R.drawable.ic_close)
            .create()
            .also { dialog ->
                if (!isFinishing) dialog.show()
            }
    }

    private fun showSuccessDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.done)
            .setMessage(message)
            .setIcon(R.drawable.ic_check)
            .create()
            .also { dialog ->
                if (!isFinishing) dialog.show()
            }
    }

    companion object {
        private const val RC_IMPORT_USER_PARAMS: Int = 1
        private const val RC_EXPORT_USER_PARAMS: Int = 2
        private const val RC_BACKUP_PARAMS: Int = 3
        private const val RC_RESTORE_PARAMS: Int = 4
    }
}

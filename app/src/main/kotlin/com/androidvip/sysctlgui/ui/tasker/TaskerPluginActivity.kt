package com.androidvip.sysctlgui.ui.tasker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.androidvip.sysctlgui.databinding.ActivityTaskerPluginBinding
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class TaskerPluginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskerPluginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskerPluginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.taskerDoneButton.setOnClickListener {
            val selectedListNumber = binding.taskerListSpinner.selectedItemPosition // 0-based index

            val resultBundle = bundleOf(
                TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER to selectedListNumber,
                TaskerReceiver.EXTRA_STRING_BLURB to binding.taskerListSpinner.selectedItem.toString()
            )

            val resultIntent = Intent().apply {
                putExtra(TaskerReceiver.EXTRA_BUNDLE, resultBundle)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

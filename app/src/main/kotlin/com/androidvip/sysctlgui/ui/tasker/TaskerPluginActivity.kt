package com.androidvip.sysctlgui.ui.tasker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.androidvip.sysctlgui.databinding.ActivityTaskerPluginBinding
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class TaskerPluginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskerPluginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskerPluginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom
            )

            WindowInsetsCompat.CONSUMED
        }

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

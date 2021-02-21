package com.androidvip.sysctlgui.ui.tasker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import kotlinx.android.synthetic.main.activity_tasker_plugin.*

class TaskerPluginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasker_plugin)

        taskerDoneButton.setOnClickListener {
            val selectedListNumber = taskerListSpinner.selectedItemPosition // 0-based index

            val resultBundle = bundleOf(
                TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER to selectedListNumber,
                TaskerReceiver.EXTRA_STRING_BLURB to taskerListSpinner.selectedItem.toString()
            )

            val resultIntent = Intent().apply {
                putExtra(TaskerReceiver.EXTRA_BUNDLE, resultBundle)
            }

            setResult(Activity.RESULT_OK, resultIntent)
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

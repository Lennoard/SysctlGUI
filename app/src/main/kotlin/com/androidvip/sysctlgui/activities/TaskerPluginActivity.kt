package com.androidvip.sysctlgui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.receivers.TaskerReceiver
import kotlinx.android.synthetic.main.activity_tasker_plugin.*

class TaskerPluginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasker_plugin)

        taskerDoneButton.setOnClickListener {
            val selectedListNumber = taskerListSpinner.selectedItemPosition // 0-based index

            val resultBundle = Bundle().apply {
                putInt(TaskerReceiver.BUNDLE_EXTRA_LIST_NUMBER, selectedListNumber)
                putString(TaskerReceiver.EXTRA_STRING_BLURB, taskerListSpinner.selectedItem.toString())
            }

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

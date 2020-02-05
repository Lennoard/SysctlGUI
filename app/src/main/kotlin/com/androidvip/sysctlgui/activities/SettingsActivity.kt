package com.androidvip.sysctlgui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.helpers.Actions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    companion object {
        private const val CREATE_FILE_REQUEST_CODE: Int = 2
    }

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
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            CREATE_FILE_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    finish()
                    return
                }

                data?.data?.let { uri ->

                    GlobalScope.launch(Dispatchers.IO) {
                        val message: String = if (KernelParamUtils(this@SettingsActivity).exportParamsToUri(uri)) {
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

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)

            val currCommitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")
            val commitModePref = findPreference<Preference?>(Prefs.COMMIT_MODE)
            commitModePref?.summary = if (currCommitMode == "sysctl")
                "Use sysctl -w"
            else
                "Use echo 'value' > /proc/sys/…"

            commitModePref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                commitModePref?.summary = if (newValue == "sysctl")
                    "Use sysctl -w"
                else
                    "Use echo 'value' > /proc/sys/…"
                true
            }

            val useBusyboxPref = findPreference(Prefs.USE_BUSYBOX) as SwitchPreferenceCompat?

            GlobalScope.launch(Dispatchers.IO) {
                val isBusyboxAvailable = RootUtils.isBusyboxAvailable()
                activity.runSafeOnUiThread {
                    if (isBusyboxAvailable) {
                        useBusyboxPref?.isEnabled = true
                    } else {
                        useBusyboxPref?.isChecked = false
                        useBusyboxPref?.isEnabled = false
                    }
                }
            }
        }
    }
}
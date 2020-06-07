package com.androidvip.sysctlgui.activities

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
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
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.receivers.BootReceiver
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

            val currCommitMode = prefs.getString(Prefs.COMMIT_MODE, "sysctl")
            val commitModePref = findPreference<Preference?>(Prefs.COMMIT_MODE)
            commitModePref?.summary = if (currCommitMode == "sysctl")
                "Use sysctl -w"
            else
                "Use echo 'value' > /proc/sys/…"

            val startupDelay = prefs.getInt(Prefs.START_UP_DELAY, 0)
            val startupDelayPref = findPreference<Preference?>(Prefs.START_UP_DELAY)

            startupDelayPref?.summary = if (startupDelay > 0) {
                getString(R.string.startup_delay_sum, startupDelay)
            } else {
                getString(R.string.startup_delay_disabled)
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

            commitModePref?.onPreferenceChangeListener = this
            startupDelayPref?.onPreferenceChangeListener = this
            findPreference<Preference?>(Prefs.RUN_ON_START_UP)?.onPreferenceChangeListener = this
        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            when (preference?.key) {
                Prefs.RUN_ON_START_UP -> {
                    val receiver = ComponentName(requireContext(), BootReceiver::class.java)
                    val state = if (newValue == true) {
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    } else {
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    }

                    requireContext().packageManager.setComponentEnabledSetting(
                        receiver,
                        state,
                        PackageManager.DONT_KILL_APP
                    )
                }

                Prefs.COMMIT_MODE -> {
                    preference.summary = if (newValue == "sysctl")
                        "Use sysctl -w"
                    else
                        "Use echo 'value' > /proc/sys/…"
                }

                Prefs.START_UP_DELAY -> {
                    val selectedValue = newValue as Int

                    preference.summary = if (selectedValue > 0) {
                         getString(R.string.startup_delay_sum, selectedValue)
                    } else {
                        getString(R.string.startup_delay_disabled)
                    }
                }
            }

            return true
        }
    }
}
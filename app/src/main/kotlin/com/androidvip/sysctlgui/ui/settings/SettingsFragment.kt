package com.androidvip.sysctlgui.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.helpers.StartUpServiceToggle
import com.androidvip.sysctlgui.utils.Consts
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private val prefs: AppPrefs by inject()
    private val rootUtils: RootUtils by inject()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val currCommitMode = prefs.commitMode
        val commitModePref = findPreference<Preference?>(Consts.Prefs.COMMIT_MODE)
        commitModePref?.summary = if (currCommitMode == "sysctl") {
            "Use sysctl -w"
        } else {
            "Use echo 'value' > /proc/sys/…"
        }

        val startupDelay = prefs.startUpDelay
        val startupDelayPref = findPreference<Preference?>(Consts.Prefs.START_UP_DELAY)

        startupDelayPref?.summary = if (startupDelay > 0) {
            getString(R.string.startup_delay_sum, startupDelay)
        } else {
            getString(R.string.startup_delay_disabled)
        }

        val useBusyboxPref = findPreference(Consts.Prefs.USE_BUSYBOX) as SwitchPreferenceCompat?
        lifecycleScope.launch {
            if (rootUtils.isBusyboxAvailable()) {
                useBusyboxPref?.isEnabled = true
            } else {
                useBusyboxPref?.isChecked = false
                useBusyboxPref?.isEnabled = false
            }
        }

        val dynamicColorsPref = findPreference(Consts.Prefs.DYNAMIC_COLORS) as SwitchPreferenceCompat?
        dynamicColorsPref?.isEnabled = DynamicColors.isDynamicColorAvailable()

        commitModePref?.onPreferenceChangeListener = this
        startupDelayPref?.onPreferenceChangeListener = this
        dynamicColorsPref?.onPreferenceChangeListener = this
        findPreference<Preference?>(Consts.Prefs.RUN_ON_START_UP)?.onPreferenceChangeListener = this
        findPreference<Preference?>(Consts.Prefs.FORCE_DARK_THEME)?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference.key) {
            Consts.Prefs.RUN_ON_START_UP -> {
                StartUpServiceToggle.toggleStartUpService(requireContext(), newValue == true)
                askForNotificationPermission()
            }

            Consts.Prefs.COMMIT_MODE -> {
                preference.summary = if (newValue == "sysctl") {
                    "Use sysctl -w"
                } else {
                    "Use echo 'value' > /proc/sys/…"
                }
            }

            Consts.Prefs.START_UP_DELAY -> {
                val selectedValue = (newValue as? Int) ?: 0

                preference.summary = if (selectedValue > 0) {
                    getString(R.string.startup_delay_sum, selectedValue)
                } else {
                    getString(R.string.startup_delay_disabled)
                }
            }

            Consts.Prefs.FORCE_DARK_THEME -> {
                requireActivity().recreate()
            }

            Consts.Prefs.DYNAMIC_COLORS -> {
                if (newValue == true) {
                    DynamicColors.applyToActivitiesIfAvailable(requireActivity().application)
                }
                requireActivity().recreate()
            }
        }

        return true
    }

    private fun askForNotificationPermission() {
        val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!manager.areNotificationsEnabled()) {
                notificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
}

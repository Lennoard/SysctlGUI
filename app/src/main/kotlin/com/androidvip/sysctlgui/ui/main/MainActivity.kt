package com.androidvip.sysctlgui.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.databinding.ActivityMain2Binding
import com.androidvip.sysctlgui.helpers.Actions

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setUpNavigation()
        navigateFromIntent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.action_exit -> {
                moveTaskToBack(true)
                finish()
            }
        }

        return false // Let fragments have a chance to consume it
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setUpNavigation() = with(binding) {
        val navController = navHost.navController
        val defaultIds = setOf(
            R.id.navigationBrowse,
            R.id.navigationList,
            R.id.navigationExport,
            R.id.navigationSettings
        )
        val appBarConfiguration = AppBarConfiguration(defaultIds)

        toolbar.setupWithNavController(navController, appBarConfiguration)
        navView?.setupWithNavController(navController)
        navRail?.setupWithNavController(navController)
    }

    private fun navigateFromIntent() {
        val fragmentName = intent.getStringExtra(EXTRA_DESTINATION) ?: return
        when (fragmentName) {
            Actions.BrowseParams.name -> R.id.navigationBrowse
            Actions.ListParams.name -> R.id.navigationList
            Actions.ExportParams.name -> R.id.navigationExport
            Actions.OpenSettings.name -> R.id.navigationSettings
            else -> null
        }?.let { id ->
            navHost.navController.navigate(id)
        }
    }

    private val navHost: NavHostFragment
        get() = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

    companion object {
        internal const val EXTRA_DESTINATION = "destination"
    }
}

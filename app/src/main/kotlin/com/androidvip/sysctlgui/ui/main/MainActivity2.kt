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

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setUpNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.action_exit -> {
                moveTaskToBack(true)
                finish()
            }
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHost.navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setUpNavigation() = with(binding) {
        val navController = navHost.navController
        val defaultIds = setOf(R.id.navigationBrowse, R.id.navigationExport, R.id.navigationSettings)
        val appBarConfiguration = AppBarConfiguration(defaultIds)

        toolbarLayout.setupWithNavController(toolbar, navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private val navHost: NavHostFragment
        get() = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
}

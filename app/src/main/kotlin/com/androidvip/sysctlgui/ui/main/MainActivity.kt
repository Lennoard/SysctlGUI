package com.androidvip.sysctlgui.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.SettingsItem
import com.androidvip.sysctlgui.data.utils.RootUtils
import com.androidvip.sysctlgui.databinding.ActivityMainBinding
import com.androidvip.sysctlgui.domain.repository.AppPrefs
import com.androidvip.sysctlgui.domain.usecase.PerformDatabaseMigrationUseCase
import com.androidvip.sysctlgui.helpers.OnSettingsItemClickedListener
import com.androidvip.sysctlgui.ui.export.ExportOptionsActivity
import com.androidvip.sysctlgui.ui.params.list.KernelParamListActivity
import com.androidvip.sysctlgui.ui.params.user.ManageFavoritesParamsActivity
import com.androidvip.sysctlgui.ui.settings.SettingsActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), OnSettingsItemClickedListener {
    private lateinit var binding: ActivityMainBinding
    private val rootUtils: RootUtils by inject()
    private val viewModel: MainViewModel by viewModel()
    private val prefs: AppPrefs by inject()
    private val performDatabaseMigrationUseCase: PerformDatabaseMigrationUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val adapter = HomeItemAdapter(this)
        binding.content.recyclerView.adapter = adapter
        adapter.submitList(viewModel.getHomeItems())

        observeUi()

        binding.content.mainAppDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.action_settings -> viewModel.doWhenSettingsPressed()

            R.id.action_exit -> {
                moveTaskToBack(true)
                finish()
            }
        }

        return true
    }

    override fun onDestroy() {
        rootUtils.finishProcess()
        super.onDestroy()
    }

    override fun onSettingsItemClicked(item: SettingsItem, position: Int) {
        when (position) {
            0 -> viewModel.doWhenListPressed()
            1 -> viewModel.doWhenBrowsePressed()
            2 -> viewModel.doWhenImportPressed()
            3 -> viewModel.doWhenFavoritesPressed()
        }
    }

    private fun observeUi() = viewModel.viewEffect.observe(this) { viewEffect ->
        when (viewEffect) {
            is MainViewEffect.NavigateToKernelList -> startActivity(
                Intent(this, KernelParamListActivity::class.java)
            )

            is MainViewEffect.ExportParams -> {
                startActivity(Intent(this, ExportOptionsActivity::class.java))
            }

            is MainViewEffect.NavigateToFavorites -> startActivity(
                Intent(this, ManageFavoritesParamsActivity::class.java)
            )

            is MainViewEffect.NavigateToSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            else -> {}
        }
    }
}

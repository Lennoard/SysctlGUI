package com.androidvip.sysctlgui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.activities.base.BaseSearchActivity
import com.androidvip.sysctlgui.adapters.RemovableParamAdapter
import com.androidvip.sysctlgui.helpers.SwipeToDeleteCallback
import com.androidvip.sysctlgui.runSafeOnUiThread
import com.androidvip.sysctlgui.showAsLight
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_manage_apply_on_startup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageOnStartUpParamsActivity : BaseSearchActivity() {

    private val removableParamAdapter: RemovableParamAdapter by lazy {
        RemovableParamAdapter(this, mutableListOf())
    }

    override fun onQueryTextChanged() {
        updateRecyclerViewData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_apply_on_startup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        applyOnStartUpSwipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { updateRecyclerViewData() }
            isRefreshing = true
        }

        applyOnStartUpRecyclerView.apply {
            addItemDecoration(DividerItemDecoration(this.context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)
            applyOnStartUpRecyclerView.adapter = removableParamAdapter

            val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(removableParamAdapter))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    override fun onStart() {
        super.onStart()
        updateRecyclerViewData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateRecyclerViewData() {
        val snackbar = Snackbar.make(applyOnStartUpRecyclerView,
            R.string.no_parameters_found,
            Snackbar.LENGTH_INDEFINITE
        )

        GlobalScope.launch {
            var kernelParams = getSavedKernelParams()

            if (searchExpression.isNotEmpty()) {
                kernelParams = kernelParams.filter { kernelParameter ->
                    kernelParameter.name.toLowerCase(defaultLocale)
                        .replace(".", "")
                        .contains(searchExpression.toLowerCase(defaultLocale))
                }.toMutableList()
            }

            runSafeOnUiThread {
                applyOnStartUpSwipeLayout.isRefreshing = false
                removableParamAdapter.updateData(kernelParams)

                if (kernelParams.isEmpty()) {
                    snackbar.showAsLight()
                } else {
                    snackbar.dismiss()
                }
            }
        }
    }

    private suspend fun getSavedKernelParams() = withContext(Dispatchers.IO) {
        Prefs.getUserParamsSet(this@ManageOnStartUpParamsActivity)
    }
}

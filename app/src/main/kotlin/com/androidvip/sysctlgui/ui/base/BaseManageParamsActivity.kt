package com.androidvip.sysctlgui.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.helpers.RemovableParamAdapter
import com.androidvip.sysctlgui.helpers.SwipeToDeleteCallback
import com.androidvip.sysctlgui.prefs.base.BasePrefs
import com.androidvip.sysctlgui.showAsLight
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_manage_param_set.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

abstract class BaseManageParamsActivity: BaseSearchActivity() {

    val prefs: BasePrefs by lazy {
        setPrefs()
    }

    private val removableParamAdapter: RemovableParamAdapter by lazy {
        RemovableParamAdapter(
            WeakReference(this),
            mutableListOf(),
            prefs
        )
    }

    override fun onQueryTextChanged() {
        updateRecyclerViewData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_param_set)
        afterSetContentView()
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

        launch {
            var kernelParams = getSavedKernelParams()

            if (searchExpression.isNotEmpty()) {
                withContext(Dispatchers.Default) {
                    kernelParams = kernelParams.filter { kernelParameter ->
                        kernelParameter.name.toLowerCase(defaultLocale)
                            .replace(".", "")
                            .contains(searchExpression.toLowerCase(defaultLocale))
                    }.toMutableList()
                }
            }

            applyOnStartUpSwipeLayout.isRefreshing = false
            removableParamAdapter.updateData(kernelParams)

            if (kernelParams.isEmpty()) {
                snackbar.showAsLight()
            } else {
                snackbar.dismiss()
            }
        }
    }

    private fun afterSetContentView()  {
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

    // todo abstract here
    private suspend fun getSavedKernelParams() = withContext(Dispatchers.IO) {
        prefs.getUserParamsSet()
    }

    abstract fun setPrefs(): BasePrefs
}

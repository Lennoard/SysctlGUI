package com.androidvip.sysctlgui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.androidvip.sysctlgui.KernelParameter
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.RootUtils
import com.androidvip.sysctlgui.activities.base.BaseSearchActivity
import com.androidvip.sysctlgui.adapters.KernelParamListAdapter
import com.androidvip.sysctlgui.runSafeOnUiThread
import kotlinx.android.synthetic.main.activity_kernel_params_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KernelParamsListActivity : BaseSearchActivity() {

    private val paramsListAdapter: KernelParamListAdapter by lazy {
        KernelParamListAdapter(this, mutableListOf())
    }

    override fun onQueryTextChanged() {
        updateRecyclerViewData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kernel_params_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        paramsListSwipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { updateRecyclerViewData() }
            isRefreshing = true
        }

        paramsListRecyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this.context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)
            paramsListRecyclerView.adapter = paramsListAdapter
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
        GlobalScope.launch {
            var kernelParams = getKernelParams()

            if (searchExpression.isNotEmpty()) {
                kernelParams = kernelParams.filter { kernelParameter ->
                    kernelParameter.name.toLowerCase(defaultLocale)
                        .replace(".", "")
                        .contains(searchExpression.toLowerCase(defaultLocale))
                }.toMutableList()
            }

            runSafeOnUiThread {
                paramsListSwipeLayout.isRefreshing = false
                paramsListAdapter.updateData(kernelParams)
            }
        }
    }

    private suspend fun getKernelParams() = withContext(Dispatchers.IO) {
        val kernelParams = mutableListOf<KernelParameter>()
        val command = if (RootUtils.isBusyboxAvailable()) "busybox sysctl -a" else "sysctl -a"
        RootUtils.executeWithOutput(command, "") { line ->
            line?.let {
                if (!it.contains("denied") && !it.startsWith("sysctl") && it.contains("=")) {
                    val paramName = it.split("=").first().trim()
                    kernelParams.add(KernelParameter(name = paramName).apply {
                        setPathFromName(paramName)
                    })
                }
            }
        }
        kernelParams
    }
}

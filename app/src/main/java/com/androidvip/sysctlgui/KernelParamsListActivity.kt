package com.androidvip.sysctlgui

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_kernel_params_list.*
import kotlinx.coroutines.*

class KernelParamsListActivity : AppCompatActivity() {
    private val paramsListAdapter: KernelParamListAdapter by lazy { KernelParamListAdapter(this, mutableListOf()) }

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

        updateRecyclerViewData()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateRecyclerViewData() {
        GlobalScope.launch {
            val kernelParams = getKernelParams()

            withContext(Dispatchers.Main) {
                paramsListSwipeLayout.isRefreshing = false
                paramsListAdapter.updateData(kernelParams)
            }
        }
    }

    private suspend fun getKernelParams() = withContext(Dispatchers.Default) {
        delay(500)
        val kernelParams = mutableListOf<KernelParam>()
        RootUtils.executeWithOutput("sysctl -a", "", true) { line ->
            line?.let {
                if (!it.contains("denied") && !it.startsWith("sysctl") && it.contains("=")) {
                    val kernelParam = it.split("=").first().trim()
                    kernelParams.add(KernelParam(param = kernelParam).apply {
                        setPathFromParam(kernelParam)
                    })
                }
            }
        }
        println(kernelParams)
        kernelParams
    }

    private val recyclerViewColumns: Int
        get() {
            val isLandscape = resources.getBoolean(R.bool.is_landscape)
            return if (isLandscape) 2 else 1
        }
}

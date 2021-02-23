package com.androidvip.sysctlgui.ui.params.list

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ActivityKernelParamsListBinding
import com.androidvip.sysctlgui.helpers.RemovableParamAdapter
import com.androidvip.sysctlgui.ui.base.BaseSearchActivity
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.ParamsViewModel
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class KernelParamListActivity : BaseSearchActivity(), OnParamItemClickedListener {
    private lateinit var binding: ActivityKernelParamsListBinding
    private val paramViewModel: ParamsViewModel by inject()
    private val paramsListAdapter: KernelParamListAdapter by lazy {
        KernelParamListAdapter(this, lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKernelParamsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.swipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { refreshList() }
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this.context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)
            adapter = paramsListAdapter
        }

        paramViewModel.loading.observe(this, Observer {
            binding.swipeLayout.isRefreshing = it
        })

        paramViewModel.kernelParams.observe(this, Observer {
            lifecycleScope.launch {
                paramsListAdapter.updateData(filterList(it))
            }
        })

        refreshList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onParamItemClicked(param: KernelParam) {
        Intent(this, EditKernelParamActivity::class.java).apply {
            putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
            startActivity(this)
        }
    }

    override fun onQueryTextChanged() {
        refreshList()
    }

    private fun refreshList() = lifecycleScope.launch {
        paramViewModel.getKernelParams()
    }

    private suspend fun filterList(list: List<KernelParam>) = withContext(Dispatchers.Default) {
        if (searchExpression.isEmpty()) return@withContext list.toMutableList()

        return@withContext list.filter { param ->
            param.name.toLowerCase(defaultLocale)
                .replace(".", "")
                .contains(searchExpression.toLowerCase(defaultLocale))
        }.toMutableList()
    }

}

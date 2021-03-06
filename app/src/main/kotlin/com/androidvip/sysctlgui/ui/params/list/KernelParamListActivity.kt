package com.androidvip.sysctlgui.ui.params.list

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ActivityKernelParamsListBinding
import com.androidvip.sysctlgui.ui.settings.RemovableParamAdapter
import com.androidvip.sysctlgui.ui.base.BaseSearchActivity
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class KernelParamListActivity : BaseSearchActivity(), OnParamItemClickedListener {
    private lateinit var binding: ActivityKernelParamsListBinding
    private val paramViewModel: ListParamsViewModel by inject()
    private val paramsListAdapter: KernelParamListAdapter by lazy {
        KernelParamListAdapter(this)
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

        paramViewModel.viewState.observe(this) { state ->
            lifecycleScope.launch {
                binding.swipeLayout.isRefreshing = state.isLoading
                paramsListAdapter.updateData(filterList(state.data))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onParamItemClicked(param: KernelParam, itemLayout: View) {
        val sharedElements = arrayOf(
            Pair<View, String>(
                itemLayout.findViewById(R.id.paramName),
                EditKernelParamActivity.NAME_TRANSITION_NAME
            ),
            Pair<View, String>(
                itemLayout.findViewById(R.id.paramValue),
                EditKernelParamActivity.VALUE_TRANSITION_NAME
            )
        )
        val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            *sharedElements,
        )
        Intent(this, EditKernelParamActivity::class.java).apply {
            putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
            startActivity(this, options.toBundle())
        }
    }

    override fun onQueryTextChanged() {
        refreshList()
    }

    private fun refreshList() = paramViewModel.getKernelParams()

    private suspend fun filterList(list: List<KernelParam>) = withContext(Dispatchers.Default) {
        if (searchExpression.isEmpty()) return@withContext list.toMutableList()

        return@withContext list.filter { param ->
            param.name.toLowerCase(defaultLocale)
                .replace(".", "")
                .contains(searchExpression.toLowerCase(defaultLocale))
        }.toMutableList()
    }

}

package com.androidvip.sysctlgui.ui.params.list

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ActivityKernelParamsListBinding
import com.androidvip.sysctlgui.getColorRoles
import com.androidvip.sysctlgui.ui.base.BaseSearchActivity
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import androidx.core.util.Pair as PairUtil

// TODO: Improve by delegating any non-presentation logic to a view model
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.swipeLayout.apply {
            val roles = getColorRoles()
            setColorSchemeColors(roles.accent)
            setProgressBackgroundColorSchemeColor(roles.accentContainer)
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
        val sharedElements = arrayOf<PairUtil<View, String>>(
            PairUtil(
                itemLayout.findViewById(R.id.paramName),
                EditKernelParamActivity.NAME_TRANSITION_NAME
            ),
            PairUtil(
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
            param.name.lowercase(defaultLocale)
                .replace(".", "")
                .contains(searchExpression.lowercase(defaultLocale))
        }.toMutableList()
    }
}

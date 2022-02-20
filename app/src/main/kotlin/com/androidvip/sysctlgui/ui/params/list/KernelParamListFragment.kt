package com.androidvip.sysctlgui.ui.params.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.FragmentKernelParamListBinding
import com.androidvip.sysctlgui.domain.models.ViewState
import com.androidvip.sysctlgui.getColorRoles
import com.androidvip.sysctlgui.ui.base.BaseSearchFragment
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.util.Pair as PairUtil

class KernelParamListFragment :
    BaseSearchFragment<FragmentKernelParamListBinding>(FragmentKernelParamListBinding::inflate),
    OnParamItemClickedListener {

    private val paramViewModel: ListParamsViewModel by viewModel()
    private val paramsListAdapter: KernelParamListAdapter by lazy {
        KernelParamListAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        paramViewModel.viewState.observe(viewLifecycleOwner, ::renderState)
    }

    override fun onStart() {
        super.onStart()
        refreshList()
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
            requireActivity(),
            *sharedElements
        )
        Intent(requireContext(), EditKernelParamActivity::class.java).apply {
            putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
            startActivity(this, options.toBundle())
        }
    }

    private fun refreshList() = paramViewModel.requestKernelParams()

    private fun renderState(state: ViewState<KernelParam>) {
        binding.swipeLayout.isRefreshing = state.isLoading
        paramsListAdapter.updateData(state.data)
    }

    override fun onQueryTextChanged() {
        paramViewModel.setSearchExpression(searchExpression)
        refreshList()
    }
}

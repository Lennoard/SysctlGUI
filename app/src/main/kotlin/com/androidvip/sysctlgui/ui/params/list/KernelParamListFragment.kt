package com.androidvip.sysctlgui.ui.params.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.FragmentKernelParamListBinding
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.ui.base.BaseSearchFragment
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class KernelParamListFragment : BaseSearchFragment<FragmentKernelParamListBinding>(
    FragmentKernelParamListBinding::inflate
) {
    private val viewModel: ListParamsViewModel by viewModel()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SysctlGuiTheme {
                    val state by viewModel.uiState.collectAsState()
                    val refreshing = state.isLoading
                    val refreshState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = { refreshList() }
                    )

                    Box(Modifier.pullRefresh(refreshState)) {
                        if (state.showEmptyState) {
                            EmptyState()
                        } else {
                            KernelParamsList(state.data)
                        }

                        PullRefreshIndicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            refreshing = refreshing,
                            state = refreshState,
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.effect.collect(::processEffect)
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main_search, menu)
        setUpSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_favorites -> findNavController().navigate(R.id.navigateFavoritesParams)
            else -> return false
        }

        return true
    }

    override fun onQueryTextChanged() {
        viewModel.processEvent(ParamViewEvent.OnSearchExpressionChanged(searchExpression))
    }

    private fun onParamItemClicked(param: KernelParam) {
        Intent(requireContext(), EditKernelParamActivity::class.java).apply {
            putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
            startActivity(this)
        }
    }

    private fun refreshList() {
        viewModel.processEvent(ParamViewEvent.OnRefreshRequested)
    }

    private fun processEffect(effect: ParamViewEffect) {
        when (effect) {
            is ParamViewEffect.OpenParamDetails -> onParamItemClicked(effect.param)
        }
    }

    @Composable
    private fun KernelParamsList(params: List<KernelParam>) {
        LazyColumn {
            itemsIndexed(params) { index, param ->
                ParamItem(
                    onParamClick = { viewModel.processEvent(ParamViewEvent.OnParamClicked(param)) },
                    param = param
                )
                if (index < params.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }
            }
        }
    }

    @Composable
    private fun EmptyState() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.error),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(id = R.string.no_parameters_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

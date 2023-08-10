package com.androidvip.sysctlgui.ui.params.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.utils.ComposeTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class BaseManageParamsActivity : ComponentActivity() {
    private val viewModel: UserParamsViewModel by viewModel()
    abstract val filterPredicate: (KernelParam) -> Boolean
    abstract val title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                UserParamsScreen(
                    topBarTitle = title,
                    params = state.params,
                    searchViewVisible = state.searchViewVisible,
                    onQueryChanged = {
                        viewModel.onEvent(UserParamsViewEvent.SearchQueryChanged(it))
                    },
                    onSearch = { viewModel.onEvent(UserParamsViewEvent.SearchPressed) },
                    onSearchPressed = { viewModel.onEvent(UserParamsViewEvent.SearchViewPressed) },
                    onSearchClose = { viewModel.onEvent(UserParamsViewEvent.CloseSearchPressed) },
                    onParamClicked = { startActivity(EditKernelParamActivity.getIntent(this, it)) },
                    onDelete = { viewModel.onEvent(UserParamsViewEvent.DeleteSwipe(it)) },
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.setBaseFilterPredicate(filterPredicate)
        viewModel.onEvent(UserParamsViewEvent.ParamsRequested)
    }
}

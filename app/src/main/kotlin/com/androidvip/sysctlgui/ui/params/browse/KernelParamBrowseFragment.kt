package com.androidvip.sysctlgui.ui.params.browse

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme
import com.androidvip.sysctlgui.getColorRoles
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.show
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.base.BaseSearchFragment
import com.androidvip.sysctlgui.ui.params.EmptyParamsWarning
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.utils.Consts
import com.google.android.material.color.MaterialColors
import java.io.File
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class KernelParamBrowseFragment : BaseSearchFragment(), OnParamItemClickedListener {
    private var actionBarMenu: Menu? = null
    private val viewModel: BrowseParamsViewModel by inject()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SysctlGuiTheme {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    val refreshing = state.isLoading
                    val refreshState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = { refresh() }
                    )

                    actionBarMenu
                        ?.findItem(R.id.action_documentation)
                        ?.isVisible = state.showDocumentationMenu

                    Box(Modifier.pullRefresh(refreshState)) {
                        if (state.showEmptyState) {
                            EmptyParamsWarning()
                        } else {
                            KernelParamsExplorer(state.data)
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
            viewModel.onEvent(ParamBrowserViewEvent.DirectoryChanged(File(Consts.PROC_SYS)))
            viewModel.effect.collect(::handleViewEffect)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentPath = viewModel.currentState.currentPath
                    if (currentPath == Consts.PROC_SYS) {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    } else {
                        onDirectoryChanged(
                            File(currentPath).parentFile ?: File(Consts.PROC_SYS)
                        )
                    }
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse_params, menu)
        actionBarMenu = menu

        setUpSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_documentation -> {
                viewModel.onEvent(ParamBrowserViewEvent.DocumentationMenuClicked)
            }
            R.id.action_favorites -> {
                viewModel.onEvent(ParamBrowserViewEvent.FavoritesMenuClicked)
            }
            else -> return false
        }

        return true
    }

    override fun onQueryTextChanged() {
        viewModel.onEvent(ParamBrowserViewEvent.SearchExpressionChanged(searchExpression))
    }

    override fun onParamItemClicked(param: KernelParam, itemLayout: View) {
        viewModel.onEvent(ParamBrowserViewEvent.ParamClicked(param))
    }

    private fun onDirectoryChanged(newDir: File) {
        viewModel.onEvent(ParamBrowserViewEvent.DirectoryChanged(newDir))
        resetSearchExpression()
    }

    private fun handleViewEffect(viewEffect: ParamBrowserViewEffect) {
        when (viewEffect) {
            is ParamBrowserViewEffect.NavigateToParamDetails -> {
                navigateToParamDetails(viewEffect.param)
            }
            is ParamBrowserViewEffect.NavigateToFavorite -> {
                findNavController().navigate(R.id.navigateFavoritesParams)
            }
            is ParamBrowserViewEffect.OpenDocumentationUrl -> openDocumentationUrl(viewEffect.url)
            is ParamBrowserViewEffect.ShowToast -> toast(viewEffect.stringRes)
        }
    }

    private fun navigateToParamDetails(param: KernelParam) {
        startActivity(EditKernelParamActivity.getIntent(requireContext(), param))
    }

    private fun refresh() {
        viewModel.onEvent(ParamBrowserViewEvent.RefreshRequested)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openDocumentationUrl(url: String) {
        if (!isAdded) return

        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_web)
            setCancelable(true)
        }

        val progressBar: ProgressBar = dialog.findViewById(R.id.webDialogProgress)
        val swipeLayout: SwipeRefreshLayout = dialog.findViewById(R.id.webDialogSwipeLayout)

        val webView = dialog.findViewById<WebView>(R.id.webDialogWebView).apply {
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }

            loadUrl(url)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    swipeLayout.isRefreshing = false

                    val containerColorInt = MaterialColors.getColor(
                        swipeLayout,
                        R.attr.colorPrimaryContainer
                    )
                    val colorInt = MaterialColors.getColor(
                        swipeLayout,
                        R.attr.colorOnPrimaryContainer
                    )

                    val containerColorHex = "#%06X".format(0xFFFFFF and containerColorInt)
                    val colorHex = "#%06X".format(0xFFFFFF and colorInt)
                    // Change webView background and text color to match the app theme
                    view.loadUrl(
                        """
                        |javascript:(
                            |function() { 
                                |document.querySelector('body').style.color='$containerColorHex'; 
                                |document.querySelector('body').style.background='$colorHex';
                            |}
                        |)()
                        """.trimMargin()
                    )
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, progress: Int) {
                    progressBar.progress = progress
                    if (progress == 100) {
                        progressBar.goAway()
                        swipeLayout.isRefreshing = false
                    } else {
                        progressBar.show()
                    }
                }
            }
        }

        swipeLayout.apply {
            val roles = getColorRoles()
            setColorSchemeColors(roles.accent)
            setProgressBackgroundColorSchemeColor(roles.accentContainer)

            setOnRefreshListener { webView.reload() }
        }

        dialog.show()
    }

    @Composable
    private fun KernelParamsExplorer(params: List<KernelParam>) {
        LazyColumn {
            itemsIndexed(params) { index, param ->
                ParamBrowseItem(
                    onParamClick = {
                        viewModel.onEvent(ParamBrowserViewEvent.ParamClicked(param))
                    },
                    onDirectoryChanged = {
                        viewModel.onEvent(ParamBrowserViewEvent.DirectoryChanged(it))
                    },
                    param = param,
                    paramFile = File(param.path)
                )
                if (index < params.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }
            }
        }
    }
}

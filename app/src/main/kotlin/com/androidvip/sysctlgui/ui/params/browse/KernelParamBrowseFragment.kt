package com.androidvip.sysctlgui.ui.params.browse

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.FragmentKernelParamBrowserBinding
import com.androidvip.sysctlgui.domain.Consts
import com.androidvip.sysctlgui.getColorRoles
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.show
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.base.BaseSearchFragment
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.androidvip.sysctlgui.ui.params.user.RemovableParamAdapter
import com.google.android.material.color.MaterialColors
import org.koin.android.ext.android.inject
import java.io.File

class KernelParamBrowseFragment :
    BaseSearchFragment<FragmentKernelParamBrowserBinding>(FragmentKernelParamBrowserBinding::inflate),
    OnParamItemClickedListener,
    DirectoryChangedListener {

    private var actionBarMenu: Menu? = null
    private val viewModel: BrowseParamsViewModel by inject()
    private val paramsBrowserAdapter: KernelParamBrowserAdapter by lazy {
        KernelParamBrowserAdapter(this, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            viewState.observe(viewLifecycleOwner, ::renderState)
            viewEffect.observe(viewLifecycleOwner, ::handleViewEffect)
            setPath(Consts.PROC_SYS)
        }

        binding.swipeLayout.apply {
            val roles = getColorRoles()
            setColorSchemeColors(roles.accent)
            setProgressBackgroundColorSchemeColor(roles.accentContainer)
            setOnRefreshListener { refresh() }
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this.context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)
            adapter = paramsBrowserAdapter
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentPath = viewModel.viewState.value?.currentPath.orEmpty()
                    if (currentPath == Consts.PROC_SYS) {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
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
            R.id.action_documentation -> viewModel.doWhenDocumentationMenuClicked()
            R.id.action_favorites -> viewModel.doWhenFavoritesMenuClicked()
            else -> return false
        }

        return true
    }

    override fun onQueryTextChanged() {
        viewModel.setSearchExpression(searchExpression)
        refresh()
    }

    override fun onParamItemClicked(param: KernelParam, itemLayout: View) {
        viewModel.doWhenParamItemClicked(param, itemLayout, requireActivity())
    }

    override fun onDirectoryChanged(newDir: File) {
        viewModel.doWhenDirectoryChanges(newDir)
        resetSearchExpression()
    }

    private fun renderState(state: ParamBrowserViewState) {
        actionBarMenu?.findItem(R.id.action_documentation)?.isVisible = state.showDocumentationMenu
        binding.swipeLayout.isRefreshing = state.isLoading

        paramsBrowserAdapter.updateData(state.data)
    }

    private fun handleViewEffect(viewEffect: ParamBrowserViewEffect) {
        when (viewEffect) {
            is ParamBrowserViewEffect.NavigateToParamDetails -> {
                navigateToParamDetails(viewEffect.param, viewEffect.options)
            }
            is ParamBrowserViewEffect.NavigateToFavorite -> {
                findNavController().navigate(R.id.navigateFavoritesParams)
            }
            is ParamBrowserViewEffect.OpenDocumentationUrl -> openDocumentationUrl(viewEffect.url)
            is ParamBrowserViewEffect.ShowToast -> toast(viewEffect.stringRes)
        }
    }

    private fun navigateToParamDetails(param: KernelParam, options: ActivityOptionsCompat) {
        Intent(requireContext(), EditKernelParamActivity::class.java).apply {
            putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
            startActivity(this, options.toBundle())
        }
    }

    private fun refresh() = viewModel.setPath(viewModel.viewState.value?.currentPath.orEmpty())

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
                        swipeLayout, R.attr.colorPrimaryContainer
                    )
                    val colorInt = MaterialColors.getColor(
                        swipeLayout, R.attr.colorOnPrimaryContainer
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
                        |)()""".trimMargin()
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
}

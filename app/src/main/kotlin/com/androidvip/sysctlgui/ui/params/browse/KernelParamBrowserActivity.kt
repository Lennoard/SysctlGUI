package com.androidvip.sysctlgui.ui.params.browse

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ActivityKernelParamBrowserBinding
import com.androidvip.sysctlgui.goAway
import com.androidvip.sysctlgui.ui.settings.RemovableParamAdapter
import com.androidvip.sysctlgui.prefs.Prefs
import com.androidvip.sysctlgui.show
import com.androidvip.sysctlgui.toast
import com.androidvip.sysctlgui.ui.base.BaseSearchActivity
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File

class KernelParamBrowserActivity : BaseSearchActivity(),
    DirectoryChangedListener,
    OnParamItemClickedListener
{
    private lateinit var binding: ActivityKernelParamBrowserBinding
    private var actionBarMenu: Menu? = null
    private var documentationUrl = "https://www.kernel.org/doc/Documentation"
    private var currentPath = "/proc/sys"
    private val paramViewModel: BrowseParamsViewModel by inject()
    private val prefs: SharedPreferences by inject()
    private val paramsBrowserAdapter: KernelParamBrowserAdapter by lazy {
        KernelParamBrowserAdapter(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKernelParamBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        paramViewModel.listFoldersFirst = prefs.getBoolean(Prefs.LIST_FOLDERS_FIRST, true)

        binding.swipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent, R.color.colorAccentLight)
            setOnRefreshListener { refreshList() }
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this.context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)
            adapter = paramsBrowserAdapter
        }

        paramViewModel.viewState.observe(this) { state ->
            lifecycleScope.launch {
                binding.swipeLayout.isRefreshing = state.isLoading
                paramsBrowserAdapter.updateData(filterList(state.data))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    override fun onBackPressed() {
        if (currentPath == "/proc/sys") {
            finish()
        } else {
            onDirectoryChanged(File(currentPath).parentFile ?: File("/proc/sys"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_browse_params, menu)
        actionBarMenu = menu

        setUpSearchView(menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_documentation -> openDocumentationUrl()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDirectoryChanged(newDir: File) {
        val newPath = newDir.absolutePath
        if (newPath.isNotEmpty() && newPath.startsWith("/proc/sys")) {
            currentPath = newPath
            supportActionBar?.subtitle = newPath

            fun setItemVisible() = actionBarMenu?.findItem(R.id.action_documentation)?.setVisible(true)

            when {
                newPath.startsWith("/proc/sys/abi") -> setItemVisible().also {
                    documentationUrl = "https://www.kernel.org/doc/Documentation/sysctl/abi.txt"
                }

                newPath.startsWith("/proc/sys/fs") -> setItemVisible().also {
                    documentationUrl = "https://www.kernel.org/doc/Documentation/sysctl/fs.txt"
                }

                newPath.startsWith("/proc/sys/kernel") -> setItemVisible().also {
                    documentationUrl = "https://www.kernel.org/doc/Documentation/sysctl/kernel.txt"
                }

                newPath.startsWith("/proc/sys/net") -> setItemVisible().also {
                    documentationUrl = "https://www.kernel.org/doc/Documentation/sysctl/net.txt"
                }

                newPath.startsWith("/proc/sys/vm") -> setItemVisible().also {
                    documentationUrl = "https://www.kernel.org/doc/Documentation/sysctl/vm.txt"
                }

                else -> actionBarMenu?.findItem(R.id.action_documentation)?.isVisible = false
            }
        } else {
            toast(getString(R.string.invalid_path))
        }

        resetSearchExpression()
        refreshList()
    }

    override fun onParamItemClicked(param: KernelParam, itemLayout: View) {
        val sharedElements = arrayOf(
            Pair<View, String>(
                itemLayout.findViewById(R.id.name),
                EditKernelParamActivity.NAME_TRANSITION_NAME
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

    private fun refreshList() {
        paramViewModel.setPath(currentPath)
    }

    private suspend fun filterList(list: List<KernelParam>) = withContext(Dispatchers.Default) {
        if (searchExpression.isEmpty()) return@withContext list.toMutableList()

        return@withContext list.filter { param ->
            param.name.toLowerCase(defaultLocale)
                .replace(".", "")
                .contains(searchExpression.toLowerCase(defaultLocale))
        }.toMutableList()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openDocumentationUrl() {
        if (isFinishing) return

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_web)
            setCancelable(true)
        }

        val progressBar: ProgressBar = dialog.findViewById(R.id.webDialogProgress)
        val swipeLayout: SwipeRefreshLayout = dialog.findViewById(R.id.webDialogSwipeLayout)

        val webView = dialog.findViewById<WebView>(R.id.webDialogWebView).apply {
            settings.apply {
                javaScriptEnabled = true
                setAppCacheEnabled(true)
                setAppCachePath(cacheDir.absolutePath)
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }

            loadUrl(documentationUrl)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    swipeLayout.isRefreshing = false

                    // Change webView background and text color to match the app theme
                    view.loadUrl("""
                        |javascript:(
                            |function() { 
                                |document.querySelector('body').style.color='#FFFFFF'; 
                                |document.querySelector('body').style.background='#232F34';
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
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { webView.reload() }
        }

        dialog.show()
    }
}

interface DirectoryChangedListener {
    fun onDirectoryChanged(newDir: File)
}


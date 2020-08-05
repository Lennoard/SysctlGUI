package com.androidvip.sysctlgui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidvip.sysctlgui.*
import com.androidvip.sysctlgui.activities.base.BaseSearchActivity
import com.androidvip.sysctlgui.adapters.KernelParamBrowserAdapter
import kotlinx.android.synthetic.main.activity_kernel_param_browser.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference

class KernelParamBrowserActivity : BaseSearchActivity(), DirectoryChangedListener {
    private var actionBarMenu: Menu? = null
    private var documentationUrl = "https://www.kernel.org/doc/Documentation"
    private var currentPath = "/proc/sys"
    private val paramsBrowserAdapter: KernelParamBrowserAdapter by lazy {
        KernelParamBrowserAdapter(arrayOf(), WeakReference(this), this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kernel_param_browser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        paramBrowserSwipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { refreshList() }
        }

        paramBrowserRecyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this.context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(this.context, recyclerViewColumns)
            adapter = paramsBrowserAdapter
        }
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

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    override fun onQueryTextChanged() {
        refreshList()
    }

    override fun onBackPressed() {
        if (currentPath == "/proc/sys") {
            finish()
        } else {
            onDirectoryChanged(File(currentPath).parentFile!!)
            refreshList()
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

    private fun refreshList() {
        paramBrowserSwipeLayout.isRefreshing = true

        launch {
            var files = getCurrentPathFiles()

            withContext(Dispatchers.Default) {
                files = files?.filter { file ->
                    file.name.toLowerCase(defaultLocale)
                        .replace(".", "")
                        .contains(searchExpression.toLowerCase(defaultLocale))
                }?.toTypedArray()
            }

            paramBrowserSwipeLayout.isRefreshing = false
            files?.let {
                paramsBrowserAdapter.updateData(it)
            }
        }
    }

    private suspend fun getCurrentPathFiles() : Array<File>? = withContext(Dispatchers.IO) {
        runCatching {
            File(currentPath).listFiles()
        }.getOrDefault(arrayOf())
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

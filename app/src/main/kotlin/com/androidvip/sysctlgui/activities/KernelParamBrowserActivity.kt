package com.androidvip.sysctlgui.activities

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.activities.base.BaseSearchActivity
import com.androidvip.sysctlgui.adapters.KernelParamBrowserAdapter
import com.androidvip.sysctlgui.runSafeOnUiThread
import kotlinx.android.synthetic.main.activity_kernel_param_browser.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class KernelParamBrowserActivity : BaseSearchActivity(), DirectoryChangedListener {
    private var actionBarMenu: Menu? = null
    private var documentationUrl = "https://www.kernel.org/doc/Documentation"
    private var currentPath = "/proc/sys"
    private val paramsBrowserAdapter: KernelParamBrowserAdapter by lazy {
        KernelParamBrowserAdapter(arrayOf(), this, this)
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
            Toast.makeText(this, getString(R.string.invalid_path), Toast.LENGTH_SHORT).show()
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
            onDirectoryChanged(File(currentPath).parentFile)
            refreshList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_browse_params, menu)
        actionBarMenu = menu

        setUpSearchView(menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_documentation -> openDocumentationUrl()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshList() {
        paramBrowserSwipeLayout.isRefreshing = true

        GlobalScope.launch {
            var files = getCurrentPathFiles()

            if (searchExpression.isNotEmpty()) {
                files = files.filter { file ->
                    file.name.toLowerCase(defaultLocale)
                        .contains(searchExpression.toLowerCase(defaultLocale))
                }.toTypedArray()
            }

            runSafeOnUiThread {
                paramBrowserSwipeLayout.isRefreshing = false
                files?.let {
                    paramsBrowserAdapter.updateData(it)
                }
            }
        }
    }

    private suspend fun getCurrentPathFiles() = withContext(Dispatchers.IO) {
        runCatching {
            File(currentPath).listFiles()
        }.getOrDefault(arrayOf<File>())
    }

    private fun openDocumentationUrl() {
        if (isFinishing) return

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_web)
            setCancelable(true)
        }

        val progressBar = dialog.findViewById<ProgressBar>(R.id.webDialogProgress)
        val swipeLayout= dialog.findViewById<SwipeRefreshLayout>(R.id.webDialogSwipeLayout)

        val webView = dialog.findViewById<WebView>(R.id.webDialogWebView).apply {
            settings.javaScriptEnabled = false
            loadUrl(documentationUrl)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    swipeLayout.isRefreshing = false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, progress: Int) {
                    progressBar.progress = progress
                    if (progress == 100) {
                        progressBar.visibility = View.GONE
                        swipeLayout.isRefreshing = false
                    } else progressBar.visibility = View.VISIBLE
                }
            }
        }

        swipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { webView.reload() }
        }

        dialog.show()
    }

    private val recyclerViewColumns: Int
        get() {
            val isLandscape = resources.getBoolean(R.bool.is_landscape)
            return if (isLandscape) 2 else 1
        }
}

interface DirectoryChangedListener {
    fun onDirectoryChanged(newDir: File)
}

package com.androidvip.sysctlgui.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.androidvip.sysctlgui.R

abstract class BaseSearchActivity<Binding : ViewBinding>(
    override val bindingFactory: (LayoutInflater) -> Binding
) : BaseActivity<Binding>(bindingFactory) {
    protected var searchExpression: String = ""
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        setUpSearchView(menu)

        return true
    }

    abstract fun onQueryTextChanged()

    protected fun setUpSearchView(menu: Menu?) {
        searchView = (menu?.findItem(R.id.action_search)?.actionView as SearchView?)?.apply {
            setOnQueryTextListener(
                object :
                    androidx.appcompat.widget.SearchView.OnQueryTextListener,
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        searchExpression = newText.orEmpty().replace(".", "")

                        this@BaseSearchActivity.onQueryTextChanged()
                        return true
                    }
                })

            // expand and show keyboard
            isIconifiedByDefault = false
            onActionViewExpanded()
        }
    }

    protected fun resetSearchExpression() {
        searchExpression = ""
        searchView?.setQuery("", false)
    }

    protected val recyclerViewColumns: Int
        get() = if (resources.getBoolean(R.bool.is_landscape)) 2 else 1
}

package com.androidvip.sysctlgui.ui.base

import android.view.Menu
import android.widget.SearchView
import com.androidvip.sysctlgui.R
import java.util.*

abstract class BaseSearchActivity : BaseActivity() {
    protected val defaultLocale: Locale by lazy { Locale.getDefault() }
    protected var searchExpression: String = ""
    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        setUpSearchView(menu)

        return true
    }

    abstract fun onQueryTextChanged()

    fun setUpSearchView(menu: Menu?) {
        searchView = (menu?.findItem(R.id.action_search)?.actionView as SearchView?)?.apply {
            setOnQueryTextListener(object :
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

    fun resetSearchExpression() {
        searchExpression = ""
        searchView?.setQuery("", false)
    }

    val recyclerViewColumns : Int
        get() = if (resources.getBoolean(R.bool.is_landscape)) 2 else 1

}

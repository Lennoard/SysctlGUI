package com.androidvip.sysctlgui.activities.base

import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.androidvip.sysctlgui.R
import java.util.*

abstract class BaseSearchActivity : AppCompatActivity() {
    protected var searchExpression: String = ""
    protected var defaultLocale: Locale = Locale.getDefault()
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
                    searchExpression = newText.orEmpty()

                    this@BaseSearchActivity.onQueryTextChanged()
                    return true
                }
            })

            // expand and show keyboard
            setIconifiedByDefault(false)
            onActionViewExpanded()
        }
    }

    fun resetSearchExpression() {
        searchExpression = ""
        searchView?.setQuery("", false)
    }
}
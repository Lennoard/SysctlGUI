package com.androidvip.sysctlgui.ui.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.androidvip.sysctlgui.R

abstract class BaseSearchFragment : Fragment() {
    protected var searchExpression: String = ""
    private var searchView: SearchView? = null

    abstract fun onQueryTextChanged()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_search, menu)
        setUpSearchView(menu)
    }

    protected fun setUpSearchView(menu: Menu?) {
        searchView = (menu?.findItem(R.id.action_search)?.actionView as? SearchView)?.apply {
            setOnQueryTextListener(
                object :
                    androidx.appcompat.widget.SearchView.OnQueryTextListener,
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        searchExpression = newText.orEmpty().replace(".", "")

                        this@BaseSearchFragment.onQueryTextChanged()
                        return true
                    }
                }
            )

            // expand and show keyboard
            isIconifiedByDefault = false
            onActionViewExpanded()
        }
    }

    protected fun resetSearchExpression() {
        searchExpression = ""
        searchView?.setQuery("", false)
    }
}

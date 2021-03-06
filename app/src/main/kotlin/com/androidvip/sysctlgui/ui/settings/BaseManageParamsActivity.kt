package com.androidvip.sysctlgui.ui.settings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.data.repository.ParamRepository
import com.androidvip.sysctlgui.databinding.ActivityManageParamSetBinding
import com.androidvip.sysctlgui.helpers.SwipeToDeleteCallback
import com.androidvip.sysctlgui.showAsLight
import com.androidvip.sysctlgui.ui.base.BaseSearchActivity
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference

abstract class BaseManageParamsActivity : BaseSearchActivity(),
    OnParamItemClickedListener,
    OnPopUpMenuItemSelectedListener, RemovableParamAdapter.OnRemoveRequestedListener {
    private lateinit var binding: ActivityManageParamSetBinding
    private val repository: ParamRepository by inject()
    private val savedKernelParams = mutableListOf<KernelParam>()
    private val removableParamAdapter: RemovableParamAdapter by lazy {
        RemovableParamAdapter(
            this,
            this,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageParamSetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        afterSetContentView()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            savedKernelParams.clear()
            savedKernelParams.addAll(
                repository.getParams(ParamRepository.SOURCE_ROOM).filter(filterPredicate)
            )
            updateRecyclerViewData()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextChanged() {
        updateRecyclerViewData()
    }

    override fun onParamItemClicked(param: KernelParam, itemLayout: View) {
        val sharedElements = arrayOf(
            Pair<View, String>(
                itemLayout.findViewById(R.id.paramName),
                EditKernelParamActivity.NAME_TRANSITION_NAME
            ),
            Pair<View, String>(
                itemLayout.findViewById(R.id.paramValue),
                EditKernelParamActivity.VALUE_TRANSITION_NAME
            )
        )
        val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            *sharedElements,
        )
        Intent(this, EditKernelParamActivity::class.java).apply {
            putExtra(RemovableParamAdapter.EXTRA_PARAM, param)
            putExtra(RemovableParamAdapter.EXTRA_EDIT_SAVED_PARAM, true)
            startActivity(this, options.toBundle())
        }
    }

    override fun onPopUpMenuItemSelected(
        kernelParam: KernelParam,
        itemId: Int,
        removableLayout: ConstraintLayout
    ) {
        when (itemId) {
            R.id.popupEdit -> onParamItemClicked(kernelParam, removableLayout)
            R.id.popupRemove -> {
                val index = savedKernelParams.indexOf(kernelParam)
                if (index < 0) return

                onRemoveRequested(index, false, removableLayout)
            }
        }
    }

    override fun onRemoveRequested(
        position: Int,
        fakeGesture: Boolean,
        removableLayout: View
    ) {
        savedKernelParams.getOrNull(position)?.let {
            lifecycleScope.launch {
                repository.delete(it, ParamRepository.SOURCE_ROOM)
                if (fakeGesture) {
                    Handler(mainLooper).postDelayed({
                        savedKernelParams.removeAt(position)
                        updateRecyclerViewData()
                    }, 400)
                } else {
                    savedKernelParams.removeAt(position)
                    updateRecyclerViewData()
                }
            }
        }
    }

    abstract val filterPredicate: (KernelParam) -> Boolean

    private fun updateRecyclerViewData() {
        val snackbar = Snackbar.make(
            binding.recyclerView,
            R.string.no_parameters_found,
            Snackbar.LENGTH_INDEFINITE
        )

        binding.swipeLayout.isRefreshing = true

        var kernelParams = savedKernelParams
        if (searchExpression.isNotEmpty()) {
            kernelParams = kernelParams.filter { kernelParameter ->
                kernelParameter.name.toLowerCase(defaultLocale)
                    .replace(".", "")
                    .contains(searchExpression.toLowerCase(defaultLocale))
            }.toMutableList()
        }

        binding.swipeLayout.isRefreshing = false
        removableParamAdapter.updateData(kernelParams)

        if (kernelParams.isEmpty()) {
            snackbar.showAsLight()
        } else {
            snackbar.dismiss()
        }
    }

    private fun afterSetContentView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.swipeLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener { updateRecyclerViewData() }
        }

        binding.recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
            layoutManager = GridLayoutManager(context, recyclerViewColumns)
            adapter = removableParamAdapter

            val itemTouchHelper = ItemTouchHelper(
                SwipeToDeleteCallback(
                    removableParamAdapter, WeakReference(this@BaseManageParamsActivity)
                )
            )
            itemTouchHelper.attachToRecyclerView(this)
        }
    }
}

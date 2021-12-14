package com.androidvip.sysctlgui.ui.params.user

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.databinding.ActivityManageParamSetBinding
import com.androidvip.sysctlgui.getColorRoles
import com.androidvip.sysctlgui.helpers.SwipeToDeleteCallback
import com.androidvip.sysctlgui.showAsLight
import com.androidvip.sysctlgui.ui.base.BaseSearchActivity
import com.androidvip.sysctlgui.ui.params.OnParamItemClickedListener
import com.androidvip.sysctlgui.ui.params.OnPopUpMenuItemSelectedListener
import com.androidvip.sysctlgui.ui.params.edit.EditKernelParamActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference
import androidx.core.util.Pair as PairUtil

abstract class BaseManageParamsActivity :
    BaseSearchActivity(),
    OnParamItemClickedListener,
    OnPopUpMenuItemSelectedListener,
    RemovableParamAdapter.OnRemoveRequestedListener {
    protected val paramViewModel: UserParamsViewModel by inject()
    private lateinit var binding: ActivityManageParamSetBinding
    private val noParamSnackbar: Snackbar by lazy {
        Snackbar.make(
            binding.recyclerView,
            R.string.no_parameters_found,
            Snackbar.LENGTH_INDEFINITE
        )
    }
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.swipeLayout.apply {
            val roles = getColorRoles()
            setColorSchemeColors(roles.accent)
            setProgressBackgroundColorSchemeColor(roles.accentContainer)
            setOnRefreshListener { refreshList() }
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

        paramViewModel.viewState.observe(this) { state ->
            binding.swipeLayout.isRefreshing = state.isLoading
            updateRecyclerViewData(state.data)
        }
    }

    override fun onStart() {
        super.onStart()
        paramViewModel.setFilterPredicate(filterPredicate)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextChanged() {
        if (searchExpression.isNotEmpty()) {
            paramViewModel.setFilterPredicate {
                it.name.lowercase(defaultLocale)
                    .replace(".", "")
                    .contains(searchExpression.lowercase(defaultLocale)) &&
                    filterPredicate.invoke(it)
            }
        } else {
            paramViewModel.setFilterPredicate(filterPredicate)
        }
    }

    override fun onParamItemClicked(param: KernelParam, itemLayout: View) {
        val sharedElements = arrayOf<PairUtil<View, String>>(
            PairUtil(
                itemLayout.findViewById(R.id.paramName),
                EditKernelParamActivity.NAME_TRANSITION_NAME
            ),
            PairUtil(
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
                onRemoveRequested(kernelParam, true, removableLayout)
            }
        }
    }

    override fun onRemoveRequested(
        kernelParam: KernelParam,
        fakeGesture: Boolean,
        removableLayout: View
    ) {
        if (fakeGesture) {
            val viewWidth = removableLayout.measuredWidth
            ObjectAnimator.ofFloat(
                removableLayout,
                "translationX",
                viewWidth * (-1F)
            ).apply {
                duration = 300
                start()
            }.addListener(onEnd = {
                paramViewModel.delete(kernelParam)
            })
        } else {
            paramViewModel.delete(kernelParam)
        }
    }

    abstract val filterPredicate: (KernelParam) -> Boolean

    private fun refreshList() = paramViewModel.getParams()

    private fun updateRecyclerViewData(params: List<KernelParam>) {
        removableParamAdapter.updateData(params)

        if (params.isEmpty() && !noParamSnackbar.isShown) {
            noParamSnackbar.showAsLight()
        } else if (noParamSnackbar.isShown) {
            noParamSnackbar.dismiss()
        }
    }
}

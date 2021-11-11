package com.androidvip.sysctlgui.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.data.models.HomeItem

class MainViewModel : ViewModel() {
    private val _viewEffect = MutableLiveData<MainViewEffect>()
    val viewEffect: LiveData<MainViewEffect> = _viewEffect

    fun getHomeItems(): List<HomeItem> = listOf(
        HomeItem(R.string.show_variables, R.string.show_variables_sum, R.drawable.ic_edit_outline),
        HomeItem(
            R.string.browse_variables,
            R.string.browse_variables_sum,
            R.drawable.ic_folder_outline
        ),
        HomeItem(
            R.string.read_from_file,
            R.string.read_from_file_sum,
            R.drawable.ic_file_import_outline
        ),
        HomeItem(
            R.string.show_favorites,
            R.string.show_favorites_sum,
            R.drawable.ic_favorite_unselected
        )
    )

    fun doWhenListPressed() = _viewEffect.postValue(MainViewEffect.NavigateToKernelList)

    fun doWhenBrowsePressed() = _viewEffect.postValue(MainViewEffect.NavigateToKernelBrowser)

    fun doWhenImportPressed() = _viewEffect.postValue(MainViewEffect.ImportParamsFromFile)

    fun doWhenFavoritesPressed() = _viewEffect.postValue(MainViewEffect.NavigateToFavorites)

    fun doWhenSettingsPressed() = _viewEffect.postValue(MainViewEffect.NavigateToSettings)
}

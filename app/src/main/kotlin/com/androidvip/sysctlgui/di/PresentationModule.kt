package com.androidvip.sysctlgui.di

import com.androidvip.sysctlgui.ui.export.ExportOptionsViewModel
import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.params.browse.BrowseParamsViewModel
import com.androidvip.sysctlgui.ui.params.list.ListParamsViewModel
import com.androidvip.sysctlgui.ui.params.user.UserParamsViewModel
import com.androidvip.sysctlgui.widgets.FavoriteWidgetParamUpdater
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModules = module {
    viewModel { BrowseParamsViewModel(get(), Dispatchers.IO, get()) }
    viewModelOf(::ListParamsViewModel)
    viewModelOf(::UserParamsViewModel)
    viewModelOf(::MainViewModel)
    viewModel { ExportOptionsViewModel(get(), get(), get()) }

    single { FavoriteWidgetParamUpdater(androidContext()).getListener() }
}

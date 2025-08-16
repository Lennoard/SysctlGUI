package com.androidvip.sysctlgui.di

import com.androidvip.sysctlgui.ui.main.MainViewModel
import com.androidvip.sysctlgui.ui.params.browse.ParamBrowseViewModel
import com.androidvip.sysctlgui.ui.params.edit.EditParamViewModel
import com.androidvip.sysctlgui.ui.presets.PresetsViewModel
import com.androidvip.sysctlgui.ui.search.SearchViewModel
import com.androidvip.sysctlgui.ui.settings.SettingsViewModel
import com.androidvip.sysctlgui.ui.user.UserParamsViewModel
import com.androidvip.sysctlgui.widgets.UpdateFavoriteWidgetUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule = module {
    singleOf(::MainViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ParamBrowseViewModel)
    viewModelOf(::EditParamViewModel)
    viewModelOf(::SearchViewModel)
    singleOf(::PresetsViewModel)
    viewModelOf(::UserParamsViewModel)

    factory { UpdateFavoriteWidgetUseCase(androidContext()) }
}

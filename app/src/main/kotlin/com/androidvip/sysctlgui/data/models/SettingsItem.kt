package com.androidvip.sysctlgui.data.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class SettingsItem(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int
)

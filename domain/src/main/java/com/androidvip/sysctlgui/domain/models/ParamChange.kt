package com.androidvip.sysctlgui.domain.models

data class ParamChange(
    val id: Long = 0,
    val paramName: String,
    val path: String,
    val previousValue: String,
    val newValue: String,
    val changedAt: Long
)

package com.androidvip.sysctlgui.data.mapper

interface Mapper<F, T> {
    fun map(from: F): T
    fun unmap(from: T): F
}

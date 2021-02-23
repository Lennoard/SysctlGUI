package com.androidvip.sysctlgui.data.repository

import com.androidvip.sysctlgui.data.ParamDao
import com.androidvip.sysctlgui.data.models.KernelParam
import com.androidvip.sysctlgui.utils.RootUtils
import java.io.File

class ParamRepository(private val paramDao: ParamDao) {
    suspend fun getParamsFromKernel(): MutableList<KernelParam> {
        val command = if (RootUtils.isBusyboxAvailable()) "busybox sysctl -a" else "sysctl -a"
        val lines = mutableListOf<String>()
        RootUtils.executeWithOutput(command, "") { lines += it }

        return lines.filter {
            it.validSysctlOutput()
        }.map {
            it.split("=").first().trim()
        }.mapIndexed { index, paramName ->
            KernelParam(
                id = index + 1,
                name = paramName
            ).apply {
                setPathFromName(paramName)
            }
        }.toMutableList()
    }

    suspend fun getParamsFileList(files: Array<out File>): MutableList<KernelParam> {
        return files.map {
            it.absolutePath
        }.mapIndexed { index, path ->
            KernelParam(
                id = index + 1,
                path = path,
            ).apply {
                setNameFromPath(path)
                value = RootUtils.executeWithOutput("cat $path", "")
            }
        }.toMutableList()
    }

    suspend fun getParams() = paramDao.getAll().orEmpty()

    suspend fun clearAllParams() = paramDao.clearTable()

    suspend fun delete(param: KernelParam) = paramDao.delete(param)

    suspend fun update(param: KernelParam) = paramDao.update(param)

    suspend fun addParam(param: KernelParam) {
        if (getParams().contains(param)) {
            update(param)
        } else {
            paramDao.insertAll(param)
        }
    }

    suspend fun addAll(params: List<KernelParam>) {
        paramDao.insertAll(*params.toTypedArray())
    }

    private fun String.validSysctlOutput(): Boolean {
        return !contains("denied") && !startsWith("sysctl") && contains("=")
    }

}
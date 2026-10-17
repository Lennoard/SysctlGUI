package com.androidvip.sysctlgui.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.androidvip.sysctlgui.domain.models.ParamChange

@Entity(
    tableName = PARAM_CHANGE_HISTORY_TABLE_NAME,
    indices = [Index(value = ["changedAt"])]
)
data class ParamChangeDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paramName: String,
    val path: String,
    val previousValue: String,
    val newValue: String,
    val changedAt: Long
) {
    fun toParamChange() = ParamChange(
        id = id,
        paramName = paramName,
        path = path,
        previousValue = previousValue,
        newValue = newValue,
        changedAt = changedAt
    )

    companion object {
        fun fromParamChange(change: ParamChange) = ParamChangeDTO(
            id = change.id,
            paramName = change.paramName,
            path = change.path,
            previousValue = change.previousValue,
            newValue = change.newValue,
            changedAt = change.changedAt
        )
    }
}

private const val PARAM_CHANGE_HISTORY_TABLE_NAME = "ParamChangeHistory"

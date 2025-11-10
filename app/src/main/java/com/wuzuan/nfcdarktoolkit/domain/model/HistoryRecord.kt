package com.wuzuan.nfcdarktoolkit.domain.model

/**
 * 操作歷史記錄（Domain Model）
 */
data class HistoryRecord(
    val id: Long = 0,
    val tagId: String?,
    val tagType: String?,
    val actionType: ActionType,
    val title: String,
    val description: String,
    val payloadRaw: String,
    val timestamp: Long
)

enum class ActionType {
    READ,
    WRITE,
    FORMAT,
    LOCK,
    CLONE,
    EMULATE
}

/**
 * 歷史記錄篩選器
 */
data class HistoryFilter(
    val actionType: ActionType? = null,
    val searchQuery: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)


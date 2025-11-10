package com.wuzuan.nfcdarktoolkit.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 歷史記錄 Entity
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagId: String?,
    val tagType: String?,
    val actionType: String,   // READ, WRITE, FORMAT, LOCK, CLONE, EMULATE
    val title: String,
    val description: String,
    val payloadRaw: String,   // 原始資料（JSON 或 Hex String）
    val timestamp: Long
)


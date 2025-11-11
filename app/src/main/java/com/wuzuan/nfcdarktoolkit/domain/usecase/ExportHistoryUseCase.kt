package com.wuzuan.nfcdarktoolkit.domain.usecase

import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.google.gson.GsonBuilder
import javax.inject.Inject

/**
 * 匯出歷史記錄 UseCase
 */
class ExportHistoryUseCase @Inject constructor() {
    
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    /**
     * 匯出為 JSON
     */
    fun exportAsJson(historyList: List<HistoryRecord>): String {
        return try {
            gson.toJson(historyList)
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }
    
    /**
     * 匯出為 CSV
     */
    fun exportAsCsv(historyList: List<HistoryRecord>): String {
        return buildString {
            // CSV 標題行
            appendLine("ID,Tag ID,Tag Type,Action,Title,Description,Timestamp")
            
            // 資料行
            historyList.forEach { record ->
                append("${record.id},")
                append("\"${record.tagId ?: ""}\",")
                append("\"${record.tagType ?: ""}\",")
                append("\"${record.actionType}\",")
                append("\"${record.title.replace("\"", "\"\"")}\",")
                append("\"${record.description.replace("\"", "\"\"")}\",")
                appendLine(record.timestamp)
            }
        }
    }
}


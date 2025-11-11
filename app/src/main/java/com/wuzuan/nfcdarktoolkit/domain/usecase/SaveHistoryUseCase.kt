package com.wuzuan.nfcdarktoolkit.domain.usecase

import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.data.repository.SettingsRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 儲存歷史記錄 UseCase
 */
class SaveHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) {
    
    suspend operator fun invoke(
        tagId: String?,
        tagType: String?,
        actionType: ActionType,
        title: String,
        description: String,
        payloadRaw: String
    ): Result<Long> {
        return try {
            // 檢查是否啟用自動儲存
            val autoSave = settingsRepository.autoSaveHistory.first()
            if (!autoSave) {
                return Result.success(-1L)
            }
            
            val record = HistoryRecord(
                tagId = tagId,
                tagType = tagType,
                actionType = actionType,
                title = title,
                description = description,
                payloadRaw = payloadRaw,
                timestamp = System.currentTimeMillis()
            )
            
            val id = historyRepository.insertHistory(record)
            Result.success(id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}


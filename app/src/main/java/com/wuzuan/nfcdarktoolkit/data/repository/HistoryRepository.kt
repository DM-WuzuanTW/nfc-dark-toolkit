package com.wuzuan.nfcdarktoolkit.data.repository

import com.wuzuan.nfcdarktoolkit.data.local.db.HistoryDao
import com.wuzuan.nfcdarktoolkit.data.local.db.HistoryEntity
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 歷史記錄 Repository
 */
@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    
    fun getAllHistory(): Flow<List<HistoryRecord>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getHistoryById(id: Long): HistoryRecord? {
        return historyDao.getHistoryById(id)?.toDomain()
    }
    
    fun getHistoryByActionType(actionType: ActionType): Flow<List<HistoryRecord>> {
        return historyDao.getHistoryByActionType(actionType.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun searchHistory(query: String): Flow<List<HistoryRecord>> {
        return historyDao.searchHistory(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getHistoryByDateRange(startDate: Long, endDate: Long): Flow<List<HistoryRecord>> {
        return historyDao.getHistoryByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun insertHistory(record: HistoryRecord): Long {
        return historyDao.insertHistory(record.toEntity())
    }
    
    suspend fun deleteHistory(record: HistoryRecord) {
        historyDao.deleteHistoryById(record.id)
    }
    
    suspend fun deleteAllHistory() {
        historyDao.deleteAllHistory()
    }
    
    suspend fun getHistoryCount(): Int {
        return historyDao.getHistoryCount()
    }
    
    // Mappers
    private fun HistoryEntity.toDomain(): HistoryRecord {
        return HistoryRecord(
            id = id,
            tagId = tagId,
            tagType = tagType,
            actionType = ActionType.valueOf(actionType),
            title = title,
            description = description,
            payloadRaw = payloadRaw,
            timestamp = timestamp
        )
    }
    
    private fun HistoryRecord.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            tagId = tagId,
            tagType = tagType,
            actionType = actionType.name,
            title = title,
            description = description,
            payloadRaw = payloadRaw,
            timestamp = timestamp
        )
    }
}


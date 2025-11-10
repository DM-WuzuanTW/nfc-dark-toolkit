package com.wuzuan.nfcdarktoolkit.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 歷史記錄 DAO
 */
@Dao
interface HistoryDao {
    
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>
    
    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: Long): HistoryEntity?
    
    @Query("SELECT * FROM history WHERE actionType = :actionType ORDER BY timestamp DESC")
    fun getHistoryByActionType(actionType: String): Flow<List<HistoryEntity>>
    
    @Query("""
        SELECT * FROM history 
        WHERE (tagId LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%') 
        ORDER BY timestamp DESC
    """)
    fun searchHistory(query: String): Flow<List<HistoryEntity>>
    
    @Query("""
        SELECT * FROM history 
        WHERE timestamp BETWEEN :startDate AND :endDate 
        ORDER BY timestamp DESC
    """)
    fun getHistoryByDateRange(startDate: Long, endDate: Long): Flow<List<HistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(historyList: List<HistoryEntity>)
    
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)
    
    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)
    
    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
    
    @Query("SELECT COUNT(*) FROM history")
    suspend fun getHistoryCount(): Int
}


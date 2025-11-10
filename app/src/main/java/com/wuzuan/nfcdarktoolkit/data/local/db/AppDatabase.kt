package com.wuzuan.nfcdarktoolkit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database
 */
@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    
    companion object {
        const val DATABASE_NAME = "nfc_dark_toolkit_db"
    }
}


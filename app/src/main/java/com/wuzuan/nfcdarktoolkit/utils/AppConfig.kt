package com.wuzuan.nfcdarktoolkit.utils

/**
 * 應用配置常數
 * 統一管理應用中的配置參數
 */
object AppConfig {
    
    // NFC 配置
    object Nfc {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val MAX_RETRY_COUNT = 3
        const val FOREGROUND_DISPATCH_DELAY_MS = 100L
        
        // NDEF 配置
        const val MAX_NDEF_SIZE = 8192 // 8KB
        const val DEFAULT_LANGUAGE_CODE = "zh"
        const val DEFAULT_CHARSET = "UTF-8"
    }
    
    // 數據庫配置
    object Database {
        const val MAX_HISTORY_RECORDS = 1000
        const val CLEANUP_THRESHOLD = 1200
        const val BATCH_SIZE = 50
    }
    
    // UI 配置
    object UI {
        const val AD_ROTATION_INTERVAL_MS = 15000L
        const val ANIMATION_DURATION_MS = 300L
        const val DEBOUNCE_DELAY_MS = 500L
    }
    
    // 性能配置
    object Performance {
        const val MEMORY_THRESHOLD_MB = 50
        const val GC_TRIGGER_THRESHOLD = 0.8f
        const val CACHE_SIZE_MB = 10
    }
    
    // 日誌配置
    object Logging {
        const val MAX_LOG_FILE_SIZE_MB = 5
        const val LOG_RETENTION_DAYS = 7
        val SENSITIVE_FIELDS = setOf("password", "token", "key", "secret")
    }
    
    // 網路配置
    object Network {
        const val CONNECT_TIMEOUT_MS = 10000L
        const val READ_TIMEOUT_MS = 15000L
        const val WRITE_TIMEOUT_MS = 15000L
    }
    
    // 安全配置
    object Security {
        const val ENABLE_SAFE_MODE_DEFAULT = false
        const val MAX_TAG_SIZE_BYTES = 1024 * 1024 // 1MB
        val ALLOWED_MIME_TYPES = setOf(
            "text/plain",
            "text/vcard",
            "application/json",
            "application/xml"
        )
    }
}

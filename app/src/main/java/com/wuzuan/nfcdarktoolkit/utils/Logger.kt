package com.wuzuan.nfcdarktoolkit.utils

import android.util.Log

/**
 * 統一日誌工具類
 * 提供結構化的日誌記錄功能
 */
object Logger {
    
    private const val TAG = "NfcDarkToolkit"
    
    // Debug Reporter 實例 (將在 Application 中設置)
    var debugReporter: DebugReporter? = null
    
    /**
     * Debug 日誌
     */
    fun d(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }
    
    /**
     * Info 日誌
     */
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }
    
    /**
     * Warning 日誌
     */
    fun w(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }
    
    /**
     * Error 日誌 (自動回報到 Discord)
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
            // 自動回報嚴重錯誤到 Discord
            debugReporter?.reportAppError(
                component = tag,
                error = throwable,
                userAction = message
            )
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * NFC 操作專用日誌 (自動回報 NFC 錯誤到 Discord)
     */
    fun nfc(operation: String, message: String, throwable: Throwable? = null) {
        val logMessage = "[$operation] $message"
        if (throwable != null) {
            Log.e(TAG, logMessage, throwable)
            // 自動回報 NFC 錯誤到 Discord
            debugReporter?.reportNfcError(
                operation = operation,
                error = throwable,
                additionalInfo = mapOf("details" to message)
            )
        } else {
            Log.i(TAG, logMessage)
        }
    }
    
    /**
     * 手動回報錯誤到 Discord
     */
    fun reportError(
        category: String,
        operation: String,
        error: Throwable,
        additionalInfo: Map<String, String> = emptyMap()
    ) {
        when (category.lowercase()) {
            "nfc" -> debugReporter?.reportNfcError(operation, error, additionalInfo = additionalInfo)
            "system" -> debugReporter?.reportSystemError(operation, error, additionalInfo)
            else -> debugReporter?.reportAppError(operation, error, additionalInfo = additionalInfo)
        }
    }
}

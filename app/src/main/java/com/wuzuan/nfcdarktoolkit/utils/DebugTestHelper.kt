package com.wuzuan.nfcdarktoolkit.utils

import android.content.Context
import android.widget.Toast

/**
 * Debug 測試輔助工具
 */
object DebugTestHelper {
    
    /**
     * 測試 Discord 錯誤回報功能
     */
    fun testDiscordReporting(context: Context, debugReporter: DebugReporter) {
        try {
            // 模擬 NFC 錯誤
            debugReporter.reportNfcError(
                operation = "測試回報",
                error = RuntimeException("這是一個測試錯誤訊息"),
                tagInfo = "測試標籤 ID: AA:BB:CC:DD",
                additionalInfo = mapOf(
                    "測試類型" to "手動觸發",
                    "測試時間" to System.currentTimeMillis().toString()
                )
            )
            
            Toast.makeText(context, "測試錯誤已發送到 Discord", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "發送測試錯誤失敗: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 測試應用錯誤回報
     */
    fun testAppErrorReporting(context: Context, debugReporter: DebugReporter) {
        try {
            debugReporter.reportAppError(
                component = "測試元件",
                error = IllegalStateException("測試應用錯誤"),
                userAction = "用戶點擊了測試按鈕",
                additionalInfo = mapOf(
                    "螢幕" to "設定頁面",
                    "功能" to "Debug 測試"
                )
            )
            
            Toast.makeText(context, "測試應用錯誤已發送到 Discord", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "發送測試應用錯誤失敗: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 測試系統錯誤回報
     */
    fun testSystemErrorReporting(context: Context, debugReporter: DebugReporter) {
        try {
            debugReporter.reportSystemError(
                system = "測試系統",
                error = OutOfMemoryError("模擬記憶體不足錯誤"),
                additionalInfo = mapOf(
                    "可用記憶體" to "${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB",
                    "總記憶體" to "${Runtime.getRuntime().totalMemory() / 1024 / 1024} MB"
                )
            )
            
            Toast.makeText(context, "測試系統錯誤已發送到 Discord", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(context, "發送測試系統錯誤失敗: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

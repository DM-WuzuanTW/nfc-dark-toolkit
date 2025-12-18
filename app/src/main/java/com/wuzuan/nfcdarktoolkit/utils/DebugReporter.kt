package com.wuzuan.nfcdarktoolkit.utils

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug 錯誤回報工具
 * 自動將錯誤傳送到 Discord Webhook
 */
@Singleton
class DebugReporter @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1436321652412649622/OepW2A6pXiSAFqsFfiIwE7LBqTHeG4Qd_IeKFyKOLPv7-juaDbdEhYQNLnUmVSscMsme"
        private const val MAX_MESSAGE_LENGTH = 2000 // Discord 訊息長度限制
    }
    
    /**
     * 回報 NFC 相關錯誤
     */
    fun reportNfcError(
        operation: String,
        error: Throwable,
        tagInfo: String? = null,
        additionalInfo: Map<String, String> = emptyMap()
    ) {
        val errorReport = createErrorReport(
            category = "NFC Error",
            operation = operation,
            error = error,
            context = mapOf(
                "tagInfo" to (tagInfo ?: "N/A")
            ) + additionalInfo
        )
        
        sendToDiscord(errorReport)
    }
    
    /**
     * 回報一般應用錯誤
     */
    fun reportAppError(
        component: String,
        error: Throwable,
        userAction: String? = null,
        additionalInfo: Map<String, String> = emptyMap()
    ) {
        val errorReport = createErrorReport(
            category = "App Error",
            operation = component,
            error = error,
            context = mapOf(
                "userAction" to (userAction ?: "N/A")
            ) + additionalInfo
        )
        
        sendToDiscord(errorReport)
    }
    
    /**
     * 回報編譯或初始化錯誤
     */
    fun reportSystemError(
        system: String,
        error: Throwable,
        additionalInfo: Map<String, String> = emptyMap()
    ) {
        val errorReport = createErrorReport(
            category = "System Error",
            operation = system,
            error = error,
            context = additionalInfo
        )
        
        sendToDiscord(errorReport)
    }
    
    /**
     * 創建錯誤報告
     */
    private fun createErrorReport(
        category: String,
        operation: String,
        error: Throwable,
        context: Map<String, String> = emptyMap()
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val deviceInfo = getDeviceInfo()
        
        val report = StringBuilder()
        report.append("🚨 **$category** 🚨\n")
        report.append("**時間**: $timestamp\n")
        report.append("**操作**: $operation\n")
        report.append("**錯誤**: ${error.javaClass.simpleName}\n")
        report.append("**訊息**: ${error.message ?: "無訊息"}\n")
        
        // 設備資訊
        report.append("\n📱 **設備資訊**\n")
        deviceInfo.forEach { (key, value) ->
            report.append("**$key**: $value\n")
        }
        
        // 額外上下文
        if (context.isNotEmpty()) {
            report.append("\n📋 **上下文資訊**\n")
            context.forEach { (key, value) ->
                report.append("**$key**: $value\n")
            }
        }
        
        // Stack trace (截斷以符合 Discord 限制)
        val stackTrace = error.stackTraceToString()
        report.append("\n📄 **Stack Trace**\n")
        report.append("```\n")
        
        val remainingSpace = MAX_MESSAGE_LENGTH - report.length - 10 // 預留結尾空間
        if (stackTrace.length > remainingSpace) {
            report.append(stackTrace.substring(0, remainingSpace))
            report.append("\n... (截斷)")
        } else {
            report.append(stackTrace)
        }
        report.append("\n```")
        
        return report.toString()
    }
    
    /**
     * 獲取設備資訊
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "型號" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "Android版本" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "應用版本" to getAppVersion(),
            "NFC支援" to if (NfcPermissionHelper.isNfcSupported(context)) "是" else "否",
            "NFC啟用" to if (NfcPermissionHelper.isNfcEnabled(context)) "是" else "否"
        )
    }
    
    /**
     * 獲取應用版本
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "未知"
        }
    }
    
    /**
     * 發送到 Discord
     */
    private fun sendToDiscord(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(DISCORD_WEBHOOK_URL)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", "NFC-Dark-Toolkit-Debug")
                connection.doOutput = true
                
                // 創建 JSON payload
                val json = JSONObject()
                json.put("content", message)
                json.put("username", "NFC Dark Toolkit Debug")
                
                // 發送請求
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()
                
                val responseCode = connection.responseCode
                Logger.d("Discord webhook 回應碼: $responseCode")
                
                if (responseCode == 204) {
                    Logger.d("錯誤報告已成功發送到 Discord")
                } else {
                    Logger.w("Discord webhook 回應異常: $responseCode")
                }
                
            } catch (e: Exception) {
                Logger.e("發送錯誤報告到 Discord 失敗: ${e.message}", e)
                // 不要在這裡再次調用 reportError，避免無限循環
            }
        }
    }
}

/**
 * 全域錯誤回報擴展函數
 */
fun Throwable.reportToDiscord(
    reporter: DebugReporter,
    operation: String,
    category: String = "General",
    additionalInfo: Map<String, String> = emptyMap()
) {
    when (category.lowercase()) {
        "nfc" -> reporter.reportNfcError(operation, this, additionalInfo = additionalInfo)
        "system" -> reporter.reportSystemError(operation, this, additionalInfo)
        else -> reporter.reportAppError(operation, this, additionalInfo = additionalInfo)
    }
}

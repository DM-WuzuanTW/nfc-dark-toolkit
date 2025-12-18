package com.wuzuan.nfcdarktoolkit

import android.app.Application
import com.wuzuan.nfcdarktoolkit.utils.DebugReporter
import com.wuzuan.nfcdarktoolkit.utils.Logger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * NFC Dark Toolkit 應用程式類
 */
@HiltAndroidApp
class NfcApp : Application() {
    
    @Inject
    lateinit var debugReporter: DebugReporter
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Debug Reporter
        Logger.debugReporter = debugReporter
        
        // 設置全域異常處理器
        setupGlobalExceptionHandler()
    }
    
    /**
     * 設置全域異常處理器
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                // 回報崩潰到 Discord
                debugReporter.reportSystemError(
                    system = "Application Crash",
                    error = exception,
                    additionalInfo = mapOf(
                        "thread" to thread.name,
                        "crashType" to "Uncaught Exception"
                    )
                )
                
                // 等待一小段時間讓網路請求完成
                Thread.sleep(1000)
            } catch (e: Exception) {
                // 忽略回報過程中的錯誤
            }
            
            // 調用原始處理器
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}

package com.wuzuan.nfcdarktoolkit.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * 性能優化工具類
 * 提供性能監控和優化功能
 */
object PerformanceUtils {
    
    /**
     * 測量執行時間
     */
    suspend inline fun <T> measureExecutionTime(
        tag: String,
        crossinline block: suspend () -> T
    ): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }
        Logger.d("[$tag] 執行時間: ${time}ms")
        return result
    }
    
    /**
     * 在 IO 線程執行
     */
    suspend fun <T> executeOnIO(
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        block()
    }
    
    /**
     * 在主線程執行
     */
    suspend fun <T> executeOnMain(
        block: suspend () -> T
    ): T = withContext(Dispatchers.Main) {
        block()
    }
    
    /**
     * 在計算線程執行
     */
    suspend fun <T> executeOnDefault(
        block: suspend () -> T
    ): T = withContext(Dispatchers.Default) {
        block()
    }
    
    /**
     * 批量處理數據
     */
    suspend fun <T, R> batchProcess(
        items: List<T>,
        batchSize: Int = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        processor: suspend (List<T>) -> List<R>
    ): List<R> = withContext(dispatcher) {
        items.chunked(batchSize).flatMap { batch ->
            processor(batch)
        }
    }
    
    /**
     * 記憶體使用監控
     */
    fun logMemoryUsage(tag: String) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        Logger.d("[$tag] 記憶體使用: ${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB")
        Logger.d("[$tag] 可用記憶體: ${availableMemory / 1024 / 1024}MB")
    }
}

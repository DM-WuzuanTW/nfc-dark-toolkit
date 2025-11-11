package com.wuzuan.nfcdarktoolkit.ui.home

import android.nfc.NdefMessage
import android.nfc.Tag
import com.wuzuan.nfcdarktoolkit.nfc.TagOperations
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 標籤複製輔助類（參考 NFC Tools 的複製功能）
 */
@Singleton
class CloneHelper @Inject constructor(
    private val tagOperations: TagOperations
) {
    
    private var clonedMessage: NdefMessage? = null
    private var isWaitingForTarget = false
    
    /**
     * 開始複製流程 - 讀取來源標籤
     */
    fun startCloning(sourceTag: Tag): Result<String> {
        val result = tagOperations.readTagForCloning(sourceTag)
        
        return if (result.isSuccess) {
            clonedMessage = result.getOrNull()
            isWaitingForTarget = true
            Result.success("來源標籤讀取成功，請靠近目標標籤")
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("讀取失敗"))
        }
    }
    
    /**
     * 完成複製 - 寫入目標標籤
     */
    fun completeCloning(targetTag: Tag): Result<String> {
        if (!isWaitingForTarget || clonedMessage == null) {
            return Result.failure(Exception("請先讀取來源標籤"))
        }
        
        val result = tagOperations.writeTagForCloning(targetTag, clonedMessage!!)
        
        if (result.isSuccess) {
            reset()
            return Result.success("複製成功！")
        } else {
            return Result.failure(result.exceptionOrNull() ?: Exception("寫入失敗"))
        }
    }
    
    /**
     * 取消複製
     */
    fun cancelCloning() {
        reset()
    }
    
    /**
     * 是否正在等待目標標籤
     */
    fun isWaitingForTarget(): Boolean = isWaitingForTarget
    
    /**
     * 重置狀態
     */
    private fun reset() {
        clonedMessage = null
        isWaitingForTarget = false
    }
}


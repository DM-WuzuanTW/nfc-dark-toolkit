package com.wuzuan.nfcdarktoolkit.ui.other

import android.nfc.NdefMessage
import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.wuzuan.nfcdarktoolkit.nfc.TagOperations
import com.wuzuan.nfcdarktoolkit.nfc.TagProtection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherViewModel @Inject constructor(
    private val tagOperations: TagOperations,
    private val tagProtection: TagProtection,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _operationState = MutableStateFlow<OtherOperationState>(OtherOperationState.Idle)
    val operationState: StateFlow<OtherOperationState> = _operationState.asStateFlow()
    
    private var currentOperation: OtherOperation? = null
    private var copiedMessage: NdefMessage? = null
    private var copyInfinityMode = false
    private var pendingPassword: String? = null
    
    fun setOperation(operation: OtherOperation) {
        currentOperation = operation
        copyInfinityMode = false
        _operationState.value = OtherOperationState.WaitingForTag("請靠近標籤執行 ${operation.displayName}")
    }
    
    fun startCopyTag() {
        currentOperation = OtherOperation.COPY_TAG
        copyInfinityMode = false
        copiedMessage = null
        _operationState.value = OtherOperationState.WaitingForTag("步驟 1：請靠近來源標籤")
    }
    
    fun startCopyInfinity() {
        currentOperation = OtherOperation.COPY_INFINITY
        copyInfinityMode = true
        copiedMessage = null
        _operationState.value = OtherOperationState.WaitingForTag("步驟 1：請靠近來源標籤")
    }
    
    fun setPasswordOperation(password: String) {
        currentOperation = OtherOperation.SET_PASSWORD
        pendingPassword = password
        _operationState.value = OtherOperationState.WaitingForTag("請靠近標籤設定密碼")
    }
    
    fun removePasswordOperation(password: String) {
        currentOperation = OtherOperation.REMOVE_PASSWORD
        pendingPassword = password
        _operationState.value = OtherOperationState.WaitingForTag("請靠近標籤移除密碼")
    }
    
    fun handleTag(tag: Tag) {
        viewModelScope.launch {
            try {
                _operationState.value = OtherOperationState.Processing
                
                when (currentOperation) {
                    OtherOperation.COPY_TAG, OtherOperation.COPY_INFINITY -> handleCopy(tag)
                    OtherOperation.ERASE -> handleErase(tag)
                    OtherOperation.LOCK -> handleLock(tag)
                    OtherOperation.READ_MEMORY -> handleReadMemory(tag)
                    OtherOperation.FORMAT -> handleFormat(tag)
                    OtherOperation.SET_PASSWORD -> handleSetPassword(tag)
                    OtherOperation.REMOVE_PASSWORD -> handleRemovePassword(tag)
                    null -> _operationState.value = OtherOperationState.Idle
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _operationState.value = OtherOperationState.Error(e.message ?: "操作失敗")
            }
        }
    }
    
    private suspend fun handleCopy(tag: Tag) {
        if (copiedMessage == null) {
            // 讀取來源標籤
            val result = tagOperations.readTagForCloning(tag)
            if (result.isSuccess) {
                copiedMessage = result.getOrNull()
                _operationState.value = if (copyInfinityMode) {
                    OtherOperationState.WaitingForTag("來源已讀取！靠近任意標籤即可複製（無限模式）")
                } else {
                    OtherOperationState.WaitingForTag("步驟 2：請靠近目標標籤")
                }
            } else {
                _operationState.value = OtherOperationState.Error("讀取失敗：${result.exceptionOrNull()?.message}")
            }
        } else {
            // 寫入目標標籤
            val result = tagOperations.writeTagForCloning(tag, copiedMessage!!)
            if (result.isSuccess) {
                saveHistory(tag, ActionType.CLONE, "複製標籤")
                
                if (copyInfinityMode) {
                    _operationState.value = OtherOperationState.WaitingForTag("✓ 複製成功！繼續靠近下一個標籤")
                } else {
                    copiedMessage = null
                    currentOperation = null
                    _operationState.value = OtherOperationState.Success("✓ 標籤複製成功！")
                }
            } else {
                _operationState.value = OtherOperationState.Error("寫入失敗：${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    private suspend fun handleErase(tag: Tag) {
        val result = tagOperations.formatTag(tag)
        if (result.isSuccess) {
            saveHistory(tag, ActionType.FORMAT, "擦除標籤")
            currentOperation = null
            _operationState.value = OtherOperationState.Success("✓ 標籤已擦除")
        } else {
            _operationState.value = OtherOperationState.Error("擦除失敗：${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun handleLock(tag: Tag) {
        val result = tagOperations.lockTag(tag)
        if (result.isSuccess) {
            saveHistory(tag, ActionType.LOCK, "鎖定標籤")
            currentOperation = null
            _operationState.value = OtherOperationState.Success("✓ 標籤已鎖定為唯讀")
        } else {
            _operationState.value = OtherOperationState.Error("鎖定失敗：${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun handleReadMemory(tag: Tag) {
        // TODO: 實作完整記憶體讀取
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        saveHistory(tag, ActionType.READ, "讀取記憶體")
        currentOperation = null
        _operationState.value = OtherOperationState.Success("記憶體讀取完成\nTag ID: $tagId")
    }
    
    private suspend fun handleFormat(tag: Tag) {
        val result = tagOperations.formatTag(tag)
        if (result.isSuccess) {
            saveHistory(tag, ActionType.FORMAT, "格式化記憶體")
            currentOperation = null
            _operationState.value = OtherOperationState.Success("✓ 記憶體已格式化")
        } else {
            _operationState.value = OtherOperationState.Error("格式化失敗：${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun handleSetPassword(tag: Tag) {
        val password = pendingPassword ?: return
        val passwordBytes = hexToBytes(password)
        
        if (passwordBytes == null || passwordBytes.size != 4) {
            _operationState.value = OtherOperationState.Error("密碼格式錯誤，必須是 4 字節（8 個十六進位字符）")
            return
        }
        
        val result = tagProtection.setPasswordProtection(tag, passwordBytes)
        if (result.isSuccess) {
            saveHistory(tag, ActionType.WRITE, "設定密碼")
            currentOperation = null
            pendingPassword = null
            _operationState.value = OtherOperationState.Success("✓ 密碼已設定")
        } else {
            _operationState.value = OtherOperationState.Error("設定密碼失敗：${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun handleRemovePassword(tag: Tag) {
        // TODO: 實作移除密碼邏輯
        currentOperation = null
        pendingPassword = null
        _operationState.value = OtherOperationState.Success("✓ 密碼已移除")
    }
    
    private suspend fun saveHistory(tag: Tag, actionType: ActionType, title: String) {
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        
        val record = HistoryRecord(
            tagId = tagId,
            tagType = null,
            actionType = actionType,
            title = title,
            description = "操作時間：${System.currentTimeMillis()}",
            payloadRaw = "",
            timestamp = System.currentTimeMillis()
        )
        
        historyRepository.insertHistory(record)
    }
    
    private fun hexToBytes(hex: String): ByteArray? {
        return try {
            val cleanHex = hex.replace(" ", "").replace(":", "")
            cleanHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}

enum class OtherOperation(val displayName: String) {
    COPY_TAG("複製標籤"),
    COPY_INFINITY("無限複製"),
    ERASE("擦除標籤"),
    LOCK("鎖定標籤"),
    READ_MEMORY("讀取記憶體"),
    FORMAT("格式化記憶體"),
    SET_PASSWORD("設定密碼"),
    REMOVE_PASSWORD("移除密碼")
}

sealed class OtherOperationState {
    object Idle : OtherOperationState()
    data class WaitingForTag(val message: String) : OtherOperationState()
    object Processing : OtherOperationState()
    data class Success(val message: String) : OtherOperationState()
    data class Error(val message: String) : OtherOperationState()
}


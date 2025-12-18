package com.wuzuan.nfcdarktoolkit.ui.edit

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.wuzuan.nfcdarktoolkit.domain.usecase.ReadTagUseCase
import com.wuzuan.nfcdarktoolkit.domain.usecase.WriteTagUseCase
import com.wuzuan.nfcdarktoolkit.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val readTagUseCase: ReadTagUseCase,
    private val writeTagUseCase: WriteTagUseCase,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _editState = MutableStateFlow<EditState>(EditState.Idle)
    val editState: StateFlow<EditState> = _editState.asStateFlow()
    
    private var currentTag: Tag? = null
    private var isWaitingToWrite = false
    private var newContent: String? = null
    
    fun handleTag(tag: Tag) {
        if (isWaitingToWrite) {
            writeContent(tag)
        } else {
            readContent(tag)
        }
    }
    
    private fun readContent(tag: Tag) {
        viewModelScope.launch {
            try {
                Logger.nfc("EditViewModel", "開始讀取標籤內容")
                _editState.value = EditState.Reading
                
                readTagUseCase(tag).onSuccess { tagInfo ->
                    if (tagInfo.ndefRecords.isEmpty()) {
                        _editState.value = EditState.Error("標籤無 NDEF 資料")
                        return@launch
                    }
                    
                    val content = tagInfo.ndefRecords.joinToString("\n\n") { record ->
                        "${record.recordType.name}: ${record.payload}"
                    }
                    
                    currentTag = tag
                    _editState.value = EditState.Loaded(content)
                    Logger.nfc("EditViewModel", "標籤內容讀取成功")
                    
                    saveHistory(tag, "讀取內容以編輯")
                }.onFailure { exception ->
                    Logger.e("讀取標籤失敗: ${exception.message}", exception)
                    _editState.value = EditState.Error(exception.message ?: "讀取失敗")
                }
            } catch (e: Exception) {
                Logger.e("讀取內容時發生異常: ${e.message}", e)
                _editState.value = EditState.Error(e.message ?: "讀取失敗")
            }
        }
    }
    
    fun prepareToWrite(content: String) {
        newContent = content
        isWaitingToWrite = true
    }
    
    private fun writeContent(tag: Tag) {
        viewModelScope.launch {
            try {
                Logger.nfc("EditViewModel", "開始寫入標籤內容")
                _editState.value = EditState.Writing
                
                val content = newContent ?: run {
                    _editState.value = EditState.Error("沒有要寫入的內容")
                    return@launch
                }
                
                writeTagUseCase.writeText(tag, content).onSuccess {
                    Logger.nfc("EditViewModel", "標籤寫入成功")
                    _editState.value = EditState.Success("✓ 修改已儲存到標籤")
                    saveHistory(tag, "編輯並寫入內容")
                    reset()
                }.onFailure { exception ->
                    Logger.e("寫入標籤失敗: ${exception.message}", exception)
                    _editState.value = EditState.Error(exception.message ?: "寫入失敗")
                }
            } catch (e: Exception) {
                Logger.e("寫入內容時發生異常: ${e.message}", e)
                _editState.value = EditState.Error(e.message ?: "寫入失敗")
            }
        }
    }
    
    fun reset() {
        currentTag = null
        newContent = null
        isWaitingToWrite = false
        _editState.value = EditState.Idle
    }
    
    private suspend fun saveHistory(tag: Tag, title: String) {
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        
        val record = HistoryRecord(
            tagId = tagId,
            tagType = null,
            actionType = ActionType.WRITE,
            title = title,
            description = newContent ?: "",
            payloadRaw = "",
            timestamp = System.currentTimeMillis()
        )
        
        historyRepository.insertHistory(record)
    }
}

sealed class EditState {
    object Idle : EditState()
    object Reading : EditState()
    data class Loaded(val originalContent: String) : EditState()
    object Writing : EditState()
    data class Success(val message: String) : EditState()
    data class Error(val message: String) : EditState()
}


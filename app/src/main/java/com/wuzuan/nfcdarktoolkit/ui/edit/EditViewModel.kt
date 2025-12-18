package com.wuzuan.nfcdarktoolkit.ui.edit

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.wuzuan.nfcdarktoolkit.nfc.NdefReader
import com.wuzuan.nfcdarktoolkit.nfc.NdefWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val ndefReader: NdefReader,
    private val ndefWriter: NdefWriter,
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
                _editState.value = EditState.Reading
                
                val records = ndefReader.readNdefFromTag(tag)
                if (records.isEmpty()) {
                    _editState.value = EditState.Error("標籤無資料")
                    return@launch
                }
                
                val content = records.joinToString("\n\n") { record ->
                    "${record.recordType}：${record.payload}"
                }
                
                currentTag = tag
                _editState.value = EditState.Loaded(content)
                
                saveHistory(tag, "讀取內容以編輯")
            } catch (e: Exception) {
                e.printStackTrace()
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
                _editState.value = EditState.Writing
                
                val content = newContent ?: return@launch
                val result = ndefWriter.writeText(tag, content)
                
                if (result.isSuccess) {
                    _editState.value = EditState.Success("✓ 修改已儲存到標籤")
                    saveHistory(tag, "編輯並寫入內容")
                    reset()
                } else {
                    _editState.value = EditState.Error(result.exceptionOrNull()?.message ?: "寫入失敗")
                }
            } catch (e: Exception) {
                e.printStackTrace()
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


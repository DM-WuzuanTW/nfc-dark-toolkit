package com.wuzuan.nfcdarktoolkit.ui.home

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.data.repository.SettingsRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.wuzuan.nfcdarktoolkit.domain.model.TagInfo
import com.wuzuan.nfcdarktoolkit.nfc.NdefReader
import com.wuzuan.nfcdarktoolkit.nfc.NfcManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 掃描 ViewModel
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val nfcManager: NfcManager,
    private val ndefReader: NdefReader,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    fun onTagDetected(tag: Tag) {
        viewModelScope.launch {
            try {
                _uiState.value = ScanUiState.Reading
                
                // 解析標籤資訊
                val tagInfo = nfcManager.parseTagInfo(tag)
                
                // 讀取 NDEF 資料
                val ndefRecords = ndefReader.readNdefFromTag(tag)
                val completeTagInfo = tagInfo.copy(ndefRecords = ndefRecords)
                
                _uiState.value = ScanUiState.Success(
                    tagInfo = completeTagInfo,
                    rawTag = tag
                )
                
                // 儲存歷史記錄
                saveHistory(completeTagInfo)
                
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ScanUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    private suspend fun saveHistory(tagInfo: TagInfo) {
        val autoSave = settingsRepository.autoSaveHistory.first()
        if (!autoSave) return
        
        val record = HistoryRecord(
            tagId = tagInfo.id,
            tagType = tagInfo.type.name,
            actionType = ActionType.READ,
            title = "讀取標籤",
            description = "標籤類型: ${tagInfo.type.name}, 技術: ${tagInfo.techList.joinToString(", ")}",
            payloadRaw = tagInfo.ndefRecords.joinToString("\n") { it.payload },
            timestamp = System.currentTimeMillis()
        )
        
        historyRepository.insertHistory(record)
    }
}

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Reading : ScanUiState()
    data class Success(val tagInfo: TagInfo, val rawTag: Tag? = null) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}


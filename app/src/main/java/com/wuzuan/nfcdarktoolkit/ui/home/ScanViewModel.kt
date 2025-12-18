package com.wuzuan.nfcdarktoolkit.ui.home

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.data.repository.SettingsRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.wuzuan.nfcdarktoolkit.domain.model.TagInfo
import com.wuzuan.nfcdarktoolkit.domain.usecase.ReadTagUseCase
import com.wuzuan.nfcdarktoolkit.utils.Logger
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
    private val readTagUseCase: ReadTagUseCase,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    fun onTagDetected(tag: Tag) {
        viewModelScope.launch {
            try {
                val tagId = tag.id.joinToString(":") { "%02X".format(it) }
                Logger.nfc("ScanViewModel", "偵測到標籤: $tagId")
                Logger.nfc("ScanViewModel", "標籤技術: ${tag.techList.joinToString()}")
                
                _uiState.value = ScanUiState.Reading
                
                // 直接嘗試讀取，不做預先檢查
                readTagUseCase(tag).onSuccess { tagInfo ->
                    Logger.nfc("ScanViewModel", "標籤讀取成功: ${tagInfo.id}")
                    _uiState.value = ScanUiState.Success(tagInfo, tag)
                    saveHistory(tagInfo)
                }.onFailure { exception ->
                    Logger.e("標籤讀取失敗: ${exception.message}", exception)
                    _uiState.value = ScanUiState.Error("讀取失敗: ${exception.message}")
                }
                
            } catch (e: Exception) {
                Logger.e("處理標籤時發生異常: ${e.message}", e)
                _uiState.value = ScanUiState.Error("處理標籤時發生錯誤: ${e.message}")
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


package com.wuzuan.nfcdarktoolkit.ui.write

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.domain.model.ActionType
import com.wuzuan.nfcdarktoolkit.domain.model.HistoryRecord
import com.wuzuan.nfcdarktoolkit.nfc.NdefWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 專業級寫入 ViewModel
 */
@HiltViewModel
class WriteProfessionalViewModel @Inject constructor(
    private val ndefWriter: NdefWriter,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _selectedType = MutableStateFlow(WriteType.TEXT)
    val selectedType: StateFlow<WriteType> = _selectedType.asStateFlow()
    
    private val _writeState = MutableStateFlow<WriteProfessionalState>(WriteProfessionalState.Idle)
    val writeState: StateFlow<WriteProfessionalState> = _writeState.asStateFlow()
    
    fun setWriteType(type: WriteType) {
        _selectedType.value = type
        _writeState.value = WriteProfessionalState.Idle
    }
    
    fun writeToTag(tag: Tag, input: String) {
        if (input.isBlank()) {
            _writeState.value = WriteProfessionalState.Error("輸入不能為空")
            return
        }
        
        viewModelScope.launch {
            try {
                _writeState.value = WriteProfessionalState.Writing
                
                val finalContent = processInput(input, _selectedType.value)
                val result = ndefWriter.writeUri(tag, finalContent)
                
                if (result.isSuccess) {
                    _writeState.value = WriteProfessionalState.Success("✓ 寫入成功！")
                    saveHistory(tag, _selectedType.value, input, finalContent)
                } else {
                    _writeState.value = WriteProfessionalState.Error(
                        result.exceptionOrNull()?.message ?: "寫入失敗"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _writeState.value = WriteProfessionalState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    /**
     * 處理輸入，轉換為正確格式
     */
    private fun processInput(input: String, type: WriteType): String {
        return when (type) {
            WriteType.TEXT -> input
            WriteType.URL -> input
            WriteType.SEARCH -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
            
            // 社交網路
            WriteType.SOCIAL_DISCORD,
            WriteType.SOCIAL_INSTAGRAM,
            WriteType.SOCIAL_FACEBOOK,
            WriteType.SOCIAL_LINE,
            WriteType.SOCIAL_TELEGRAM,
            WriteType.SOCIAL_TWITTER,
            WriteType.SOCIAL_YOUTUBE,
            WriteType.SOCIAL_TIKTOK -> SocialUrlBuilder.buildUrl(type, input)
            
            WriteType.VIDEO -> input
            WriteType.FILE -> input
            WriteType.APPLICATION -> "market://details?id=$input"
            WriteType.MAIL -> "mailto:$input"
            WriteType.CONTACT -> input // vCard 格式另外處理
            WriteType.PHONE -> "tel:$input"
            WriteType.SMS -> "sms:?body=$input"
            WriteType.LOCATION -> {
                val coords = input.split(",").map { it.trim() }
                if (coords.size == 2) {
                    "geo:${coords[0]},${coords[1]}"
                } else {
                    "geo:$input"
                }
            }
            WriteType.ADDRESS -> "geo:0,0?q=$input"
            WriteType.BITCOIN -> "bitcoin:$input"
            WriteType.BLUETOOTH -> input // 藍牙配對另外處理
            WriteType.WIFI -> input // WiFi 配置另外處理
        }
    }
    
    private suspend fun saveHistory(tag: Tag, type: WriteType, input: String, finalContent: String) {
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        
        val record = HistoryRecord(
            tagId = tagId,
            tagType = null,
            actionType = ActionType.WRITE,
            title = "寫入${type.displayName}",
            description = "內容: $input",
            payloadRaw = finalContent,
            timestamp = System.currentTimeMillis()
        )
        
        historyRepository.insertHistory(record)
    }
}

sealed class WriteProfessionalState {
    object Idle : WriteProfessionalState()
    object Writing : WriteProfessionalState()
    data class Success(val message: String) : WriteProfessionalState()
    data class Error(val message: String) : WriteProfessionalState()
}


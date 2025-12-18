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
    
    fun writeToTag(tag: Tag, input: String, wifiPassword: String? = null, smsPhone: String? = null, contactName: String? = null, contactPhone: String? = null, contactEmail: String? = null) {
        viewModelScope.launch {
            try {
                _writeState.value = WriteProfessionalState.Writing
                
                val result: Result<String> = when (_selectedType.value) {
                    WriteType.WIFI -> ndefWriter.writeWifi(tag, input, wifiPassword)
                    WriteType.SMS -> ndefWriter.writeSms(tag, smsPhone ?: "", input)
                    WriteType.CONTACT -> ndefWriter.writeVCard(tag, contactName, contactPhone, contactEmail)
                    else -> {
                        val finalContent = processInput(input, _selectedType.value)
                        ndefWriter.writeUri(tag, finalContent).map { finalContent } 
                    }
                }
                
                if (result.isSuccess) {
                    _writeState.value = WriteProfessionalState.Success("✓ 寫入成功！")
                    
                    // 根據類型產生詳細描述
                    val description = when (val type = _selectedType.value) {
                        WriteType.WIFI -> "SSID: $input\n密碼: ${wifiPassword ?: "無"}"
                        WriteType.SMS -> "收件人: ${smsPhone ?: "無"}\n內容: $input"
                        WriteType.CONTACT -> "姓名: $contactName\n電話: $contactPhone\nEmail: $contactEmail"
                        WriteType.LOCATION -> "地理位置: $input"
                        WriteType.ADDRESS -> "地址: $input"
                        WriteType.SOCIAL_DISCORD,
                        WriteType.SOCIAL_INSTAGRAM,
                        WriteType.SOCIAL_FACEBOOK,
                        WriteType.SOCIAL_LINE,
                        WriteType.SOCIAL_TELEGRAM,
                        WriteType.SOCIAL_TWITTER,
                        WriteType.SOCIAL_YOUTUBE,
                        WriteType.SOCIAL_TIKTOK -> "${type.displayName}: $input"
                        else -> "內容: $input"
                    }
                    
                    saveHistory(tag, _selectedType.value, description, result.getOrNull() ?: "")
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
            WriteType.PHONE -> "tel:$input"
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
            else -> ""
        }
    }
    
    private suspend fun saveHistory(tag: Tag, type: WriteType, description: String, payload: String) {
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        
        val record = HistoryRecord(
            tagId = tagId,
            tagType = null,
            actionType = ActionType.WRITE,
            title = "寫入${type.displayName}",
            description = description,
            payloadRaw = payload,
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

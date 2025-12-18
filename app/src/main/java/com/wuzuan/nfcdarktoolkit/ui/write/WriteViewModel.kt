package com.wuzuan.nfcdarktoolkit.ui.write

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.HistoryRepository
import com.wuzuan.nfcdarktoolkit.data.repository.SettingsRepository
import com.wuzuan.nfcdarktoolkit.domain.model.*
import com.wuzuan.nfcdarktoolkit.nfc.NdefWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 寫入 ViewModel
 */
@HiltViewModel
class WriteViewModel @Inject constructor(
    private val ndefWriter: NdefWriter,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WriteUiState>(WriteUiState.Idle)
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()
    
    private val _writeType = MutableStateFlow(WriteType.TEXT)
    val writeType: StateFlow<WriteType> = _writeType.asStateFlow()
    
    init {
        // 載入預設寫入格式
        viewModelScope.launch {
            val defaultFormat = settingsRepository.defaultWriteFormat.first()
            _writeType.value = when (defaultFormat) {
                "text" -> WriteType.TEXT
                "url" -> WriteType.URL
                "wifi" -> WriteType.WIFI
                "vcard" -> WriteType.CONTACT
                "json" -> WriteType.TEXT
                else -> WriteType.TEXT
            }
        }
    }
    
    fun setWriteType(type: WriteType) {
        _writeType.value = type
        _uiState.value = WriteUiState.Idle
    }
    
    fun writeText(tag: Tag, text: String, languageCode: String = "zh") {
        if (text.isBlank()) {
            _uiState.value = WriteUiState.Error("文字不能為空")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = WriteUiState.Writing
                
                val result = ndefWriter.writeText(tag, text, languageCode)
                
                if (result.isSuccess) {
                    _uiState.value = WriteUiState.Success("文字寫入成功")
                    saveHistory(tag, "文字", text)
                } else {
                    _uiState.value = WriteUiState.Error(
                        result.exceptionOrNull()?.message ?: "寫入失敗"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WriteUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    fun writeUri(tag: Tag, uri: String) {
        if (uri.isBlank()) {
            _uiState.value = WriteUiState.Error("網址不能為空")
            return
        }
        
        if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
            _uiState.value = WriteUiState.Error("網址必須以 http:// 或 https:// 開頭")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = WriteUiState.Writing
                
                val result = ndefWriter.writeUri(tag, uri)
                
                if (result.isSuccess) {
                    _uiState.value = WriteUiState.Success("網址寫入成功")
                    saveHistory(tag, "網址", uri)
                } else {
                    _uiState.value = WriteUiState.Error(
                        result.exceptionOrNull()?.message ?: "寫入失敗"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WriteUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    fun writeWiFi(tag: Tag, ssid: String, password: String?, securityType: WiFiSecurityType) {
        if (ssid.isBlank()) {
            _uiState.value = WriteUiState.Error("Wi-Fi 名稱不能為空")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = WriteUiState.Writing
                
                val wifiContent = NdefContent.WiFi(ssid, password, securityType)
                val result = ndefWriter.writeCustom(tag, wifiContent)
                
                if (result.isSuccess) {
                    _uiState.value = WriteUiState.Success("Wi-Fi 設定寫入成功")
                    saveHistory(tag, "Wi-Fi", "SSID: $ssid")
                } else {
                    _uiState.value = WriteUiState.Error(
                        result.exceptionOrNull()?.message ?: "寫入失敗"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WriteUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    fun writeVCard(
        tag: Tag,
        name: String?,
        phone: String?,
        email: String?,
        company: String?,
        title: String?,
        address: String?,
        website: String?
    ) {
        if (name.isNullOrBlank() && phone.isNullOrBlank() && email.isNullOrBlank()) {
            _uiState.value = WriteUiState.Error("至少需要填寫姓名、電話或 Email")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = WriteUiState.Writing
                
                val vcardContent = NdefContent.VCard(
                    name = name,
                    phone = phone,
                    email = email,
                    company = company,
                    title = title,
                    address = address,
                    website = website
                )
                
                val result = ndefWriter.writeCustom(tag, vcardContent)
                
                if (result.isSuccess) {
                    _uiState.value = WriteUiState.Success("名片寫入成功")
                    saveHistory(tag, "名片", name ?: phone ?: email ?: "")
                } else {
                    _uiState.value = WriteUiState.Error(
                        result.exceptionOrNull()?.message ?: "寫入失敗"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WriteUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    fun writeJson(tag: Tag, json: String) {
        if (json.isBlank()) {
            _uiState.value = WriteUiState.Error("JSON 內容不能為空")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = WriteUiState.Writing
                
                val jsonContent = NdefContent.Json(json)
                val result = ndefWriter.writeCustom(tag, jsonContent)
                
                if (result.isSuccess) {
                    _uiState.value = WriteUiState.Success("JSON 資料寫入成功")
                    saveHistory(tag, "JSON", json.take(50))
                } else {
                    _uiState.value = WriteUiState.Error(
                        result.exceptionOrNull()?.message ?: "寫入失敗"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WriteUiState.Error(e.message ?: "未知錯誤")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = WriteUiState.Idle
    }
    
    private suspend fun saveHistory(tag: Tag, type: String, content: String) {
        val autoSave = settingsRepository.autoSaveHistory.first()
        if (!autoSave) return
        
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        
        val record = HistoryRecord(
            tagId = tagId,
            tagType = null,
            actionType = ActionType.WRITE,
            title = "寫入$type",
            description = content,
            payloadRaw = content,
            timestamp = System.currentTimeMillis()
        )
        
        historyRepository.insertHistory(record)
    }
}

sealed class WriteUiState {
    object Idle : WriteUiState()
    object Writing : WriteUiState()
    data class Success(val message: String) : WriteUiState()
    data class Error(val message: String) : WriteUiState()
}


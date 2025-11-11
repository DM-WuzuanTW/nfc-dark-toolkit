package com.wuzuan.nfcdarktoolkit.ui.emulate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.nfc.HceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HCE 模擬 ViewModel
 */
@HiltViewModel
class EmulateViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow<EmulateUiState>(EmulateUiState.Inactive)
    val uiState: StateFlow<EmulateUiState> = _uiState.asStateFlow()
    
    private val _aid = MutableStateFlow("F0010203040506")
    val aid: StateFlow<String> = _aid.asStateFlow()
    
    private val _responseData = MutableStateFlow("")
    val responseData: StateFlow<String> = _responseData.asStateFlow()
    
    fun setAid(aid: String) {
        _aid.value = aid.replace(" ", "").replace(":", "")
    }
    
    fun setResponseData(data: String) {
        _responseData.value = data.replace(" ", "")
    }
    
    fun startEmulation() {
        val aidValue = _aid.value
        val responseValue = _responseData.value
        
        // 驗證 AID
        if (!isValidHex(aidValue) || aidValue.length < 10) {
            _uiState.value = EmulateUiState.Error("AID 格式錯誤，必須是至少 5 個字節的十六進位")
            return
        }
        
        // 驗證回應資料（如果有）
        if (responseValue.isNotEmpty() && !isValidHex(responseValue)) {
            _uiState.value = EmulateUiState.Error("回應資料格式錯誤，必須是十六進位")
            return
        }
        
        viewModelScope.launch {
            try {
                // 設定 HCE Service
                HceService.customResponseData = if (responseValue.isNotEmpty()) {
                    hexStringToByteArray(responseValue)
                } else {
                    null
                }
                HceService.isActive = true
                
                _uiState.value = EmulateUiState.Active(
                    aid = aidValue,
                    responseData = responseValue.ifEmpty { "無" }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = EmulateUiState.Error(e.message ?: "啟動失敗")
            }
        }
    }
    
    fun stopEmulation() {
        HceService.isActive = false
        HceService.customResponseData = null
        _uiState.value = EmulateUiState.Inactive
    }
    
    private fun isValidHex(hex: String): Boolean {
        return hex.matches(Regex("[0-9A-Fa-f]+"))
    }
    
    private fun hexStringToByteArray(hex: String): ByteArray {
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}

sealed class EmulateUiState {
    object Inactive : EmulateUiState()
    data class Active(val aid: String, val responseData: String) : EmulateUiState()
    data class Error(val message: String) : EmulateUiState()
}


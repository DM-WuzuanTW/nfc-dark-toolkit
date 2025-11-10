package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Host Card Emulation Service
 * 用於模擬 NFC 卡片
 */
class HceService : HostApduService() {
    
    companion object {
        private const val TAG = "HceService"
        
        // 成功狀態碼
        private val SW_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        
        // 錯誤狀態碼
        private val SW_UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        private val SW_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())
        
        // 預設回應資料
        var customResponseData: ByteArray? = null
        var isActive: Boolean = false
    }
    
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return SW_UNKNOWN
        }
        
        Log.d(TAG, "Received APDU: ${bytesToHex(commandApdu)}")
        
        // 如果 HCE 服務未啟用，返回錯誤
        if (!isActive) {
            return SW_NOT_FOUND
        }
        
        // SELECT AID 指令 (CLA=00, INS=A4, P1=04, P2=00)
        if (commandApdu.size >= 4 && 
            commandApdu[0] == 0x00.toByte() && 
            commandApdu[1] == 0xA4.toByte() && 
            commandApdu[2] == 0x04.toByte()) {
            
            Log.d(TAG, "SELECT AID command received")
            
            // 返回成功狀態
            return customResponseData?.plus(SW_SUCCESS) ?: SW_SUCCESS
        }
        
        // 其他 APDU 指令
        return customResponseData?.plus(SW_SUCCESS) ?: SW_UNKNOWN
    }
    
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Service deactivated, reason: $reason")
    }
    
    /**
     * 將 Byte Array 轉換為十六進位字串
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }
}


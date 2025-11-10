package com.wuzuan.nfcdarktoolkit.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.*
import com.wuzuan.nfcdarktoolkit.domain.model.TagInfo
import com.wuzuan.nfcdarktoolkit.domain.model.TagType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NFC 管理器 - 封裝 NFC 基本操作
 */
@Singleton
class NfcManager @Inject constructor(
    private val nfcAdapter: NfcAdapter?
) {
    
    /**
     * 檢查設備是否支援 NFC
     */
    fun isNfcSupported(): Boolean {
        return nfcAdapter != null
    }
    
    /**
     * 檢查 NFC 是否已啟用
     */
    fun isNfcEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * 啟用前景調度
     */
    fun enableForegroundDispatch(activity: Activity) {
        if (nfcAdapter == null) return
        
        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            activity, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null)
    }
    
    /**
     * 禁用前景調度
     */
    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }
    
    /**
     * 從 Intent 解析 Tag
     */
    fun getTagFromIntent(intent: Intent?): Tag? {
        return intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
    }
    
    /**
     * 解析標籤基本資訊
     */
    fun parseTagInfo(tag: Tag): TagInfo {
        val id = bytesToHexString(tag.id)
        val techList = tag.techList.map { it.substringAfterLast('.') }
        val tagType = detectTagType(tag)
        
        // 檢查是否可寫入
        var isWritable = false
        var maxSize: Int? = null
        var currentSize: Int? = null
        
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                isWritable = ndef.isWritable
                maxSize = ndef.maxSize
                currentSize = ndef.cachedNdefMessage?.toByteArray()?.size
                ndef.close()
            } else {
                // 檢查是否為可格式化標籤
                val ndefFormatable = NdefFormatable.get(tag)
                isWritable = ndefFormatable != null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return TagInfo(
            id = id,
            techList = techList,
            type = tagType,
            isWritable = isWritable,
            maxSize = maxSize,
            currentSize = currentSize
        )
    }
    
    /**
     * 偵測標籤類型
     */
    private fun detectTagType(tag: Tag): TagType {
        val techList = tag.techList
        
        return when {
            techList.contains(MifareClassic::class.java.name) -> TagType.MIFARE_CLASSIC
            techList.contains(MifareUltralight::class.java.name) -> {
                // 進一步判斷是否為 NTAG
                try {
                    val ultralight = MifareUltralight.get(tag)
                    ultralight.connect()
                    val type = ultralight.type
                    ultralight.close()
                    
                    when (type) {
                        MifareUltralight.TYPE_ULTRALIGHT -> TagType.MIFARE_ULTRALIGHT
                        MifareUltralight.TYPE_ULTRALIGHT_C -> TagType.MIFARE_ULTRALIGHT
                        else -> TagType.NTAG
                    }
                } catch (e: Exception) {
                    TagType.MIFARE_ULTRALIGHT
                }
            }
            techList.contains(IsoDep::class.java.name) -> TagType.ISO_14443_4
            techList.contains(NfcA::class.java.name) -> TagType.NFC_A
            techList.contains(NfcB::class.java.name) -> TagType.NFC_B
            techList.contains(NfcF::class.java.name) -> TagType.NFC_F
            techList.contains(NfcV::class.java.name) -> TagType.NFC_V
            techList.contains(Ndef::class.java.name) -> TagType.NDEF
            else -> TagType.UNKNOWN
        }
    }
    
    /**
     * 將 Byte Array 轉換為十六進位字串
     */
    fun bytesToHexString(bytes: ByteArray): String {
        return bytes.joinToString(":") { byte ->
            "%02X".format(byte)
        }
    }
    
    /**
     * 將十六進位字串轉換為 Byte Array
     */
    fun hexStringToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace(":", "").replace(" ", "")
        return cleanHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}


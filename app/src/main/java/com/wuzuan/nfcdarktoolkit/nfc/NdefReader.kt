package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import com.wuzuan.nfcdarktoolkit.domain.model.*
import com.wuzuan.nfcdarktoolkit.domain.exception.NdefParseException
import com.wuzuan.nfcdarktoolkit.domain.exception.TagReadException
import com.wuzuan.nfcdarktoolkit.nfc.UriPrefixConstants
import com.wuzuan.nfcdarktoolkit.utils.Logger
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NDEF 讀取器
 */
@Singleton
class NdefReader @Inject constructor() {
    
    /**
     * 讀取標籤的 NDEF 資料
     */
    fun readNdefFromTag(tag: Tag): List<NdefRecordData> {
        val ndef = Ndef.get(tag)
        
        if (ndef != null) {
            return readFromNdefTag(ndef)
        }
        
        // 如果不是 NDEF 標籤，嘗試其他技術
        return readFromOtherTech(tag)
    }
    
    /**
     * 從 NDEF 標籤讀取資料
     */
    private fun readFromNdefTag(ndef: Ndef): List<NdefRecordData> {
        return try {
            if (!ndef.isConnected) {
                ndef.connect()
            }
            
            try {
                // 先嘗試快取的訊息
                var ndefMessage = ndef.cachedNdefMessage
                
                // 如果沒有快取，主動讀取
                if (ndefMessage == null) {
                    Logger.nfc("ReadNdef", "無快取資料，主動讀取標籤")
                    ndefMessage = ndef.ndefMessage
                }
                
                if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                    Logger.nfc("ReadNdef", "成功讀取 ${ndefMessage.records.size} 筆記錄")
                    parseNdefMessage(ndefMessage)
                } else {
                    Logger.nfc("ReadNdef", "標籤無 NDEF 資料或為空標籤")
                    emptyList()
                }
            } finally {
                try {
                    if (ndef.isConnected) {
                        ndef.close()
                    }
                } catch (e: Exception) {
                    Logger.w("關閉 NDEF 連接時發生錯誤: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Logger.e("讀取 NDEF 標籤失敗: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * 嘗試從其他技術讀取資料
     */
    private fun readFromOtherTech(tag: Tag): List<NdefRecordData> {
        Logger.nfc("ReadNdef", "標籤不支援 NDEF，技術列表: ${tag.techList.joinToString()}")
        
        // 對於非 NDEF 標籤，返回基本資訊
        return listOf(
            NdefRecordData(
                tnf = 0,
                type = "application/octet-stream",
                id = null,
                payload = "標籤 ID: ${tag.id.joinToString(":") { "%02X".format(it) }}",
                recordType = NdefRecordType.UNKNOWN
            )
        )
    }
    
    /**
     * 解析 NDEF Message
     */
    fun parseNdefMessage(message: NdefMessage): List<NdefRecordData> {
        return message.records.map { record ->
            parseNdefRecord(record)
        }
    }
    
    /**
     * 解析 NDEF Record
     */
    fun parseNdefRecord(record: NdefRecord): NdefRecordData {
        val tnf = record.tnf
        val type = String(record.type, Charset.forName("UTF-8"))
        val id = record.id?.let { String(it, Charset.forName("UTF-8")) }
        val payload = record.payload
        
        val recordType = detectRecordType(record)
        val payloadString = when (recordType) {
            NdefRecordType.TEXT -> parseTextRecord(record)
            NdefRecordType.URI -> parseUriRecord(record)
            else -> bytesToHexString(payload)
        }
        
        return NdefRecordData(
            tnf = tnf,
            type = type,
            id = id,
            payload = payloadString,
            recordType = recordType
        )
    }
    
    /**
     * 偵測 Record 類型
     */
    private fun detectRecordType(record: NdefRecord): NdefRecordType {
        return when (record.tnf) {
            NdefRecord.TNF_EMPTY -> NdefRecordType.EMPTY
            NdefRecord.TNF_WELL_KNOWN -> {
                when {
                    record.type.contentEquals(NdefRecord.RTD_TEXT) -> NdefRecordType.TEXT
                    record.type.contentEquals(NdefRecord.RTD_URI) -> NdefRecordType.URI
                    record.type.contentEquals(NdefRecord.RTD_SMART_POSTER) -> NdefRecordType.SMART_POSTER
                    else -> NdefRecordType.UNKNOWN
                }
            }
            NdefRecord.TNF_MIME_MEDIA -> NdefRecordType.MIME
            NdefRecord.TNF_ABSOLUTE_URI -> NdefRecordType.ABSOLUTE_URI
            NdefRecord.TNF_EXTERNAL_TYPE -> NdefRecordType.EXTERNAL
            else -> NdefRecordType.UNKNOWN
        }
    }
    
    /**
     * 解析文字 Record
     */
    private fun parseTextRecord(record: NdefRecord): String {
        return try {
            val payload = record.payload
            
            if (payload.isEmpty()) {
                throw NdefParseException("文字 Record payload 為空")
            }
            
            // 第一個 byte 是狀態位元組
            val statusByte = payload[0]
            val isUtf16 = (statusByte.toInt() and 0x80) != 0
            val languageCodeLength = (statusByte.toInt() and 0x3F)
            
            if (1 + languageCodeLength >= payload.size) {
                throw NdefParseException("文字 Record 格式錯誤")
            }
            
            // 跳過語言代碼
            val textStart = 1 + languageCodeLength
            val textBytes = payload.copyOfRange(textStart, payload.size)
            
            val charset = if (isUtf16) Charset.forName("UTF-16") else Charset.forName("UTF-8")
            String(textBytes, charset)
        } catch (e: NdefParseException) {
            Logger.w("解析文字 Record 失敗: ${e.message}", e)
            bytesToHexString(record.payload)
        } catch (e: Exception) {
            Logger.w("解析文字 Record 發生未知錯誤: ${e.message}", e)
            bytesToHexString(record.payload)
        }
    }
    
    /**
     * 解析 URI Record
     */
    private fun parseUriRecord(record: NdefRecord): String {
        return try {
            val payload = record.payload
            
            if (payload.isEmpty()) {
                Logger.w("URI Record payload 為空")
                return ""
            }
            
            // 第一個 byte 是 URI 前綴識別碼
            val uriPrefixCode = payload[0].toInt() and 0xFF
            val uriPrefix = UriPrefixConstants.getUriPrefix(uriPrefixCode)
            
            // 其餘為 URI 內容
            val uriBytes = payload.copyOfRange(1, payload.size)
            val uriSuffix = String(uriBytes, Charset.forName("UTF-8"))
            
            uriPrefix + uriSuffix
        } catch (e: Exception) {
            Logger.w("解析 URI Record 失敗: ${e.message}", e)
            bytesToHexString(record.payload)
        }
    }
    
    
    /**
     * 將 Byte Array 轉換為十六進位字串
     */
    private fun bytesToHexString(bytes: ByteArray): String {
        return bytes.joinToString(" ") { byte ->
            "%02X".format(byte)
        }
    }
}


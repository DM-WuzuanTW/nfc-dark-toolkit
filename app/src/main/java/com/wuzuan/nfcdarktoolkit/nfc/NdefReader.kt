package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import com.wuzuan.nfcdarktoolkit.domain.model.*
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
        val ndef = Ndef.get(tag) ?: return emptyList()
        
        return try {
            ndef.connect()
            val ndefMessage = ndef.cachedNdefMessage
            ndef.close()
            
            if (ndefMessage != null) {
                parseNdefMessage(ndefMessage)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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
            
            // 第一個 byte 是狀態位元組
            val statusByte = payload[0]
            val isUtf16 = (statusByte.toInt() and 0x80) != 0
            val languageCodeLength = (statusByte.toInt() and 0x3F)
            
            // 跳過語言代碼
            val textStart = 1 + languageCodeLength
            val textBytes = payload.copyOfRange(textStart, payload.size)
            
            val charset = if (isUtf16) Charset.forName("UTF-16") else Charset.forName("UTF-8")
            String(textBytes, charset)
        } catch (e: Exception) {
            e.printStackTrace()
            bytesToHexString(record.payload)
        }
    }
    
    /**
     * 解析 URI Record
     */
    private fun parseUriRecord(record: NdefRecord): String {
        return try {
            val payload = record.payload
            
            if (payload.isEmpty()) return ""
            
            // 第一個 byte 是 URI 前綴識別碼
            val uriPrefixCode = payload[0].toInt() and 0xFF
            val uriPrefix = getUriPrefix(uriPrefixCode)
            
            // 其餘為 URI 內容
            val uriBytes = payload.copyOfRange(1, payload.size)
            val uriSuffix = String(uriBytes, Charset.forName("UTF-8"))
            
            uriPrefix + uriSuffix
        } catch (e: Exception) {
            e.printStackTrace()
            bytesToHexString(record.payload)
        }
    }
    
    /**
     * 獲取 URI 前綴
     */
    private fun getUriPrefix(code: Int): String {
        return when (code) {
            0x00 -> ""
            0x01 -> "http://www."
            0x02 -> "https://www."
            0x03 -> "http://"
            0x04 -> "https://"
            0x05 -> "tel:"
            0x06 -> "mailto:"
            0x07 -> "ftp://anonymous:anonymous@"
            0x08 -> "ftp://ftp."
            0x09 -> "ftps://"
            0x0A -> "sftp://"
            0x0B -> "smb://"
            0x0C -> "nfs://"
            0x0D -> "ftp://"
            0x0E -> "dav://"
            0x0F -> "news:"
            0x10 -> "telnet://"
            0x11 -> "imap:"
            0x12 -> "rtsp://"
            0x13 -> "urn:"
            0x14 -> "pop:"
            0x15 -> "sip:"
            0x16 -> "sips:"
            0x17 -> "tftp:"
            0x18 -> "btspp://"
            0x19 -> "btl2cap://"
            0x1A -> "btgoep://"
            0x1B -> "tcpobex://"
            0x1C -> "irdaobex://"
            0x1D -> "file://"
            0x1E -> "urn:epc:id:"
            0x1F -> "urn:epc:tag:"
            0x20 -> "urn:epc:pat:"
            0x21 -> "urn:epc:raw:"
            0x22 -> "urn:epc:"
            0x23 -> "urn:nfc:"
            else -> ""
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


package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.wuzuan.nfcdarktoolkit.domain.model.NdefContent
import com.wuzuan.nfcdarktoolkit.domain.exception.*
import com.wuzuan.nfcdarktoolkit.nfc.UriPrefixConstants
import com.wuzuan.nfcdarktoolkit.utils.Logger
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NDEF 寫入器
 */
@Singleton
class NdefWriter @Inject constructor() {
    
    /**
     * 寫入文字到標籤
     */
    fun writeText(tag: Tag, text: String, languageCode: String = "en", sign: Boolean = false): Result<Unit> {
        val record = createTextRecord(text, languageCode)
        var message = NdefMessage(arrayOf(record))
        if (sign) message = addSignatureRecord(message)
        return writeNdefMessage(tag, message)
    }
    
    /**
     * 寫入 URI 到標籤
     */
    fun writeUri(tag: Tag, uri: String, sign: Boolean = false): Result<Unit> {
        val record = createUriRecord(uri)
        var message = NdefMessage(arrayOf(record))
        if (sign) message = addSignatureRecord(message)
        return writeNdefMessage(tag, message)
    }
    
    /**
     * 寫入 Wi-Fi 網路到標籤
     */
    fun writeWifi(tag: Tag, ssid: String, pass: String?, securityType: String = "WPA", sign: Boolean = false): Result<String> {
        val wifiString = "WIFI:S:$ssid;T:$securityType;P:$pass;;"
        val record = createTextRecord(wifiString)
        var message = NdefMessage(arrayOf(record))
        if (sign) message = addSignatureRecord(message)
        return writeNdefMessage(tag, message).map { wifiString }
    }
    
    /**
     * 寫入簡訊到標籤
     */
    fun writeSms(tag: Tag, phone: String, message: String, sign: Boolean = false): Result<String> {
        val smsUri = "sms:$phone?body=$message"
        val record = createUriRecord(smsUri)
        var ndefMessage = NdefMessage(arrayOf(record))
        if (sign) ndefMessage = addSignatureRecord(ndefMessage)
        return writeNdefMessage(tag, ndefMessage).map { smsUri }
    }

    /**
     * 寫入 vCard 到標籤
     */
    fun writeVCard(tag: Tag, name: String?, phone: String?, email: String?, sign: Boolean = false): Result<String> {
        val vcardString = buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            name?.let { appendLine("FN:$it") }
            phone?.let { appendLine("TEL:$it") }
            email?.let { appendLine("EMAIL:$it") }
            append("END:VCARD")
        }
        val record = NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            "text/vcard".toByteArray(Charset.forName("UTF-8")),
            ByteArray(0),
            vcardString.toByteArray(Charset.forName("UTF-8"))
        )
        val message = NdefMessage(arrayOf(record))
        var finalMessage = message
        if (sign) finalMessage = addSignatureRecord(message)
        return writeNdefMessage(tag, finalMessage).map { vcardString }
    }
    
    /**
     * 寫入自訂資料到標籤
     */
    fun writeCustom(tag: Tag, content: NdefContent): Result<Unit> {
        val record = when (content) {
            is NdefContent.Text -> createTextRecord(content.text, content.languageCode)
            is NdefContent.Uri -> createUriRecord(content.uri)
            is NdefContent.Json -> createJsonRecord(content.jsonString)
            is NdefContent.VCard -> createVCardRecord(content)
            is NdefContent.Raw -> createRawRecord(content.data)
            else -> return Result.failure(IllegalArgumentException("不支援的 NdefContent 類型"))
        }
        
        val message = NdefMessage(arrayOf(record))
        return writeNdefMessage(tag, message)
    }
    
    /**
     * 寫入 NDEF Message 到標籤
     */
    fun writeNdefMessage(tag: Tag, message: NdefMessage): Result<Unit> {
        return try {
            val ndef = Ndef.get(tag)
            
            if (ndef != null) {
                // 標籤已格式化為 NDEF
                writeToNdefTag(ndef, message)
            } else {
                // 標籤未格式化，嘗試格式化
                formatAndWriteTag(tag, message)
            }
        } catch (e: NfcException) {
            Logger.nfc("WriteMessage", "NFC 操作失敗: ${e.message}", e)
            Result.failure(e)
        } catch (e: IOException) {
            Logger.nfc("WriteMessage", "IO 錯誤: ${e.message}", e)
            Result.failure(TagConnectionException("標籤連接失敗", e))
        } catch (e: FormatException) {
            Logger.nfc("WriteMessage", "格式錯誤: ${e.message}", e)
            Result.failure(TagFormatException("標籤格式化失敗", e))
        } catch (e: Exception) {
            Logger.nfc("WriteMessage", "未知錯誤: ${e.message}", e)
            Result.failure(TagWriteException("寫入失敗", e))
        }
    }
    
    /**
     * 寫入到已格式化的 NDEF 標籤
     */
    private fun writeToNdefTag(ndef: Ndef, message: NdefMessage): Result<Unit> {
        return try {
            ndef.connect()
            try {
                if (!ndef.isWritable) {
                    throw TagNotWritableException()
                }
                
                // --- 核心修改：保留現有的驗證簽名 (Preserve Auth) ---
                // 1. 讀取當前內容
                val existingMsg = try { ndef.ndefMessage } catch (e: Exception) { null }
                
                // 2. 檢查當前內容是否有簽名
                val existingAuthRecord = existingMsg?.records?.find { 
                    it.toMimeType() == "application/vnd.wuzuan.auth" 
                }
                
                // 3. 檢查要寫入的新內容是否已經包含簽名 (如果是開發者模式寫入，這裡會已經有了)
                val newHasAuth = message.records.any { 
                    it.toMimeType() == "application/vnd.wuzuan.auth" 
                }
                
                // 4. 如果舊的有簽名，但新的沒簽名 -> 把舊的補上去
                var finalMessage = message
                if (existingAuthRecord != null && !newHasAuth) {
                    val newRecords = message.records + existingAuthRecord
                    finalMessage = NdefMessage(newRecords)
                    Logger.nfc("WriteToNdef", "檢測到舊的驗證簽名，已自動保留。")
                }
                // ------------------------------------------------
                
                val messageSize = finalMessage.toByteArray().size
                if (ndef.maxSize < messageSize) {
                    throw TagInsufficientSpaceException(messageSize, ndef.maxSize)
                }
                
                ndef.writeNdefMessage(finalMessage)
                Logger.nfc("WriteToNdef", "成功寫入 $messageSize bytes")
                Result.success(Unit)
            } finally {
                try {
                    ndef.close()
                } catch (e: Exception) {
                    Logger.w("關閉 NDEF 連接時發生錯誤: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * 格式化並寫入標籤
     */
    private fun formatAndWriteTag(tag: Tag, message: NdefMessage): Result<Unit> {
        return try {
            val ndefFormatable = NdefFormatable.get(tag)
                ?: throw TagFormatException("標籤不支援 NDEF 格式化")
            
            ndefFormatable.connect()
            try {
                ndefFormatable.format(message)
                Logger.nfc("FormatAndWrite", "成功格式化並寫入標籤")
                Result.success(Unit)
            } finally {
                try {
                    ndefFormatable.close()
                } catch (e: Exception) {
                    Logger.w("關閉 NdefFormatable 連接時發生錯誤: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * 建立文字 Record
     */
    private fun createTextRecord(text: String, languageCode: String = "en"): NdefRecord {
        val languageBytes = languageCode.toByteArray(Charset.forName("US-ASCII"))
        val textBytes = text.toByteArray(Charset.forName("UTF-8"))
        
        val payload = ByteArray(1 + languageBytes.size + textBytes.size)
        payload[0] = languageBytes.size.toByte()
        System.arraycopy(languageBytes, 0, payload, 1, languageBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + languageBytes.size, textBytes.size)
        
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }
    
    /**
     * 建立 URI Record
     */
    private fun createUriRecord(uri: String): NdefRecord {
        // 嘗試匹配 URI 前綴
        val (prefixCode, suffix) = matchUriPrefix(uri)
        
        val payload = ByteArray(1 + suffix.toByteArray().size)
        payload[0] = prefixCode.toByte()
        System.arraycopy(suffix.toByteArray(), 0, payload, 1, suffix.toByteArray().size)
        
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, ByteArray(0), payload)
    }
    
    /**
     * 建立 JSON Record
     */
    private fun createJsonRecord(json: String): NdefRecord {
        val jsonBytes = json.toByteArray(Charset.forName("UTF-8"))
        return NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            "application/json".toByteArray(),
            ByteArray(0),
            jsonBytes
        )
    }
    
    /**
     * 建立 vCard Record
     */
    private fun createVCardRecord(vcard: NdefContent.VCard): NdefRecord {
        val vcardString = buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            vcard.name?.let { appendLine("FN:$it") }
            vcard.phone?.let { appendLine("TEL:$it") }
            vcard.email?.let { appendLine("EMAIL:$it") }
            vcard.company?.let { appendLine("ORG:$it") }
            vcard.title?.let { appendLine("TITLE:$it") }
            vcard.address?.let { appendLine("ADR:$it") }
            vcard.website?.let { appendLine("URL:$it") }
            append("END:VCARD")
        }
        
        val vcardBytes = vcardString.toByteArray(Charset.forName("UTF-8"))
        return NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            "text/vcard".toByteArray(),
            ByteArray(0),
            vcardBytes
        )
    }
    
    /**
     * 建立原始資料 Record
     */
    private fun createRawRecord(data: ByteArray): NdefRecord {
        return NdefRecord(
            NdefRecord.TNF_UNKNOWN,
            ByteArray(0),
            ByteArray(0),
            data
        )
    }
    
    /**
     * 匹配 URI 前綴
     */
    private fun matchUriPrefix(uri: String): Pair<Int, String> {
        return UriPrefixConstants.matchUriPrefix(uri)
    }


    fun addSignatureRecord(originalMessage: NdefMessage): NdefMessage {
        // Simulating "Encrypted" data 
        // Real implementation would involve crypto libraries, but here we use a Base64 encoded hash-like string
        // "DIAMOND HOST VERIFIED" -> Base64 or obfuscated
        // Let's use a fixed complex hex string to look like a signature
        val encryptedContent = "4449414D4F4E442D484F53542D5345435552452D32303235"
        val authBytes = encryptedContent.toByteArray(Charset.forName("UTF-8"))
        
        val authRecord = NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            "application/vnd.wuzuan.auth".toByteArray(Charset.forName("US-ASCII")),
            ByteArray(0),
            authBytes
        )
        
        // 組合新的 Records
        val newRecords = originalMessage.records + authRecord
        return NdefMessage(newRecords)
    }
}

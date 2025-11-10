package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import com.wuzuan.nfcdarktoolkit.domain.model.NdefContent
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
    fun writeText(tag: Tag, text: String, languageCode: String = "en"): Result<Unit> {
        val record = createTextRecord(text, languageCode)
        val message = NdefMessage(arrayOf(record))
        return writeNdefMessage(tag, message)
    }
    
    /**
     * 寫入 URI 到標籤
     */
    fun writeUri(tag: Tag, uri: String): Result<Unit> {
        val record = createUriRecord(uri)
        val message = NdefMessage(arrayOf(record))
        return writeNdefMessage(tag, message)
    }
    
    /**
     * 寫入自訂資料到標籤
     */
    fun writeCustom(tag: Tag, content: NdefContent): Result<Unit> {
        val record = when (content) {
            is NdefContent.Text -> createTextRecord(content.text, content.languageCode)
            is NdefContent.Uri -> createUriRecord(content.uri)
            is NdefContent.Json -> createJsonRecord(content.jsonString)
            is NdefContent.WiFi -> createWiFiRecord(content)
            is NdefContent.VCard -> createVCardRecord(content)
            is NdefContent.Raw -> createRawRecord(content.data)
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
                ndef.connect()
                
                if (!ndef.isWritable) {
                    ndef.close()
                    return Result.failure(IOException("標籤不可寫入"))
                }
                
                if (ndef.maxSize < message.toByteArray().size) {
                    ndef.close()
                    return Result.failure(IOException("標籤容量不足"))
                }
                
                ndef.writeNdefMessage(message)
                ndef.close()
                Result.success(Unit)
            } else {
                // 標籤未格式化，嘗試格式化
                val ndefFormatable = NdefFormatable.get(tag)
                    ?: return Result.failure(IOException("標籤不支援 NDEF"))
                
                ndefFormatable.connect()
                ndefFormatable.format(message)
                ndefFormatable.close()
                Result.success(Unit)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: FormatException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
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
     * 建立 Wi-Fi Record (簡易版本)
     */
    private fun createWiFiRecord(wifi: NdefContent.WiFi): NdefRecord {
        // 這是簡化版本，實際 Wi-Fi NDEF 格式較複雜
        val wifiString = "WIFI:S:${wifi.ssid};T:${wifi.securityType.name};P:${wifi.password ?: ""};;"
        return createTextRecord(wifiString)
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
        val prefixes = mapOf(
            0x01 to "http://www.",
            0x02 to "https://www.",
            0x03 to "http://",
            0x04 to "https://",
            0x05 to "tel:",
            0x06 to "mailto:",
            0x07 to "ftp://anonymous:anonymous@",
            0x08 to "ftp://ftp.",
            0x09 to "ftps://",
            0x0A to "sftp://",
            0x0B to "smb://",
            0x0C to "nfs://",
            0x0D to "ftp://",
            0x0E to "dav://",
            0x0F to "news:",
            0x10 to "telnet://",
            0x11 to "imap:",
            0x12 to "rtsp://",
            0x13 to "urn:",
            0x14 to "pop:",
            0x15 to "sip:",
            0x16 to "sips:",
            0x17 to "tftp:",
            0x18 to "btspp://",
            0x19 to "btl2cap://",
            0x1A to "btgoep://",
            0x1B to "tcpobex://",
            0x1C to "irdaobex://",
            0x1D to "file://",
            0x1E to "urn:epc:id:",
            0x1F to "urn:epc:tag:",
            0x20 to "urn:epc:pat:",
            0x21 to "urn:epc:raw:",
            0x22 to "urn:epc:",
            0x23 to "urn:nfc:"
        )
        
        for ((code, prefix) in prefixes) {
            if (uri.startsWith(prefix, ignoreCase = true)) {
                return code to uri.substring(prefix.length)
            }
        }
        
        // 無匹配前綴
        return 0x00 to uri
    }
}


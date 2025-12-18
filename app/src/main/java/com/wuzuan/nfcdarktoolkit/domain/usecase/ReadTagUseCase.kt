package com.wuzuan.nfcdarktoolkit.domain.usecase

import android.nfc.Tag
import com.wuzuan.nfcdarktoolkit.domain.model.NdefRecordType
import com.wuzuan.nfcdarktoolkit.domain.model.ParsedNdefRecord
import com.wuzuan.nfcdarktoolkit.domain.model.RecordType
import com.wuzuan.nfcdarktoolkit.domain.model.TagInfo
import com.wuzuan.nfcdarktoolkit.domain.exception.NfcException
import com.wuzuan.nfcdarktoolkit.domain.exception.TagReadException
import com.wuzuan.nfcdarktoolkit.nfc.NdefReader
import com.wuzuan.nfcdarktoolkit.nfc.NfcManager
import com.wuzuan.nfcdarktoolkit.utils.Logger
import javax.inject.Inject

/**
 * 讀取標籤 UseCase
 */
class ReadTagUseCase @Inject constructor(
    private val nfcManager: NfcManager,
    private val ndefReader: NdefReader
) {
    
    operator fun invoke(tag: Tag): Result<TagInfo> {
        return try {
            Logger.nfc("ReadTag", "開始讀取標籤，ID: ${tag.id.joinToString(":") { "%02X".format(it) }}")
            Logger.nfc("ReadTag", "標籤技術: ${tag.techList.joinToString()}")
            
            // 解析標籤基本資訊（即使失敗也要繼續）
            val tagInfo = try {
                nfcManager.parseTagInfo(tag)
            } catch (e: Exception) {
                Logger.w("解析標籤基本資訊失敗，使用預設值: ${e.message}", e)
                // 建立基本的 TagInfo
                createBasicTagInfo(tag)
            }
            
            Logger.nfc("ReadTag", "標籤基本資訊: ID=${tagInfo.id}, Type=${tagInfo.type}")
            
            // 讀取 NDEF 資料（允許失敗）
            val ndefRecords = try {
                ndefReader.readNdefFromTag(tag)
            } catch (e: Exception) {
                Logger.w("讀取 NDEF 資料失敗: ${e.message}", e)
                emptyList()
            }
            
            Logger.nfc("ReadTag", "讀取到 ${ndefRecords.size} 筆 NDEF 記錄")

            // 將原始 NDEF 資料轉換為簡化過的 ParsedNdefRecord
            val parsedNdefRecords = ndefRecords.map { ndefRecord ->
                val recordType = when (ndefRecord.recordType) {
                    NdefRecordType.TEXT -> RecordType.TEXT
                    NdefRecordType.URI -> RecordType.URI
                    NdefRecordType.MIME -> RecordType.MIME
                    else -> RecordType.UNKNOWN
                }
                ParsedNdefRecord(
                    recordType = recordType,
                    payload = ndefRecord.payload,
                    mimeType = if (recordType == RecordType.MIME) ndefRecord.type else null
                )
            }
            
            // 合併完整資訊
            val completeTagInfo = tagInfo.copy(ndefRecords = parsedNdefRecords)
            
            Logger.nfc("ReadTag", "標籤讀取完成，記錄數: ${parsedNdefRecords.size}")
            Result.success(completeTagInfo)
        } catch (e: SecurityException) {
            Logger.e("權限不足: ${e.message}", e)
            Result.failure(TagReadException("無法讀取標籤，權限不足", e))
        } catch (e: Exception) {
            Logger.e("讀取標籤時發生未預期錯誤: ${e.message}", e)
            Result.failure(TagReadException("讀取標籤失敗: ${e.message}", e))
        }
    }
    
    /**
     * 建立基本的標籤資訊
     */
    private fun createBasicTagInfo(tag: Tag): TagInfo {
        return TagInfo(
            id = tag.id.joinToString(":") { "%02X".format(it) },
            type = com.wuzuan.nfcdarktoolkit.domain.model.TagType.fromTag(tag),
            techList = tag.techList.map { it.split('.').last() },
            maxSize = 0,
            isWritable = false,
            ndefRecords = emptyList()
        )
    }
    
    /**
     * 檢查標籤是否可讀取
     */
    fun isTagReadable(tag: Tag): Boolean {
        return try {
            val techList = tag.techList
            techList.any { tech ->
                tech.contains("Ndef") || tech.contains("NdefFormatable") ||
                tech.contains("MifareClassic") || tech.contains("MifareUltralight")
            }
        } catch (e: Exception) {
            Logger.w("檢查標籤可讀性失敗: ${e.message}", e)
            false
        }
    }
}

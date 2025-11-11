package com.wuzuan.nfcdarktoolkit.domain.usecase

import android.nfc.Tag
import com.wuzuan.nfcdarktoolkit.domain.model.TagInfo
import com.wuzuan.nfcdarktoolkit.nfc.NdefReader
import com.wuzuan.nfcdarktoolkit.nfc.NfcManager
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
            // 解析標籤基本資訊
            val tagInfo = nfcManager.parseTagInfo(tag)
            
            // 讀取 NDEF 資料
            val ndefRecords = ndefReader.readNdefFromTag(tag)
            
            // 合併完整資訊
            val completeTagInfo = tagInfo.copy(ndefRecords = ndefRecords)
            
            Result.success(completeTagInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}


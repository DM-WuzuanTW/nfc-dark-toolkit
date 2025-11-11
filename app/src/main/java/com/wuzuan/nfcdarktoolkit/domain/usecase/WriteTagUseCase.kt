package com.wuzuan.nfcdarktoolkit.domain.usecase

import android.nfc.Tag
import com.wuzuan.nfcdarktoolkit.domain.model.NdefContent
import com.wuzuan.nfcdarktoolkit.nfc.NdefWriter
import javax.inject.Inject

/**
 * 寫入標籤 UseCase
 */
class WriteTagUseCase @Inject constructor(
    private val ndefWriter: NdefWriter
) {
    
    /**
     * 寫入文字
     */
    fun writeText(tag: Tag, text: String, languageCode: String = "zh"): Result<Unit> {
        return ndefWriter.writeText(tag, text, languageCode)
    }
    
    /**
     * 寫入 URL
     */
    fun writeUrl(tag: Tag, url: String): Result<Unit> {
        return ndefWriter.writeUri(tag, url)
    }
    
    /**
     * 寫入自訂內容
     */
    fun writeCustom(tag: Tag, content: NdefContent): Result<Unit> {
        return ndefWriter.writeCustom(tag, content)
    }
}


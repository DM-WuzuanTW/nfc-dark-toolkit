package com.wuzuan.nfcdarktoolkit.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import com.wuzuan.nfcdarktoolkit.MainActivity
import com.wuzuan.nfcdarktoolkit.domain.model.TagInfo
import com.wuzuan.nfcdarktoolkit.domain.exception.NfcNotEnabledException
import com.wuzuan.nfcdarktoolkit.domain.exception.NfcNotSupportedException
import com.wuzuan.nfcdarktoolkit.domain.exception.TagReadException
import com.wuzuan.nfcdarktoolkit.utils.Logger
import com.wuzuan.nfcdarktoolkit.domain.model.TagType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcManager @Inject constructor(
    private val nfcAdapter: NfcAdapter?
) {

    fun isNfcSupported(): Boolean = nfcAdapter != null

    fun isNfcEnabled(): Boolean = nfcAdapter?.isEnabled == true

    fun enableForegroundDispatch(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun getTagFromIntent(intent: Intent): Tag? {
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
    }

    fun parseTagInfo(tag: Tag): TagInfo {
        return try {
            val tagId = tag.id.joinToString(":") { "%02X".format(it) }
            val techList = tag.techList.map { it.split('.').last() }
            val type = TagType.fromTag(tag)
            
            // 安全地獲取 NDEF 資訊
            var isWritable = false
            var maxSize = 0
            
            try {
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    // 不連接，只獲取基本資訊
                    isWritable = ndef.isWritable
                    maxSize = ndef.maxSize
                }
            } catch (e: Exception) {
                Logger.w("獲取 NDEF 資訊失敗，使用預設值: ${e.message}", e)
            }
            
            Logger.nfc("NfcManager", "解析標籤資訊: ID=$tagId, Type=$type, 技術=${techList.joinToString()}")

            TagInfo(
                id = tagId,
                type = type,
                techList = techList,
                maxSize = maxSize,
                isWritable = isWritable,
                ndefRecords = emptyList() // 記錄將在後續步驟中解析
            )
        } catch (e: Exception) {
            Logger.e("解析標籤資訊失敗: ${e.message}", e)
            throw TagReadException("無法解析標籤資訊", e)
        }
    }
    
    /**
     * 檢查並確保 NFC 可用
     */
    fun ensureNfcAvailable() {
        if (!isNfcSupported()) {
            throw NfcNotSupportedException()
        }
        if (!isNfcEnabled()) {
            throw NfcNotEnabledException()
        }
    }
    
    /**
     * 安全地啟用前景調度
     */
    fun safeEnableForegroundDispatch(activity: Activity): Boolean {
        return try {
            ensureNfcAvailable()
            enableForegroundDispatch(activity)
            true
        } catch (e: Exception) {
            Logger.w("啟用 NFC 前景調度失敗: ${e.message}", e)
            false
        }
    }
    
    /**
     * 安全地禁用前景調度
     */
    fun safeDisableForegroundDispatch(activity: Activity): Boolean {
        return try {
            disableForegroundDispatch(activity)
            true
        } catch (e: Exception) {
            Logger.w("禁用 NFC 前景調度失敗: ${e.message}", e)
            false
        }
    }
}

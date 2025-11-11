package com.wuzuan.nfcdarktoolkit.utils

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import com.wuzuan.nfcdarktoolkit.domain.model.TagType

/**
 * 標籤資訊輔助工具（參考 NFC Tools）
 */
object TagInfoHelper {
    
    /**
     * 獲取標籤製造商
     */
    fun getManufacturer(tag: Tag): String {
        return try {
            val id = tag.id
            if (id.isEmpty()) return "未知"
            
            when (id[0].toInt() and 0xFF) {
                0x04 -> "NXP Semiconductors"
                0x02 -> "STMicroelectronics"
                0x07 -> "Texas Instruments"
                0x05 -> "Infineon Technologies"
                else -> "未知製造商"
            }
        } catch (e: Exception) {
            "未知"
        }
    }
    
    /**
     * 獲取詳細的標籤型號
     */
    fun getDetailedTagModel(tag: Tag, tagType: TagType): String {
        return when (tagType) {
            TagType.MIFARE_ULTRALIGHT -> {
                try {
                    val ultralight = MifareUltralight.get(tag)
                    ultralight?.connect()
                    val type = ultralight?.type ?: MifareUltralight.TYPE_UNKNOWN
                    ultralight?.close()
                    
                    when (type) {
                        MifareUltralight.TYPE_ULTRALIGHT -> "Mifare Ultralight"
                        MifareUltralight.TYPE_ULTRALIGHT_C -> "Mifare Ultralight C"
                        else -> detectNTAGType(tag)
                    }
                } catch (e: Exception) {
                    "Mifare Ultralight"
                }
            }
            TagType.NTAG -> detectNTAGType(tag)
            TagType.MIFARE_CLASSIC -> "Mifare Classic"
            TagType.NFC_A -> "NFC-A (ISO 14443-3A)"
            TagType.NFC_B -> "NFC-B (ISO 14443-3B)"
            TagType.NFC_F -> "NFC-F (FeliCa)"
            TagType.NFC_V -> "NFC-V (ISO 15693)"
            else -> tagType.name
        }
    }
    
    /**
     * 偵測 NTAG 具體型號
     */
    private fun detectNTAGType(tag: Tag): String {
        return try {
            val ultralight = MifareUltralight.get(tag) ?: return "NTAG"
            ultralight.connect()
            
            // 讀取版本資訊
            val nfcA = NfcA.get(tag)
            nfcA?.connect()
            
            val versionCommand = byteArrayOf(0x60.toByte())
            val version = nfcA?.transceive(versionCommand)
            nfcA?.close()
            ultralight.close()
            
            // 根據版本資訊判斷型號
            if (version != null && version.size >= 8) {
                when (version[6].toInt()) {
                    0x0F -> "NTAG 213"
                    0x11 -> "NTAG 215"
                    0x13 -> "NTAG 216"
                    else -> "NTAG"
                }
            } else {
                "NTAG"
            }
        } catch (e: Exception) {
            "NTAG"
        }
    }
    
    /**
     * 獲取標籤容量描述
     */
    fun getCapacityDescription(maxSize: Int?): String {
        return when (maxSize) {
            null -> "未知"
            in 0..143 -> "$maxSize bytes (NTAG 213)"
            in 144..503 -> "$maxSize bytes (NTAG 215)"
            in 504..887 -> "$maxSize bytes (NTAG 216)"
            in 888..3999 -> "${maxSize / 1024}KB"
            else -> "${maxSize / 1024}KB"
        }
    }
    
    /**
     * 格式化標籤 ID 顯示
     */
    fun formatTagId(id: ByteArray): String {
        return id.joinToString(":") { byte ->
            "%02X".format(byte)
        }
    }
    
    /**
     * 獲取標籤特性描述
     */
    fun getTagFeatures(tag: Tag): List<String> {
        val features = mutableListOf<String>()
        
        tag.techList.forEach { tech ->
            when {
                tech.contains("MifareUltralight") -> {
                    features.add("✓ 支援快速讀寫")
                    features.add("✓ 低成本標籤")
                }
                tech.contains("MifareClassic") -> {
                    features.add("✓ 支援加密")
                    features.add("✓ 分區存儲")
                }
                tech.contains("IsoDep") -> {
                    features.add("✓ 支援 APDU 指令")
                    features.add("✓ 高安全性")
                }
                tech.contains("Ndef") -> {
                    features.add("✓ 支援 NDEF 格式")
                    features.add("✓ 跨平台兼容")
                }
            }
        }
        
        return features.distinct()
    }
    
    /**
     * 獲取標籤建議用途
     */
    fun getSuggestedUsage(tagType: TagType, maxSize: Int?): String {
        return when {
            maxSize == null -> "未知用途"
            maxSize <= 143 -> "適合：名片、URL、簡單文字"
            maxSize <= 504 -> "適合：聯絡資訊、WiFi、詳細文字"
            maxSize <= 888 -> "適合：完整資訊、多筆記錄、小型檔案"
            else -> "適合：複雜應用、多功能標籤"
        }
    }
}


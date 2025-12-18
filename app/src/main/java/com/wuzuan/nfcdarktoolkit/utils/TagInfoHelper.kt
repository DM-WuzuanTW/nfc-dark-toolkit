package com.wuzuan.nfcdarktoolkit.utils

import android.nfc.Tag
import android.nfc.tech.*
import com.wuzuan.nfcdarktoolkit.domain.model.TagType

object TagInfoHelper {

    fun getDetailedTagModel(tag: Tag, type: TagType): String {
        return when (type) {
            TagType.MIFARE_CLASSIC -> getMifareClassicModel(tag)
            TagType.MIFARE_ULTRALIGHT -> getMifareUltralightModel(tag)
            else -> type.name
        }
    }

    fun getManufacturer(tag: Tag): String {
        // 嘗試從 Tag ID 或 Tech List 推測製造商
        // 簡單規則：
        // NXP: Mifare 系列, NTAG 系列
        // Sony: Felica
        // STMicroelectronics: ST25 系列 (通常支援 NfcV)
        
        val techList = tag.techList.map { it.split('.').last() }
        
        return when {
            techList.any { it.contains("Mifare") } -> "NXP Semiconductors"
            techList.contains("NfcA") -> {
                // 檢查 NXP 常見 ID 前綴 (這只是簡單推測)
                // 04開頭通常是 NXP (Mifare/NTAG)
                val id = tag.id
                if (id.isNotEmpty() && id[0] == 0x04.toByte()) "NXP Semiconductors" else "Unknown Manufacturer"
            }
            techList.contains("NfcF") -> "Sony (FeliCa)"
            techList.contains("NfcV") -> "STMicroelectronics / NXP / TI"
            techList.contains("NfcB") -> "STMicroelectronics / NXP"
            else -> "Unknown Manufacturer"
        }
    }

    private fun getMifareClassicModel(tag: Tag): String {
        val mifare = MifareClassic.get(tag) ?: return "Mifare Classic"
        return when (mifare.type) {
            MifareClassic.TYPE_CLASSIC -> "Mifare Classic"
            MifareClassic.TYPE_PLUS -> "Mifare Plus"
            MifareClassic.TYPE_PRO -> "Mifare Pro"
            else -> "Mifare Classic"
        }
    }

    private fun getMifareUltralightModel(tag: Tag): String {
        val ultralight = MifareUltralight.get(tag) ?: return "Mifare Ultralight"
        return when (ultralight.type) {
            MifareUltralight.TYPE_ULTRALIGHT -> "Mifare Ultralight"
            MifareUltralight.TYPE_ULTRALIGHT_C -> "Mifare Ultralight C"
            else -> "Mifare Ultralight"
        }
    }

    fun getCapacityDescription(size: Int): String {
        return when {
            size < 1024 -> "$size bytes"
            else -> {
                val kb = size / 1024
                "$kb KB"
            }
        }
    }
}

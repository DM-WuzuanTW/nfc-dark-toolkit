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

    private fun getMifareClassicModel(tag: Tag): String {
        val mifare = MifareClassic.get(tag)
        return when (mifare.type) {
            MifareClassic.TYPE_CLASSIC -> "Mifare Classic"
            MifareClassic.TYPE_PLUS -> "Mifare Plus"
            MifareClassic.TYPE_PRO -> "Mifare Pro"
            else -> "Mifare Classic"
        }
    }

    private fun getMifareUltralightModel(tag: Tag): String {
        val ultralight = MifareUltralight.get(tag)
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

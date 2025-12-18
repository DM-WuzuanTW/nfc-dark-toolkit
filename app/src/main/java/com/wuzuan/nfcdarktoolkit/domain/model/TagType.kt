package com.wuzuan.nfcdarktoolkit.domain.model

import android.nfc.Tag

/**
 * NFC 標籤類型枚舉
 */
enum class TagType {
    NFC_A,
    NFC_B,
    NFC_F,
    NFC_V,
    ISO_DEP,
    NDEF,
    NDEF_FORMATABLE,
    MIFARE_CLASSIC,
    MIFARE_ULTRALIGHT,
    MIFARE_DESFIRE,
    UNKNOWN;

    companion object {
        /**
         * 從 Tag 物件判斷標籤類型
         */
        fun fromTag(tag: Tag): TagType {
            val techList = tag.techList.map { it.split('.').last() }

            return when {
                techList.contains("MifareClassic") -> MIFARE_CLASSIC
                techList.contains("MifareUltralight") -> MIFARE_ULTRALIGHT
                techList.contains("NfcA") -> NFC_A
                techList.contains("NfcB") -> NFC_B
                techList.contains("NfcF") -> NFC_F
                techList.contains("NfcV") -> NFC_V
                techList.contains("IsoDep") -> ISO_DEP
                techList.contains("Ndef") -> NDEF
                techList.contains("NdefFormatable") -> NDEF_FORMATABLE
                else -> UNKNOWN
            }
        }
    }
}
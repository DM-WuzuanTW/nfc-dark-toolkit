package com.wuzuan.nfcdarktoolkit.domain.model

/**
 * NFC 標籤基本資訊
 */
data class TagInfo(
    val id: String,
    val techList: List<String>,
    val type: TagType,
    val isWritable: Boolean,
    val maxSize: Int?,
    val currentSize: Int?,
    val ndefRecords: List<NdefRecordData> = emptyList()
)

enum class TagType {
    UNKNOWN,
    MIFARE_CLASSIC,
    MIFARE_ULTRALIGHT,
    NTAG,
    ISO_14443_3A,
    ISO_14443_3B,
    ISO_14443_4,
    ISO_15693,
    NFC_A,
    NFC_B,
    NFC_F,
    NFC_V,
    NDEF
}


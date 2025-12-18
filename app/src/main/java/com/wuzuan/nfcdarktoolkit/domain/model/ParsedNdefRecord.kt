package com.wuzuan.nfcdarktoolkit.domain.model

data class ParsedNdefRecord(
    val recordType: RecordType,
    val payload: String,
    val mimeType: String? = null
)

enum class RecordType {
    TEXT,
    URI,
    MIME,
    UNKNOWN
}

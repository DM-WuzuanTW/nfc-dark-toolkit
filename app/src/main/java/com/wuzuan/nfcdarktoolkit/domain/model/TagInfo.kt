package com.wuzuan.nfcdarktoolkit.domain.model


data class TagInfo(
    val id: String,
    val type: TagType,
    val techList: List<String>,
    val maxSize: Int,
    val isWritable: Boolean,
    val ndefRecords: List<ParsedNdefRecord>
)

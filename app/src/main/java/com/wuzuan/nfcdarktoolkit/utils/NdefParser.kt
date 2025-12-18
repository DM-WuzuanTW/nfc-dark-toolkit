package com.wuzuan.nfcdarktoolkit.utils

import com.wuzuan.nfcdarktoolkit.domain.model.ParsedNdefRecord
import com.wuzuan.nfcdarktoolkit.domain.model.RecordType

object NdefParser {

    fun parse(records: List<ParsedNdefRecord>): String {
        if (records.isEmpty()) {
            return "數位名片為空或不包含可識別的資料"
        }

        return records.joinToString("\n\n") { record ->
            when (record.recordType) {
                RecordType.TEXT -> "文字:\n${record.payload}"
                RecordType.URI -> "網址:\n${record.payload}"
                RecordType.MIME -> parseMimeRecord(record)
                else -> "未知類型:\n${record.payload}"
            }
        }
    }

    private fun parseMimeRecord(record: ParsedNdefRecord): String {
        return when (record.mimeType) {
            "text/vcard" -> parseVCard(record.payload)
            "application/json" -> "JSON:\n${record.payload}"
            else -> "MIME 類型: ${record.mimeType}\n內容: ${record.payload}"
        }
    }

    private fun parseVCard(payload: String): String {
        val name = payload.lines().find { it.startsWith("FN:") }?.substring(3)
        val phone = payload.lines().find { it.startsWith("TEL:") }?.substring(4)
        val email = payload.lines().find { it.startsWith("EMAIL:") }?.substring(6)

        return buildString {
            appendLine("聯絡人:")
            name?.let { appendLine("  姓名: $it") }
            phone?.let { appendLine("  電話: $it") }
            email?.let { appendLine("  Email: $it") }
        }.trim()
    }
}

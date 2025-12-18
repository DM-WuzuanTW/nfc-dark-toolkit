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
                RecordType.URI -> parseUriRecord(record.payload)
                RecordType.MIME -> parseMimeRecord(record)
                else -> "未知類型:\n${record.payload}"
            }
        }
    }

    private fun parseUriRecord(uri: String): String {
        return when {
            uri.contains("instagram.com") -> "社交網路 (Instagram):\n$uri"
            uri.contains("facebook.com") -> "社交網路 (Facebook):\n$uri"
            uri.contains("twitter.com") || uri.contains("x.com") -> "社交網路 (Twitter / X):\n$uri"
            uri.contains("youtube.com") || uri.contains("youtu.be") -> "媒體 (YouTube):\n$uri"
            uri.contains("tiktok.com") -> "社交網路 (TikTok):\n$uri"
            uri.contains("discord.com") || uri.contains("discord.gg") -> "社交網路 (Discord):\n$uri"
            uri.contains("line.me") -> "社交網路 (Line):\n$uri"
            uri.contains("t.me") -> "社交網路 (Telegram):\n$uri"
            uri.startsWith("mailto:") -> "通訊 (Email):\n${uri.removePrefix("mailto:")}"
            uri.startsWith("tel:") -> "通訊 (電話):\n${uri.removePrefix("tel:")}"
            uri.startsWith("sms:") -> "通訊 (簡訊):\n${uri.removePrefix("sms:")}"
            uri.startsWith("geo:") -> "位置 (地理位置):\n${uri.removePrefix("geo:")}"
            uri.startsWith("bitcoin:") -> "其他 (虛擬貨幣錢包):\n${uri.removePrefix("bitcoin:")}"
            else -> "基本 (網址):\n$uri"
        }
    }

    private fun parseMimeRecord(record: ParsedNdefRecord): String {
        return when (record.mimeType) {
            "text/vcard" -> parseVCard(record.payload)
            "application/x-wifi-config" -> "其他 (Wi-Fi 網路):\n(Wi-Fi 設定檔)"
            "application/vnd.bluetooth.ep.oob" -> "其他 (藍芽):\n(藍芽配對資訊)"
            "application/json" -> "檔案 (JSON):\n${record.payload}"
            else -> "MIME 類型: ${record.mimeType}\n內容: ${record.payload}"
        }
    }

    private fun parseVCard(payload: String): String {
        val name = payload.lines().find { it.startsWith("FN:") }?.substring(3) ?: "未知"
        val phone = payload.lines().find { it.startsWith("TEL:") }?.substring(4) ?: "未知"
        val email = payload.lines().find { it.startsWith("EMAIL:") }?.substring(6) ?: "未知"

        return buildString {
            appendLine("通訊 (聯絡人):")
            appendLine("  姓名: $name")
            appendLine("  電話: $phone")
            appendLine("  Email: $email")
        }.trim()
    }
}

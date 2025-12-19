package com.wuzuan.nfcdarktoolkit.utils

import com.wuzuan.nfcdarktoolkit.domain.model.ParsedNdefRecord
import com.wuzuan.nfcdarktoolkit.domain.model.RecordType
import com.wuzuan.nfcdarktoolkit.utils.Logger

object NdefParser {

    fun parse(records: List<ParsedNdefRecord>): String {
        // 過濾掉隱藏的驗證 Record
        val visibleRecords = records.filterNot { 
            it.mimeType == "application/vnd.wuzuan.auth" 
        }

        if (visibleRecords.isEmpty()) {
            return "數位名片為空或不包含可識別的資料"
        }

        return visibleRecords.joinToString("\n\n") { record ->
            when (record.recordType) {
                RecordType.TEXT -> {
                    // 嘗試解析 Wi-Fi 格式 (有時 Wi-Fi 會被存為 Text)
                    if (record.payload.startsWith("WIFI:")) {
                        parseWifi(record.payload)
                    } else {
                        "基本 (文字):\n${record.payload}"
                    }
                }
                RecordType.URI -> parseUriRecord(record.payload)
                RecordType.MIME -> parseMimeRecord(record)
                else -> "未知類型:\n${record.payload}"
            }
        }
    }

    private fun parseUriRecord(uri: String): String {
        val decodedUri = try {
            java.net.URLDecoder.decode(uri, "UTF-8")
        } catch (e: Exception) {
            uri
        }

        return when {
            // 社交網路
            uri.contains("instagram.com/") -> {
                val user = uri.substringAfter("instagram.com/").substringBefore("?")
                "社交網路 (Instagram):\n$user"
            }
            uri.contains("facebook.com/") -> {
                val user = uri.substringAfter("facebook.com/").substringBefore("?")
                "社交網路 (FaceBook):\n$user"
            }
            uri.contains("twitter.com/") || uri.contains("x.com/") -> {
                val user = uri.substringAfter(".com/").substringBefore("?")
                "社交網路 (Twitter / X):\n$user"
            }
            uri.contains("youtube.com/@") -> {
                val user = uri.substringAfter("@").substringBefore("?")
                "社交網路 (Youtube):\n$user"
            }
            uri.contains("tiktok.com/@") -> {
                val user = uri.substringAfter("@").substringBefore("?")
                "社交網路 (Tiktok):\n$user"
            }
            uri.contains("discord.com/users/") -> {
                val id = uri.substringAfter("users/")
                "社交網路 (Discord):\n$id"
            }
            uri.contains("line.me/ti/p/~") -> {
                val id = uri.substringAfter("~")
                "社交網路 (Line):\n$id"
            }
            uri.contains("t.me/") -> {
                val user = uri.substringAfter("t.me/")
                "社交網路 (Telegram):\n$user"
            }

            // 媒體
            uri.startsWith("market://details?id=") -> {
                val pkg = uri.substringAfter("id=")
                "媒體 (應用程式):\n$pkg"
            }
            uri.contains("play.google.com/store/apps/details?id=") -> {
                val pkg = uri.substringAfter("id=")
                "媒體 (應用程式):\n$pkg"
            }
            // 影片與檔案通常是直接網址，歸類為基本或媒體視情況而定，這裡按網址處理
            
            // 通訊
            uri.startsWith("mailto:") -> "通訊 (Email):\n${decodedUri.removePrefix("mailto:")}"
            uri.startsWith("tel:") -> "通訊 (電話):\n${decodedUri.removePrefix("tel:")}"
            uri.startsWith("sms:") -> {
                val part = decodedUri.removePrefix("sms:")
                val number = part.substringBefore("?")
                val body = if (part.contains("body=")) part.substringAfter("body=") else ""
                "通訊 (簡訊):\n號碼: $number" + if (body.isNotEmpty()) "\n內容: $body" else ""
            }

            // 位置
            uri.startsWith("geo:0,0?q=") -> {
                val query = decodedUri.substringAfter("q=")
                "位置 (地址):\n$query"
            }
            uri.startsWith("geo:") -> {
                val coords = decodedUri.removePrefix("geo:")
                "位置 (地理位置):\n$coords"
            }

            // 基本 (搜尋)
            uri.contains("google.com/search?q=") -> {
                val query = decodedUri.substringAfter("q=")
                "基本 (搜尋):\n$query"
            }

            // 其他
            uri.startsWith("bitcoin:") -> "其他 (虛擬貨幣錢包):\n${uri.removePrefix("bitcoin:")}"

            // 預設
            else -> "基本 (網址):\n$decodedUri"
        }
    }

    private fun parseMimeRecord(record: ParsedNdefRecord): String {
        return when (record.mimeType) {
            "text/vcard" -> parseVCard(record.payload)
            "application/x-wifi-config" -> "其他 (Wi-Fi網路):\n(Wi-Fi 設定檔)"
            "application/vnd.bluetooth.ep.oob" -> "其他 (藍芽):\n(藍芽配對資訊)"
            else -> "未知 MIME 類型 (${record.mimeType}):\n${record.payload}"
        }
    }

    private fun parseVCard(payload: String): String {
        // 簡單解析 vCard，解決可能的亂碼問題 (vCard 3.0 通常 UTF-8，但有時會有 QP 編碼，這裡做簡易處理)
        return try {
            val lines = payload.lines().map { it.trim() }.filter { it.isNotEmpty() }
            
            val name = lines.find { it.startsWith("FN:") }
                ?.substringAfter("FN:", "")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "未知"
                
            val phone = lines.find { it.startsWith("TEL:") }
                ?.substringAfter("TEL:", "")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "未提供"
                
            val email = lines.find { it.startsWith("EMAIL:") }
                ?.substringAfter("EMAIL:", "")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "未提供"
            
            buildString {
                appendLine("通訊 (聯絡人):")
                appendLine("  姓名: $name")
                appendLine("  電話: $phone")
                appendLine("  Email: $email")
            }.trim()
        } catch (e: Exception) {
            Logger.w("解析 vCard 失敗: ${e.message}", e)
            "通訊 (聯絡人):\n(解析失敗)"
        }
    }
    
    private fun parseWifi(payload: String): String {
        // WIFI:S:SSID;T:WPA;P:PASSWORD;;
        val ssid = payload.substringAfter("S:").substringBefore(";")
        val type = payload.substringAfter("T:").substringBefore(";")
        val password = payload.substringAfter("P:").substringBefore(";")
        
        return buildString {
            appendLine("其他 (Wi-Fi網路):")
            appendLine("  SSID: $ssid")
            appendLine("  密碼: $password")
            appendLine("  類型: $type")
        }.trim()
    }
}

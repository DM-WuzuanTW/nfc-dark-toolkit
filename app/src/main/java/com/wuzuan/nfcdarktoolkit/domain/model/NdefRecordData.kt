package com.wuzuan.nfcdarktoolkit.domain.model

/**
 * NDEF 記錄資料
 */
data class NdefRecordData(
    val tnf: Short,
    val type: String,
    val id: String?,
    val payload: String,
    val recordType: NdefRecordType
)

enum class NdefRecordType {
    TEXT,
    URI,
    MIME,
    EXTERNAL,
    SMART_POSTER,
    ABSOLUTE_URI,
    UNKNOWN,
    EMPTY
}

/**
 * 解析後的 NDEF 資料內容
 */
sealed class NdefContent {
    data class Text(
        val text: String,
        val languageCode: String = "en",
        val encoding: String = "UTF-8"
    ) : NdefContent()

    data class Uri(
        val uri: String
    ) : NdefContent()

    data class VCard(
        val name: String?,
        val phone: String?,
        val email: String?,
        val company: String?,
        val title: String?,
        val address: String?,
        val website: String?
    ) : NdefContent()

    data class WiFi(
        val ssid: String,
        val password: String?,
        val securityType: WiFiSecurityType
    ) : NdefContent()

    data class Json(
        val jsonString: String
    ) : NdefContent()

    data class Raw(
        val data: ByteArray
    ) : NdefContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Raw
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
}

enum class WiFiSecurityType {
    NONE,
    WEP,
    WPA,
    WPA2
}


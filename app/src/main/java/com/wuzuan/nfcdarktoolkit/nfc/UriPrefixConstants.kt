package com.wuzuan.nfcdarktoolkit.nfc

/**
 * URI 前綴常數
 * 統一管理 NDEF URI Record 的前綴定義，避免重複代碼
 */
object UriPrefixConstants {
    
    /**
     * URI 前綴映射表
     */
    val URI_PREFIXES = mapOf(
        0x00 to "",
        0x01 to "http://www.",
        0x02 to "https://www.",
        0x03 to "http://",
        0x04 to "https://",
        0x05 to "tel:",
        0x06 to "mailto:",
        0x07 to "ftp://anonymous:anonymous@",
        0x08 to "ftp://ftp.",
        0x09 to "ftps://",
        0x0A to "sftp://",
        0x0B to "smb://",
        0x0C to "nfs://",
        0x0D to "ftp://",
        0x0E to "dav://",
        0x0F to "news:",
        0x10 to "telnet://",
        0x11 to "imap:",
        0x12 to "rtsp://",
        0x13 to "urn:",
        0x14 to "pop:",
        0x15 to "sip:",
        0x16 to "sips:",
        0x17 to "tftp:",
        0x18 to "btspp://",
        0x19 to "btl2cap://",
        0x1A to "btgoep://",
        0x1B to "tcpobex://",
        0x1C to "irdaobex://",
        0x1D to "file://",
        0x1E to "urn:epc:id:",
        0x1F to "urn:epc:tag:",
        0x20 to "urn:epc:pat:",
        0x21 to "urn:epc:raw:",
        0x22 to "urn:epc:",
        0x23 to "urn:nfc:"
    )
    
    /**
     * 根據代碼獲取 URI 前綴
     */
    fun getUriPrefix(code: Int): String {
        return URI_PREFIXES[code] ?: ""
    }
    
    /**
     * 匹配 URI 前綴，返回代碼和剩餘部分
     */
    fun matchUriPrefix(uri: String): Pair<Int, String> {
        for ((code, prefix) in URI_PREFIXES) {
            if (prefix.isNotEmpty() && uri.startsWith(prefix, ignoreCase = true)) {
                return code to uri.substring(prefix.length)
            }
        }
        // 無匹配前綴
        return 0x00 to uri
    }
}

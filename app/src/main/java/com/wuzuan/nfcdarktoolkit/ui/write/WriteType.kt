package com.wuzuan.nfcdarktoolkit.ui.write

/**
 * 寫入資料類型（專業版，無 emoji）
 */
enum class WriteType(val displayName: String, val category: String) {
    // 基本類型
    TEXT("文字", "基本"),
    URL("網址", "基本"),
    SEARCH("搜尋", "基本"),
    
    // 社交網路
    SOCIAL_DISCORD("Discord", "社交網路"),
    SOCIAL_INSTAGRAM("Instagram", "社交網路"),
    SOCIAL_FACEBOOK("Facebook", "社交網路"),
    SOCIAL_LINE("Line", "社交網路"),
    SOCIAL_TELEGRAM("Telegram", "社交網路"),
    SOCIAL_TWITTER("Twitter / X", "社交網路"),
    SOCIAL_YOUTUBE("YouTube", "社交網路"),
    SOCIAL_TIKTOK("TikTok", "社交網路"),
    
    // 媒體
    VIDEO("影片", "媒體"),
    FILE("檔案", "媒體"),
    APPLICATION("應用程式", "媒體"),
    
    // 通訊
    MAIL("Email", "通訊"),
    CONTACT("聯絡人", "通訊"),
    PHONE("電話號碼", "通訊"),
    SMS("簡訊", "通訊"),
    
    // 位置
    LOCATION("地理位置", "位置"),
    ADDRESS("地址", "位置"),
    
    // 其他
    BITCOIN("比特幣錢包", "其他"),
    BLUETOOTH("藍牙", "其他"),
    WIFI("Wi-Fi 網路", "其他");
    
    companion object {
        fun getCategories(): Map<String, List<WriteType>> {
            return values().groupBy { it.category }
        }
        
        fun getCategoryList(): List<String> {
            return values().map { it.category }.distinct()
        }
    }
}

/**
 * 社交網路 URL 產生器
 */
object SocialUrlBuilder {
    
    fun buildUrl(type: WriteType, username: String): String {
        return when (type) {
            WriteType.SOCIAL_DISCORD -> "https://discord.com/users/$username"
            WriteType.SOCIAL_INSTAGRAM -> "https://www.instagram.com/$username"
            WriteType.SOCIAL_FACEBOOK -> "https://www.facebook.com/$username"
            WriteType.SOCIAL_LINE -> "https://line.me/ti/p/~$username"
            WriteType.SOCIAL_TELEGRAM -> "https://t.me/$username"
            WriteType.SOCIAL_TWITTER -> "https://twitter.com/$username"
            WriteType.SOCIAL_YOUTUBE -> "https://www.youtube.com/@$username"
            WriteType.SOCIAL_TIKTOK -> "https://www.tiktok.com/@$username"
            else -> username
        }
    }
    
    fun getPlaceholder(type: WriteType): String {
        return when (type) {
            WriteType.TEXT -> "輸入文字內容"
            WriteType.URL -> "輸入完整網址（https://...）"
            WriteType.SEARCH -> "輸入搜尋關鍵字"
            
            WriteType.SOCIAL_DISCORD -> "輸入 Discord 用戶 ID"
            WriteType.SOCIAL_INSTAGRAM -> "輸入 Instagram 用戶名稱"
            WriteType.SOCIAL_FACEBOOK -> "輸入 Facebook 用戶名稱"
            WriteType.SOCIAL_LINE -> "輸入 LINE ID"
            WriteType.SOCIAL_TELEGRAM -> "輸入 Telegram 用戶名稱"
            WriteType.SOCIAL_TWITTER -> "輸入 Twitter 用戶名稱"
            WriteType.SOCIAL_YOUTUBE -> "輸入 YouTube 頻道名稱"
            WriteType.SOCIAL_TIKTOK -> "輸入 TikTok 用戶名稱"
            
            WriteType.VIDEO -> "輸入影片網址"
            WriteType.FILE -> "輸入檔案下載網址"
            WriteType.APPLICATION -> "輸入應用程式包名"
            WriteType.MAIL -> "輸入 Email 地址"
            WriteType.CONTACT -> "輸入聯絡人姓名"
            WriteType.PHONE -> "輸入電話號碼"
            WriteType.SMS -> "輸入簡訊內容"
            WriteType.LOCATION -> "輸入座標（緯度,經度）"
            WriteType.ADDRESS -> "輸入地址"
            WriteType.BITCOIN -> "輸入比特幣錢包地址"
            WriteType.BLUETOOTH -> "輸入藍牙裝置 MAC"
            WriteType.WIFI -> "輸入 WiFi SSID"
        }
    }
}

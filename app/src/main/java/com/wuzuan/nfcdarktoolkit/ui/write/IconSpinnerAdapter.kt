package com.wuzuan.nfcdarktoolkit.ui.write

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.wuzuan.nfcdarktoolkit.R

/**
 * 帶圖標的 Spinner Adapter
 */
class IconSpinnerAdapter(
    context: Context,
    private val items: List<IconSpinnerItem>
) : ArrayAdapter<IconSpinnerItem>(context, 0, items) {
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }
    
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }
    
    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item_with_icon, parent, false)
        
        val item = items[position]
        val icon = view.findViewById<ImageView>(R.id.icon)
        val text = view.findViewById<TextView>(R.id.text)
        
        icon.setImageResource(item.iconRes)
        text.text = item.text
        
        return view
    }
}

/**
 * Spinner 項目資料
 */
data class IconSpinnerItem(
    val text: String,
    val iconRes: Int,
    val value: WriteType? = null
)

/**
 * WriteType 工具函數 - 獲取對應圖標
 */
fun WriteType.getIconResource(): Int {
    return when (this) {
        // 基本
        WriteType.TEXT -> R.drawable.ic_text
        WriteType.URL -> R.drawable.ic_url
        WriteType.SEARCH -> R.drawable.ic_scan
        
        // 社交網路 - 使用品牌 Logo
        WriteType.SOCIAL_DISCORD -> R.drawable.ic_discord
        WriteType.SOCIAL_INSTAGRAM -> R.drawable.ic_instagram
        WriteType.SOCIAL_FACEBOOK -> R.drawable.ic_facebook
        WriteType.SOCIAL_LINE -> R.drawable.ic_line
        WriteType.SOCIAL_TELEGRAM -> R.drawable.ic_telegram
        WriteType.SOCIAL_TWITTER -> R.drawable.ic_twitter
        WriteType.SOCIAL_YOUTUBE -> R.drawable.ic_youtube
        WriteType.SOCIAL_TIKTOK -> R.drawable.ic_tiktok
        
        // 媒體
        WriteType.VIDEO -> R.drawable.ic_video
        WriteType.FILE -> R.drawable.ic_file
        WriteType.APPLICATION -> R.drawable.ic_app
        
        // 通訊
        WriteType.MAIL -> R.drawable.ic_email
        WriteType.CONTACT -> R.drawable.ic_contact
        WriteType.PHONE -> R.drawable.ic_phone
        WriteType.SMS -> R.drawable.ic_sms
        
        // 位置
        WriteType.LOCATION -> R.drawable.ic_location
        WriteType.ADDRESS -> R.drawable.ic_location
        
        // 其他
        WriteType.BITCOIN -> R.drawable.ic_bitcoin
        WriteType.BLUETOOTH -> R.drawable.ic_bluetooth
        WriteType.WIFI -> R.drawable.ic_wifi
    }
}


package com.wuzuan.nfcdarktoolkit.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wuzuan.nfcdarktoolkit.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragmentSimple : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_simple, container, false)
        
        try {
            setupListeners(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return view
    }
    
    private fun setupListeners(view: View) {
        view.findViewById<TextView>(R.id.tv_about_digital_card).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutDigitalCardFragment)
        }

        // Developer Mode Trigger
        var clickCount = 0
        var lastClickTime = 0L
        view.findViewById<TextView>(R.id.tv_author_label).setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 1000) {
                clickCount = 0
            }
            lastClickTime = currentTime
            clickCount++

            if (clickCount == 10) {
                com.wuzuan.nfcdarktoolkit.MainActivity.isDeveloperMode = !com.wuzuan.nfcdarktoolkit.MainActivity.isDeveloperMode
                val status = if (com.wuzuan.nfcdarktoolkit.MainActivity.isDeveloperMode) "開啟" else "關閉"
                com.google.android.material.snackbar.Snackbar.make(view, "開發者模式已$status", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                clickCount = 0
            }
        }

        // Related Links
        view.findViewById<TextView>(R.id.link_website).setOnClickListener { openUrl("https://diamondhost.tw/") }
        view.findViewById<TextView>(R.id.link_panel).setOnClickListener { openUrl("https://panel.diamondhost.tw/") }
        view.findViewById<TextView>(R.id.link_store).setOnClickListener { openUrl("https://store.diamondhost.tw/link.php?id=1") }
        view.findViewById<TextView>(R.id.link_status).setOnClickListener { openUrl("https://status.diamondhost.tw/") }
        view.findViewById<TextView>(R.id.link_docs).setOnClickListener { openUrl("https://docs.diamondhost.tw/") }
        view.findViewById<TextView>(R.id.link_discord).setOnClickListener { openUrl("https://discord.gg/5Fky5SEfBd") }
        view.findViewById<TextView>(R.id.link_telegram).setOnClickListener { openUrl("https://t.me/diamond_hosting") }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

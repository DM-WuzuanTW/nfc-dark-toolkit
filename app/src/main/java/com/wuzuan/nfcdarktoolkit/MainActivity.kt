package com.wuzuan.nfcdarktoolkit

import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.domain.model.AdProvider
import com.wuzuan.nfcdarktoolkit.nfc.NfcManager
import com.wuzuan.nfcdarktoolkit.ui.settings.SettingsViewModel
import com.wuzuan.nfcdarktoolkit.utils.Logger
import com.wuzuan.nfcdarktoolkit.utils.NfcPermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主 Activity
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var nfcManager: NfcManager
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private var nfcStatusBanner: View? = null
    
    private val adHandler = Handler(Looper.getMainLooper())
    private lateinit var adRunnable: Runnable
    
    // NFC 標籤事件流
    private val _nfcTagFlow = MutableSharedFlow<Tag>(
        replay = 1, 
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val nfcTagFlow = _nfcTagFlow.asSharedFlow()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupNavigation()
        checkNfcStatus()
        setupAdBanner()
        observeSettings()
        
        // 檢查啟動 Intent 是否包含 NFC 資料 (針對冷啟動)
        if (intent != null) {
            handleNfcIntent(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // 安全地啟用 NFC 前景調度
        nfcManager.safeEnableForegroundDispatch(this)
        
        checkNfcStatus()
        startAdRotation()
    }
    
    override fun onPause() {
        super.onPause()
        
        // 安全地禁用 NFC 前景調度
        nfcManager.safeDisableForegroundDispatch(this)
        
        stopAdRotation()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // 處理 NFC Intent
        handleNfcIntent(intent)
    }
    
    /**
     * 處理 NFC Intent
     */
    private fun handleNfcIntent(intent: Intent) {
        Logger.d("MainActivity", "收到 Intent: ${intent.action}")
        
        if (isNfcIntent(intent)) {
            try {
                val tag = nfcManager.getTagFromIntent(intent)
                if (tag != null) {
                    val tagId = tag.id.joinToString(":") { "%02X".format(it) }
                    Logger.nfc("MainActivity", "從 Intent 獲取標籤: $tagId")
                    Logger.nfc("MainActivity", "標籤技術: ${tag.techList.joinToString()}")
                    
                    lifecycleScope.launch {
                        _nfcTagFlow.emit(tag)
                    }
                } else {
                    Logger.w("Intent 中沒有找到標籤資料")
                }
            } catch (e: Exception) {
                Logger.e("處理 NFC Intent 失敗: ${e.message}", e)
                // 顯示錯誤訊息
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "處理 NFC 標籤失敗: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else {
            Logger.d("MainActivity", "非 NFC Intent，忽略")
        }
    }
    
    /**
     * 檢查是否為 NFC Intent
     */
    private fun isNfcIntent(intent: Intent): Boolean {
        return intent.action in listOf(
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED
        )
    }
    
    /**
     * 設定導航
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
    }
    
    /**
     * 設定廣告橫幅
     */
    private fun setupAdBanner() {
        val adBanner = findViewById<LinearLayout>(R.id.ad_banner)
        val adTitle = findViewById<TextView>(R.id.ad_title)
        val adDescription = findViewById<TextView>(R.id.ad_description)

        adRunnable = Runnable {
            val ad = AdProvider.getRandomAd()
            adTitle.text = ad.title
            adDescription.text = ad.description
            adBanner.setOnClickListener { openUrl(ad.url) }
            adHandler.postDelayed(adRunnable, 15000)
        }
    }

    private fun startAdRotation() {
        adHandler.post(adRunnable)
    }

    private fun stopAdRotation() {
        adHandler.removeCallbacks(adRunnable)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            settingsViewModel.themeMode.collect { mode ->
                when (mode) {
                    "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
        lifecycleScope.launch {
            settingsViewModel.autoSaveHistory.collect { 
                // 持續觀察以確保 DataStore 保持活躍
            }
        }
        lifecycleScope.launch {
            settingsViewModel.safeModeEnabled.collect {
                // 持續觀察以確保 DataStore 保持活躍
            }
        }
    }
    
    /**
     * 檢查 NFC 狀態
     */
    private fun checkNfcStatus() {
        val nfcStatus = NfcPermissionHelper.getNfcStatus(this)
        Logger.d("NFC 狀態檢查: $nfcStatus")
        
        when (nfcStatus) {
            com.wuzuan.nfcdarktoolkit.utils.NfcStatus.NOT_SUPPORTED -> {
                showNfcNotSupported()
            }
            com.wuzuan.nfcdarktoolkit.utils.NfcStatus.NO_PERMISSION -> {
                showNfcPermissionRequired()
            }
            com.wuzuan.nfcdarktoolkit.utils.NfcStatus.DISABLED -> {
                showNfcDisabled()
            }
            com.wuzuan.nfcdarktoolkit.utils.NfcStatus.AVAILABLE -> {
                hideNfcStatusBanner()
            }
        }
    }
    
    /**
     * 顯示不支援 NFC 的提示
     */
    private fun showNfcNotSupported() {
        Snackbar.make(
            findViewById(android.R.id.content),
            R.string.nfc_not_supported,
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }
    
    /**
     * 顯示 NFC 權限需求的提示
     */
    private fun showNfcPermissionRequired() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "需要 NFC 權限才能使用此功能",
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }
    
    /**
     * 顯示 NFC 未開啟的提示
     */
    private fun showNfcDisabled() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "請開啟 NFC 功能",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("前往設定") {
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }.show()
    }
    
    /**
     * 隱藏 NFC 狀態橫幅
     */
    private fun hideNfcStatusBanner() {
        nfcStatusBanner?.visibility = View.GONE
    }
}

package com.wuzuan.nfcdarktoolkit

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.wuzuan.nfcdarktoolkit.nfc.NfcManager
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
    
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView
    private var nfcStatusBanner: View? = null
    
    // NFC 標籤事件流
    private val _nfcTagFlow = MutableSharedFlow<Tag>(replay = 0)
    val nfcTagFlow = _nfcTagFlow.asSharedFlow()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupNavigation()
        checkNfcStatus()
    }
    
    override fun onResume() {
        super.onResume()
        
        // 啟用 NFC 前景調度
        if (nfcManager.isNfcSupported()) {
            nfcManager.enableForegroundDispatch(this)
        }
        
        checkNfcStatus()
    }
    
    override fun onPause() {
        super.onPause()
        
        // 禁用 NFC 前景調度
        if (nfcManager.isNfcSupported()) {
            nfcManager.disableForegroundDispatch(this)
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // 處理 NFC Intent
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            
            val tag = nfcManager.getTagFromIntent(intent)
            if (tag != null) {
                lifecycleScope.launch {
                    _nfcTagFlow.emit(tag)
                }
            }
        }
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
     * 檢查 NFC 狀態
     */
    private fun checkNfcStatus() {
        when {
            !nfcManager.isNfcSupported() -> {
                showNfcNotSupported()
            }
            !nfcManager.isNfcEnabled() -> {
                showNfcDisabled()
            }
            else -> {
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
     * 顯示 NFC 未開啟的提示
     */
    private fun showNfcDisabled() {
        Snackbar.make(
            findViewById(android.R.id.content),
            R.string.nfc_disabled_message,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.nfc_go_to_settings) {
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


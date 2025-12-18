package com.wuzuan.nfcdarktoolkit.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import androidx.core.content.ContextCompat

/**
 * NFC 權限檢查工具
 */
object NfcPermissionHelper {
    
    /**
     * 檢查 NFC 權限
     */
    fun hasNfcPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.NFC
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 檢查設備是否支援 NFC
     */
    fun isNfcSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
    }
    
    /**
     * 檢查 NFC 是否啟用
     */
    fun isNfcEnabled(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * 檢查 NFC 完整狀態
     */
    fun getNfcStatus(context: Context): NfcStatus {
        return when {
            !isNfcSupported(context) -> NfcStatus.NOT_SUPPORTED
            !hasNfcPermission(context) -> NfcStatus.NO_PERMISSION
            !isNfcEnabled(context) -> NfcStatus.DISABLED
            else -> NfcStatus.AVAILABLE
        }
    }
}

enum class NfcStatus {
    AVAILABLE,
    NOT_SUPPORTED,
    NO_PERMISSION,
    DISABLED
}

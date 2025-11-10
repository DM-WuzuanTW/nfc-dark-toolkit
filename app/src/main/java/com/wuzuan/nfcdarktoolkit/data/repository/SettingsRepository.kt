package com.wuzuan.nfcdarktoolkit.data.repository

import com.wuzuan.nfcdarktoolkit.data.local.prefs.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 設定 Repository
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    
    val themeMode: Flow<String> = settingsDataStore.themeMode
    val autoSaveHistory: Flow<Boolean> = settingsDataStore.autoSaveHistory
    val defaultWriteFormat: Flow<String> = settingsDataStore.defaultWriteFormat
    val safeModeEnabled: Flow<Boolean> = settingsDataStore.safeModeEnabled
    val isFirstLaunch: Flow<Boolean> = settingsDataStore.isFirstLaunch
    val legalNoticeAccepted: Flow<Boolean> = settingsDataStore.legalNoticeAccepted
    
    suspend fun setThemeMode(mode: String) {
        settingsDataStore.setThemeMode(mode)
    }
    
    suspend fun setAutoSaveHistory(enabled: Boolean) {
        settingsDataStore.setAutoSaveHistory(enabled)
    }
    
    suspend fun setDefaultWriteFormat(format: String) {
        settingsDataStore.setDefaultWriteFormat(format)
    }
    
    suspend fun setSafeModeEnabled(enabled: Boolean) {
        settingsDataStore.setSafeModeEnabled(enabled)
    }
    
    suspend fun setFirstLaunch(isFirst: Boolean) {
        settingsDataStore.setFirstLaunch(isFirst)
    }
    
    suspend fun setLegalNoticeAccepted(accepted: Boolean) {
        settingsDataStore.setLegalNoticeAccepted(accepted)
    }
}


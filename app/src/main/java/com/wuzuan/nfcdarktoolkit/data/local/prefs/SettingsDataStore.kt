package com.wuzuan.nfcdarktoolkit.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 設定資料存儲
 */
class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val AUTO_SAVE_HISTORY = booleanPreferencesKey("auto_save_history")
        private val DEFAULT_WRITE_FORMAT = stringPreferencesKey("default_write_format")
        private val SAFE_MODE_ENABLED = booleanPreferencesKey("safe_mode_enabled")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val LEGAL_NOTICE_ACCEPTED = booleanPreferencesKey("legal_notice_accepted")
        
        const val THEME_SYSTEM = "system"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        
        const val FORMAT_TEXT = "text"
        const val FORMAT_URL = "url"
        const val FORMAT_JSON = "json"
        const val FORMAT_WIFI = "wifi"
        const val FORMAT_VCARD = "vcard"
    }
    
    // 主題模式
    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: THEME_SYSTEM
        }
    
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
    
    // 自動儲存歷史
    val autoSaveHistory: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_SAVE_HISTORY] ?: true
        }
    
    suspend fun setAutoSaveHistory(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SAVE_HISTORY] = enabled
        }
    }
    
    // 預設寫入格式
    val defaultWriteFormat: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_WRITE_FORMAT] ?: FORMAT_TEXT
        }
    
    suspend fun setDefaultWriteFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_WRITE_FORMAT] = format
        }
    }
    
    // 安全模式
    val safeModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SAFE_MODE_ENABLED] ?: false
        }
    
    suspend fun setSafeModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SAFE_MODE_ENABLED] = enabled
        }
    }
    
    // 首次啟動
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FIRST_LAUNCH] ?: true
        }
    
    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = isFirst
        }
    }
    
    // 法律聲明已接受
    val legalNoticeAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[LEGAL_NOTICE_ACCEPTED] ?: false
        }
    
    suspend fun setLegalNoticeAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LEGAL_NOTICE_ACCEPTED] = accepted
        }
    }
}


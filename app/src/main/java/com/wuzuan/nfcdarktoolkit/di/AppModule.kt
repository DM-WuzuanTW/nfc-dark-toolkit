package com.wuzuan.nfcdarktoolkit.di

import android.content.Context
import android.nfc.NfcAdapter
import com.wuzuan.nfcdarktoolkit.data.local.prefs.SettingsDataStore
import com.wuzuan.nfcdarktoolkit.utils.DebugReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * App Module
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideNfcAdapter(
        @ApplicationContext context: Context
    ): NfcAdapter? {
        return NfcAdapter.getDefaultAdapter(context)
    }
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }
    
    @Provides
    @Singleton
    fun provideDebugReporter(
        @ApplicationContext context: Context
    ): DebugReporter {
        return DebugReporter(context)
    }
}

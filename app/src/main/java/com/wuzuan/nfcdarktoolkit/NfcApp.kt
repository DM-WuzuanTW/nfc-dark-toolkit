package com.wuzuan.nfcdarktoolkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 類
 */
@HiltAndroidApp
class NfcApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}


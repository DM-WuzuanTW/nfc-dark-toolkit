package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 標籤保護功能（參考 NFC Tools）
 */
@Singleton
class TagProtection @Inject constructor() {
    
    /**
     * 為 NTAG 標籤設定密碼保護
     */
    fun setPasswordProtection(tag: Tag, password: ByteArray): Result<Unit> {
        if (password.size != 4) {
            return Result.failure(IllegalArgumentException("密碼必須是 4 個字節"))
        }
        
        return try {
            val ultralight = MifareUltralight.get(tag)
                ?: return Result.failure(IOException("不支援的標籤類型"))
            
            ultralight.connect()
            
            // NTAG 密碼設定在特定頁面
            // 這是簡化版本，實際實作需要根據具體標籤類型調整
            ultralight.writePage(0x2B, password) // PWD 頁面
            
            // 設定 PACK（密碼確認）
            val pack = byteArrayOf(0x00, 0x00, 0x00, 0x00)
            ultralight.writePage(0x2C, pack)
            
            ultralight.close()
            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 驗證密碼保護的標籤
     */
    fun authenticateWithPassword(tag: Tag, password: ByteArray): Result<Unit> {
        if (password.size != 4) {
            return Result.failure(IllegalArgumentException("密碼必須是 4 個字節"))
        }
        
        return try {
            val nfcA = NfcA.get(tag)
                ?: return Result.failure(IOException("不支援的標籤類型"))
            
            nfcA.connect()
            
            // 發送 PWD_AUTH 指令
            val authCommand = byteArrayOf(
                0x1B.toByte(), // PWD_AUTH 指令
                password[0], password[1], password[2], password[3]
            )
            
            val response = nfcA.transceive(authCommand)
            nfcA.close()
            
            if (response.size == 2) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("密碼驗證失敗"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 檢查標籤是否有密碼保護
     */
    fun isPasswordProtected(tag: Tag): Boolean {
        return try {
            val ultralight = MifareUltralight.get(tag) ?: return false
            ultralight.connect()
            
            // 嘗試讀取 AUTH0 頁面（0x29）
            val page = ultralight.readPages(0x29)
            val auth0 = page[0].toInt() and 0xFF
            
            ultralight.close()
            
            // 如果 AUTH0 不是 0xFF，表示有設定保護
            auth0 != 0xFF
        } catch (e: Exception) {
            false
        }
    }
}


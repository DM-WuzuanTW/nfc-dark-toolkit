package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 標籤操作類（格式化、鎖定、複製）
 */
@Singleton
class TagOperations @Inject constructor() {
    
    /**
     * 格式化標籤為 NDEF
     */
    fun formatTag(tag: Tag): Result<Unit> {
        return try {
            val ndefFormatable = NdefFormatable.get(tag)
                ?: return Result.failure(IOException("標籤不支援 NDEF 格式化"))
            
            ndefFormatable.connect()
            
            // 建立空的 NDEF Message
            val emptyMessage = NdefMessage(byteArrayOf())
            ndefFormatable.format(emptyMessage)
            
            ndefFormatable.close()
            Result.success(Unit)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: FormatException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 鎖定標籤（設為唯讀）
     */
    fun lockTag(tag: Tag): Result<Unit> {
        return try {
            val ndef = Ndef.get(tag)
                ?: return Result.failure(IOException("標籤不支援 NDEF"))
            
            ndef.connect()
            
            if (!ndef.isWritable) {
                ndef.close()
                return Result.failure(IOException("標籤已經是唯讀"))
            }
            
            if (!ndef.canMakeReadOnly()) {
                ndef.close()
                return Result.failure(IOException("標籤不支援設為唯讀"))
            }
            
            val success = ndef.makeReadOnly()
            ndef.close()
            
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("鎖定失敗"))
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
     * 讀取標籤的 NDEF Message（用於複製）
     */
    fun readTagForCloning(tag: Tag): Result<NdefMessage> {
        return try {
            val ndef = Ndef.get(tag)
                ?: return Result.failure(IOException("標籤不支援 NDEF"))
            
            ndef.connect()
            val message = ndef.cachedNdefMessage
            ndef.close()
            
            if (message != null) {
                Result.success(message)
            } else {
                Result.failure(IOException("標籤無資料"))
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
     * 寫入 NDEF Message 到標籤（用於複製）
     */
    fun writeTagForCloning(tag: Tag, message: NdefMessage): Result<Unit> {
        return try {
            val ndef = Ndef.get(tag)
            
            if (ndef != null) {
                ndef.connect()
                
                if (!ndef.isWritable) {
                    ndef.close()
                    return Result.failure(IOException("目標標籤不可寫入"))
                }
                
                if (ndef.maxSize < message.toByteArray().size) {
                    ndef.close()
                    return Result.failure(IOException("目標標籤容量不足"))
                }
                
                ndef.writeNdefMessage(message)
                ndef.close()
                Result.success(Unit)
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                    ?: return Result.failure(IOException("目標標籤不支援 NDEF"))
                
                ndefFormatable.connect()
                ndefFormatable.format(message)
                ndefFormatable.close()
                Result.success(Unit)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: FormatException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}


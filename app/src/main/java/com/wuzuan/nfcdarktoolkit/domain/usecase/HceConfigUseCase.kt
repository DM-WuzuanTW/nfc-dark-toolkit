package com.wuzuan.nfcdarktoolkit.domain.usecase

import com.wuzuan.nfcdarktoolkit.domain.model.HceConfig
import com.wuzuan.nfcdarktoolkit.nfc.HceService
import javax.inject.Inject

/**
 * HCE 配置 UseCase
 */
class HceConfigUseCase @Inject constructor() {
    
    /**
     * 啟動 HCE 模擬
     */
    fun startEmulation(config: HceConfig): Result<Unit> {
        return try {
            // 驗證 AID
            if (!isValidAid(config.aid)) {
                return Result.failure(IllegalArgumentException("AID 格式錯誤"))
            }
            
            // 設定回應資料
            HceService.customResponseData = config.responseData?.let { hexStringToBytes(it) }
            HceService.isActive = true
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 停止 HCE 模擬
     */
    fun stopEmulation(): Result<Unit> {
        return try {
            HceService.isActive = false
            HceService.customResponseData = null
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 驗證 AID 格式
     */
    private fun isValidAid(aid: String): Boolean {
        val cleanAid = aid.replace(" ", "").replace(":", "")
        return cleanAid.matches(Regex("[0-9A-Fa-f]{10,}"))
    }
    
    /**
     * 十六進位字串轉 ByteArray
     */
    private fun hexStringToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "").replace(":", "")
        return cleanHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}


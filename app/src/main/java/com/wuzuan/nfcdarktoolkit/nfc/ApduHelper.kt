package com.wuzuan.nfcdarktoolkit.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.wuzuan.nfcdarktoolkit.domain.model.ApduCommand
import com.wuzuan.nfcdarktoolkit.domain.model.ApduResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APDU 輔助工具（F18-F19）
 */
@Singleton
class ApduHelper @Inject constructor() {
    
    /**
     * 發送 APDU 指令到標籤
     */
    fun sendApdu(tag: Tag, command: ApduCommand): Result<ApduResponse> {
        return try {
            val isoDep = IsoDep.get(tag)
                ?: return Result.failure(IOException("標籤不支援 ISO-DEP (APDU)"))
            
            isoDep.connect()
            isoDep.timeout = 3000 // 3 秒超時
            
            val response = isoDep.transceive(command.command)
            isoDep.close()
            
            if (response.size < 2) {
                return Result.failure(IOException("回應資料過短"))
            }
            
            // 解析 SW1 SW2 狀態碼（最後兩個字節）
            val sw1 = response[response.size - 2]
            val sw2 = response[response.size - 1]
            val data = response.copyOfRange(0, response.size - 2)
            
            val statusDescription = getStatusDescription(sw1, sw2)
            
            val apduResponse = ApduResponse(
                response = data,
                sw1 = sw1,
                sw2 = sw2,
                statusDescription = statusDescription
            )
            
            Result.success(apduResponse)
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 創建 SELECT AID 指令
     */
    fun createSelectCommand(aid: String): ApduCommand {
        val aidBytes = hexStringToByteArray(aid)
        val command = byteArrayOf(
            0x00.toByte(),  // CLA
            0xA4.toByte(),  // INS (SELECT)
            0x04.toByte(),  // P1 (Select by name)
            0x00.toByte(),  // P2
            aidBytes.size.toByte()  // Lc
        ) + aidBytes
        
        return ApduCommand(command, "SELECT AID: $aid")
    }
    
    /**
     * 創建讀取資料指令
     */
    fun createReadCommand(offset: Int, length: Int): ApduCommand {
        val command = byteArrayOf(
            0x00.toByte(),  // CLA
            0xB0.toByte(),  // INS (READ BINARY)
            (offset shr 8).toByte(),  // P1 (offset high)
            (offset and 0xFF).toByte(),  // P2 (offset low)
            length.toByte()  // Le (expected length)
        )
        
        return ApduCommand(command, "READ at offset $offset, length $length")
    }
    
    /**
     * 解析狀態碼
     */
    private fun getStatusDescription(sw1: Byte, sw2: Byte): String {
        val sw1Int = sw1.toInt() and 0xFF
        val sw2Int = sw2.toInt() and 0xFF
        
        return when {
            sw1Int == 0x90 && sw2Int == 0x00 -> "成功 (9000)"
            sw1Int == 0x61 -> "還有 $sw2Int 字節可讀取"
            sw1Int == 0x6A && sw2Int == 0x82 -> "檔案未找到 (6A82)"
            sw1Int == 0x6A && sw2Int == 0x86 -> "參數錯誤 (6A86)"
            sw1Int == 0x69 && sw2Int == 0x82 -> "安全狀態不滿足 (6982)"
            sw1Int == 0x69 && sw2Int == 0x85 -> "使用條件不滿足 (6985)"
            sw1Int == 0x6D && sw2Int == 0x00 -> "指令不支援 (6D00)"
            sw1Int == 0x6E && sw2Int == 0x00 -> "類別不支援 (6E00)"
            sw1Int == 0x6F && sw2Int == 0x00 -> "未知錯誤 (6F00)"
            else -> String.format("狀態碼: %02X%02X", sw1Int, sw2Int)
        }
    }
    
    /**
     * 十六進位字串轉 ByteArray
     */
    fun hexStringToByteArray(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "").replace(":", "")
        return cleanHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
    
    /**
     * ByteArray 轉十六進位字串
     */
    fun byteArrayToHexString(bytes: ByteArray, separator: String = " "): String {
        return bytes.joinToString(separator) { byte ->
            "%02X".format(byte)
        }
    }
    
    /**
     * 常用 APDU 指令模板
     */
    fun getCommonCommands(): List<Pair<String, String>> {
        return listOf(
            "SELECT MasterCard" to "00A404000E325041592E5359532E444446303100",
            "SELECT Visa" to "00A4040007A0000000031010",
            "GET DATA" to "00CA9F7F00",
            "READ RECORD" to "00B2011400",
            "GET CHALLENGE" to "0084000008"
        )
    }
}


package com.wuzuan.nfcdarktoolkit.domain.exception

/**
 * NFC 相關異常類
 * 提供更具體的錯誤分類和處理
 */

/**
 * NFC 基礎異常
 */
sealed class NfcException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * NFC 不支援異常
 */
class NfcNotSupportedException(message: String = "設備不支援 NFC") : NfcException(message)

/**
 * NFC 未啟用異常
 */
class NfcNotEnabledException(message: String = "NFC 未啟用") : NfcException(message)

/**
 * 標籤讀取異常
 */
class TagReadException(message: String, cause: Throwable? = null) : NfcException(message, cause)

/**
 * 標籤寫入異常
 */
class TagWriteException(message: String, cause: Throwable? = null) : NfcException(message, cause)

/**
 * 標籤不可寫入異常
 */
class TagNotWritableException(message: String = "標籤不可寫入") : NfcException(message)

/**
 * 標籤容量不足異常
 */
class TagInsufficientSpaceException(
    required: Int, 
    available: Int,
    message: String = "標籤容量不足：需要 $required bytes，可用 $available bytes"
) : NfcException(message)

/**
 * 標籤格式化異常
 */
class TagFormatException(message: String, cause: Throwable? = null) : NfcException(message, cause)

/**
 * 標籤連接異常
 */
class TagConnectionException(message: String, cause: Throwable? = null) : NfcException(message, cause)

/**
 * NDEF 解析異常
 */
class NdefParseException(message: String, cause: Throwable? = null) : NfcException(message, cause)

package com.wuzuan.nfcdarktoolkit.domain.model

/**
 * HCE 模擬配置
 */
data class HceConfig(
    val aid: String,
    val isActive: Boolean = false,
    val responseData: String? = null,
    val description: String? = null
)

/**
 * APDU 指令與回應
 */
data class ApduCommand(
    val command: ByteArray,
    val description: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApduCommand
        return command.contentEquals(other.command)
    }

    override fun hashCode(): Int {
        return command.contentHashCode()
    }
}

data class ApduResponse(
    val response: ByteArray,
    val sw1: Byte,
    val sw2: Byte,
    val statusDescription: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApduResponse
        if (!response.contentEquals(other.response)) return false
        if (sw1 != other.sw1) return false
        if (sw2 != other.sw2) return false
        return true
    }

    override fun hashCode(): Int {
        var result = response.contentHashCode()
        result = 31 * result + sw1
        result = 31 * result + sw2
        return result
    }
}


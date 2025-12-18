package com.wuzuan.nfcdarktoolkit.domain.model

/**
 * NFC 操作結果封裝類
 * 提供統一的操作結果處理
 */
sealed class NfcOperationResult<out T> {
    
    /**
     * 操作成功
     */
    data class Success<T>(
        val data: T,
        val message: String? = null,
        val executionTime: Long? = null
    ) : NfcOperationResult<T>()
    
    /**
     * 操作失敗
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "未知錯誤",
        val errorCode: String? = null
    ) : NfcOperationResult<Nothing>()
    
    /**
     * 操作進行中
     */
    data class Loading(
        val message: String = "處理中...",
        val progress: Float? = null
    ) : NfcOperationResult<Nothing>()
    
    /**
     * 操作被取消
     */
    data class Cancelled(
        val reason: String = "操作被取消"
    ) : NfcOperationResult<Nothing>()
    
    // 便利方法
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    val isCancelled: Boolean get() = this is Cancelled
    
    /**
     * 獲取成功數據
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * 獲取錯誤信息
     */
    fun getErrorOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }
    
    /**
     * 映射成功結果
     */
    inline fun <R> map(transform: (T) -> R): NfcOperationResult<R> = when (this) {
        is Success -> Success(transform(data), message, executionTime)
        is Error -> this
        is Loading -> this
        is Cancelled -> this
    }
    
    /**
     * 平面映射
     */
    inline fun <R> flatMap(transform: (T) -> NfcOperationResult<R>): NfcOperationResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
        is Cancelled -> this
    }
    
    companion object {
        
        /**
         * 從 Result 轉換
         */
        fun <T> fromResult(result: Result<T>): NfcOperationResult<T> {
            return result.fold(
                onSuccess = { Success(it) },
                onFailure = { Error(it) }
            )
        }
        
        /**
         * 創建成功結果
         */
        fun <T> success(data: T, message: String? = null): NfcOperationResult<T> {
            return Success(data, message)
        }
        
        /**
         * 創建錯誤結果
         */
        fun error(exception: Throwable, message: String? = null): NfcOperationResult<Nothing> {
            return Error(exception, message ?: exception.message ?: "未知錯誤")
        }
        
        /**
         * 創建載入狀態
         */
        fun loading(message: String = "處理中...", progress: Float? = null): NfcOperationResult<Nothing> {
            return Loading(message, progress)
        }
        
        /**
         * 創建取消狀態
         */
        fun cancelled(reason: String = "操作被取消"): NfcOperationResult<Nothing> {
            return Cancelled(reason)
        }
    }
}

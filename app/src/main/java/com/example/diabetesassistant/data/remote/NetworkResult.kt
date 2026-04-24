package com.example.diabetesassistant.data.remote

/**
 * 网络调用结果。
 */
sealed interface NetworkResult<out T> {
    /**
     * 成功结果。
     */
    data class Success<T>(val data: T) : NetworkResult<T>

    /**
     * 失败结果。
     */
    data class Error(
        val code: Int? = null,
        val message: String,
        val throwable: Throwable? = null,
    ) : NetworkResult<Nothing>
}

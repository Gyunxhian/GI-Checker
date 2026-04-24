package com.example.diabetesassistant.data.remote.interceptor

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 简单重试拦截器：网络异常或 5xx 自动重试。
 */
class RetryInterceptor(
    private val maxRetries: Int = 2,
) : Interceptor {
    /**
     * 执行并重试请求。
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                val response = chain.proceed(request)
                if (response.code < 500 || attempt == maxRetries) {
                    return response
                }
                response.close()
            } catch (exception: IOException) {
                lastException = exception
                if (attempt == maxRetries) {
                    throw exception
                }
            }
            attempt++
        }

        throw lastException ?: IOException("Unknown network error.")
    }
}

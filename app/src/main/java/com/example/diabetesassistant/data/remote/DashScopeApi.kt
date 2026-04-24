package com.example.diabetesassistant.data.remote

import com.example.diabetesassistant.data.remote.dto.DashScopeRequest
import com.example.diabetesassistant.data.remote.dto.DashScopeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * DashScope Qwen VL 接口。
 */
interface DashScopeApi {
    /**
     * 发送识别请求。
     */
    @POST("services/aigc/multimodal-generation/generation")
    suspend fun recognizeDish(
        @Header("Authorization") authorization: String,
        @Body request: DashScopeRequest,
    ): DashScopeResponse
}

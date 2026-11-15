package com.example.diabetesassistant.data.remote

import com.example.diabetesassistant.data.remote.dto.DeepSeekRequest
import com.example.diabetesassistant.data.remote.dto.DeepSeekResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest,
    ): DeepSeekResponse

    @Streaming
    @POST("chat/completions")
    suspend fun chatStream(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest,
    ): ResponseBody
}

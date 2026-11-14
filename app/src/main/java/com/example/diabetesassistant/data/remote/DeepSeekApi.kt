package com.example.diabetesassistant.data.remote

import com.example.diabetesassistant.data.remote.dto.DeepSeekRequest
import com.example.diabetesassistant.data.remote.dto.DeepSeekResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest,
    ): DeepSeekResponse
}

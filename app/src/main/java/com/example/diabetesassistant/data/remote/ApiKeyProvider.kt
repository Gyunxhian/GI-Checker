package com.example.diabetesassistant.data.remote

import com.example.diabetesassistant.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API Key 提供器。
 */
@Singleton
class ApiKeyProvider @Inject constructor() {
    /**
     * DashScope Key。
     */
    fun dashScopeApiKey(): String = BuildConfig.DASHSCOPE_API_KEY

    /**
     * USDA Key。
     */
    fun usdaApiKey(): String = BuildConfig.USDA_API_KEY
}

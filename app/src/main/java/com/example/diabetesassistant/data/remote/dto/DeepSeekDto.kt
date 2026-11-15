package com.example.diabetesassistant.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeepSeekRequest(
    @SerializedName("model") val model: String = "deepseek-flash",
    @SerializedName("messages") val messages: List<DeepSeekMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 2048,
    @SerializedName("temperature") val temperature: Float = 0.1f,
    @SerializedName("stream") val stream: Boolean = false,
)

data class DeepSeekMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: Any, // String or List<DeepSeekContentPart>
)

data class DeepSeekContentPart(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("image_url") val imageUrl: DeepSeekImageUrl? = null,
)

data class DeepSeekImageUrl(
    @SerializedName("url") val url: String,
)

data class DeepSeekResponse(
    @SerializedName("choices") val choices: List<DeepSeekChoice> = emptyList(),
)

data class DeepSeekChoice(
    @SerializedName("message") val message: DeepSeekResponseMessage?,
)

data class DeepSeekResponseMessage(
    @SerializedName("content") val content: String? = null,
    @SerializedName("reasoning_content") val reasoningContent: String? = null,
)

data class DeepSeekStreamChunk(
    @SerializedName("choices") val choices: List<DeepSeekStreamChoice> = emptyList(),
)

data class DeepSeekStreamChoice(
    @SerializedName("delta") val delta: DeepSeekStreamDelta?,
    @SerializedName("finish_reason") val finishReason: String? = null,
)

data class DeepSeekStreamDelta(
    @SerializedName("content") val content: String? = null,
    @SerializedName("reasoning_content") val reasoningContent: String? = null,
)

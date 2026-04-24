package com.example.diabetesassistant.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DashScope Qwen VL 请求体。
 */
data class DashScopeRequest(
    @SerializedName("model")
    val model: String = "qwen-vl-plus",
    @SerializedName("input")
    val input: DashScopeInput,
    @SerializedName("parameters")
    val parameters: DashScopeParameters = DashScopeParameters(),
)

/**
 * DashScope 输入数据。
 */
data class DashScopeInput(
    @SerializedName("messages")
    val messages: List<DashScopeMessage>,
)

/**
 * DashScope 对话消息。
 */
data class DashScopeMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: List<DashScopeContent>,
)

/**
 * DashScope 消息内容。
 */
data class DashScopeContent(
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("image")
    val image: String? = null,
)

/**
 * DashScope 可选参数。
 */
data class DashScopeParameters(
    @SerializedName("result_format")
    val resultFormat: String = "message",
)

/**
 * DashScope 响应体。
 */
data class DashScopeResponse(
    @SerializedName("output")
    val output: DashScopeOutput?,
)

/**
 * DashScope 输出结构。
 */
data class DashScopeOutput(
    @SerializedName("choices")
    val choices: List<DashScopeChoice> = emptyList(),
)

/**
 * DashScope 选项结构。
 */
data class DashScopeChoice(
    @SerializedName("message")
    val message: DashScopeAssistantMessage?,
)

/**
 * DashScope 助手消息。
 */
data class DashScopeAssistantMessage(
    @SerializedName("content")
    val content: List<DashScopeAssistantContent> = emptyList(),
)

/**
 * DashScope 助手内容。
 */
data class DashScopeAssistantContent(
    @SerializedName("text")
    val text: String? = null,
)

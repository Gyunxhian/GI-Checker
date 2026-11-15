package com.example.diabetesassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI 健康助手会话消息实体。
 * 保存在本地私有数据库中，支持多轮上下文与一键隐私销毁。
 */
@Entity(tableName = "ai_chat_messages")
data class AiChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "role")
    val role: String, // "user" or "assistant"
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "reasoning_content")
    val reasoningContent: String = "",
    @ColumnInfo(name = "search_sources_json")
    val searchSourcesJson: String = "",
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
)

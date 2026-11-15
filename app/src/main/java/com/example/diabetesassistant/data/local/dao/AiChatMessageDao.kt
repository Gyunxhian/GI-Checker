package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diabetesassistant.data.local.entity.AiChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * AI 助手聊天记录 DAO。
 */
@Dao
interface AiChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiChatMessage): Long

    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<AiChatMessage>>

    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int = 10): List<AiChatMessage>

    @Query("DELETE FROM ai_chat_messages")
    suspend fun deleteAll(): Int
}

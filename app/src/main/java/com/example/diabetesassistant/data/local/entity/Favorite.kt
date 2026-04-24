package com.example.diabetesassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 帖子收藏实体。
 */
@Entity(
    tableName = "favorites",
    primaryKeys = ["user_id", "post_id"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CommunityPost::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("post_id")],
)
data class Favorite(
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "post_id")
    val postId: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

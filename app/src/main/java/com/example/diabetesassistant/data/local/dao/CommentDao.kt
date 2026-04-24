package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diabetesassistant.data.local.entity.Comment
import kotlinx.coroutines.flow.Flow

/**
 * 评论数据访问接口。
 */
@Dao
interface CommentDao {
    /**
     * 新增评论。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: Comment): Long

    /**
     * 删除评论。
     */
    @Delete
    suspend fun delete(comment: Comment): Int

    /**
     * 观察帖子评论列表。
     */
    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at DESC")
    fun observeByPostId(postId: Long): Flow<List<Comment>>
}

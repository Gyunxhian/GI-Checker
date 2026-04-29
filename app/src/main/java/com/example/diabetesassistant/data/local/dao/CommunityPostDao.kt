package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diabetesassistant.data.local.entity.CommunityPost
import kotlinx.coroutines.flow.Flow

/**
 * 社区帖子数据访问接口。
 */
@Dao
interface CommunityPostDao {
    /**
     * 新增帖子。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: CommunityPost): Long

    /**
     * 更新帖子。
     */
    @Update
    suspend fun update(post: CommunityPost): Int

    /**
     * 删除帖子。
     */
    @Delete
    suspend fun delete(post: CommunityPost): Int

    /**
     * 查询帖子详情。
     */
    @Query("SELECT * FROM community_posts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CommunityPost?

    /**
     * 观察帖子详情。
     */
    @Query("SELECT * FROM community_posts WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<CommunityPost?>

    /**
     * 观察帖子流。
     */
    @Query("SELECT * FROM community_posts ORDER BY created_at DESC")
    fun observeAll(): Flow<List<CommunityPost>>
}

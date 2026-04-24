package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diabetesassistant.data.local.entity.Favorite
import kotlinx.coroutines.flow.Flow

/**
 * 收藏数据访问接口。
 */
@Dao
interface FavoriteDao {
    /**
     * 新增收藏。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: Favorite): Long

    /**
     * 取消收藏。
     */
    @Query("DELETE FROM favorites WHERE user_id = :userId AND post_id = :postId")
    suspend fun delete(userId: Long, postId: Long)

    /**
     * 判断帖子是否已收藏。
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = :userId AND post_id = :postId)")
    fun observeIsFavorite(userId: Long, postId: Long): Flow<Boolean>

    /**
     * 观察用户全部收藏帖子ID。
     */
    @Query("SELECT post_id FROM favorites WHERE user_id = :userId")
    fun observePostIdsByUserId(userId: Long): Flow<List<Long>>
}

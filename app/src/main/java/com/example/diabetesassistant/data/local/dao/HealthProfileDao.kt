package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diabetesassistant.data.local.entity.HealthProfile
import kotlinx.coroutines.flow.Flow

/**
 * 健康档案数据访问接口。
 */
@Dao
interface HealthProfileDao {
    /**
     * 新增健康档案。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: HealthProfile): Long

    /**
     * 更新健康档案。
     */
    @Update
    suspend fun update(profile: HealthProfile): Int

    /**
     * 观察用户健康档案。
     */
    @Query("SELECT * FROM health_profiles WHERE user_id = :userId LIMIT 1")
    fun observeByUserId(userId: Long): Flow<HealthProfile?>

    /**
     * 查询用户健康档案。
     */
    @Query("SELECT * FROM health_profiles WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: Long): HealthProfile?
}

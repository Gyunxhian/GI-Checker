package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.FoodRecordStatus
import kotlinx.coroutines.flow.Flow

/**
 * 档案数据访问接口。
 */
@Dao
interface FoodRecordDao {
    /**
     * 新增档案记录。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FoodRecord): Long

    /**
     * 更新档案记录。
     */
    @Update
    suspend fun update(record: FoodRecord): Int

    /**
     * 删除档案记录。
     */
    @Delete
    suspend fun delete(record: FoodRecord): Int

    /**
     * 查询档案详情。
     */
    @Query("SELECT * FROM food_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FoodRecord?

    /**
     * 观察全部档案。
     */
    @Query("SELECT * FROM food_records ORDER BY record_date DESC")
    fun observeAll(): Flow<List<FoodRecord>>

    /**
     * 观察指定状态档案。
     */
    @Query("SELECT * FROM food_records WHERE status = :status ORDER BY record_date DESC")
    fun observeByStatus(status: FoodRecordStatus): Flow<List<FoodRecord>>
}

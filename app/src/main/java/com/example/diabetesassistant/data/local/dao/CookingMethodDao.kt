package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diabetesassistant.data.local.entity.CookingMethodTable
import kotlinx.coroutines.flow.Flow

/**
 * 烹饪方式修正表数据访问接口。
 */
@Dao
interface CookingMethodDao {
    /**
     * 批量插入烹饪修正值。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CookingMethodTable>): List<Long>

    /**
     * 查询指定烹饪方式修正值。
     */
    @Query("SELECT * FROM cooking_method_table WHERE cooking_method = :method LIMIT 1")
    suspend fun getByMethod(method: String): CookingMethodTable?

    /**
     * 观察烹饪方式表。
     */
    @Query("SELECT * FROM cooking_method_table ORDER BY correction_value ASC, cooking_method ASC")
    fun observeAll(): Flow<List<CookingMethodTable>>
}

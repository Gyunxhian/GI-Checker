package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diabetesassistant.data.local.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问接口。
 */
@Dao
interface UserDao {
    /**
     * 插入用户。
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    /**
     * 更新用户。
     */
    @Update
    suspend fun update(user: User): Int

    /**
     * 按主键查询用户。
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): User?

    /**
     * 通过手机号或邮箱查找登录用户。
     */
    @Query(
        """
        SELECT * FROM users
        WHERE (phone = :account OR email = :account) AND password = :password
        LIMIT 1
        """,
    )
    suspend fun login(account: String, password: String): User?

    /**
     * 观察所有用户。
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun observeAll(): Flow<List<User>>
}

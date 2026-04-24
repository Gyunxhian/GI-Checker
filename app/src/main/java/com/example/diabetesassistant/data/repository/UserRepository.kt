package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.local.dao.HealthProfileDao
import com.example.diabetesassistant.data.local.dao.UserDao
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.example.diabetesassistant.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户仓库接口。
 */
interface UserRepository {
    /**
     * 注册用户。
     */
    suspend fun register(user: User): Long

    /**
     * 登录。
     */
    suspend fun login(account: String, password: String): User?

    /**
     * 读取用户信息。
     */
    suspend fun getUserById(userId: Long): User?

    /**
     * 观察全部用户。
     */
    fun observeUsers(): Flow<List<User>>

    /**
     * 保存健康档案。
     */
    suspend fun saveHealthProfile(profile: HealthProfile): Long

    /**
     * 读取健康档案。
     */
    suspend fun getHealthProfile(userId: Long): HealthProfile?

    /**
     * 观察健康档案。
     */
    fun observeHealthProfile(userId: Long): Flow<HealthProfile?>
}

/**
 * 用户仓库实现。
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val healthProfileDao: HealthProfileDao,
) : UserRepository {
    /**
     * @see UserRepository.register
     */
    override suspend fun register(user: User): Long = userDao.insert(user)

    /**
     * @see UserRepository.login
     */
    override suspend fun login(account: String, password: String): User? = userDao.login(account, password)

    /**
     * @see UserRepository.getUserById
     */
    override suspend fun getUserById(userId: Long): User? = userDao.getById(userId)

    /**
     * @see UserRepository.observeUsers
     */
    override fun observeUsers(): Flow<List<User>> = userDao.observeAll()

    /**
     * @see UserRepository.saveHealthProfile
     */
    override suspend fun saveHealthProfile(profile: HealthProfile): Long = healthProfileDao.insert(profile)

    /**
     * @see UserRepository.getHealthProfile
     */
    override suspend fun getHealthProfile(userId: Long): HealthProfile? = healthProfileDao.getByUserId(userId)

    /**
     * @see UserRepository.observeHealthProfile
     */
    override fun observeHealthProfile(userId: Long): Flow<HealthProfile?> = healthProfileDao.observeByUserId(userId)
}

package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.local.dao.FoodRecordDao
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.FoodRecordStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 档案仓库接口。
 */
interface FoodRecordRepository {
    /**
     * 新增档案。
     */
    suspend fun create(record: FoodRecord): Long

    /**
     * 更新档案。
     */
    suspend fun update(record: FoodRecord)

    /**
     * 删除档案。
     */
    suspend fun delete(record: FoodRecord)

    /**
     * 查询档案详情。
     */
    suspend fun getById(id: Long): FoodRecord?

    /**
     * 观察全部档案。
     */
    fun observeAll(): Flow<List<FoodRecord>>

    /**
     * 观察状态档案。
     */
    fun observeByStatus(status: FoodRecordStatus): Flow<List<FoodRecord>>
}

/**
 * 档案仓库实现。
 */
@Singleton
class FoodRecordRepositoryImpl @Inject constructor(
    private val foodRecordDao: FoodRecordDao,
) : FoodRecordRepository {
    /**
     * @see FoodRecordRepository.create
     */
    override suspend fun create(record: FoodRecord): Long = foodRecordDao.insert(record)

    /**
     * @see FoodRecordRepository.update
     */
    override suspend fun update(record: FoodRecord) {
        foodRecordDao.update(record)
    }

    /**
     * @see FoodRecordRepository.delete
     */
    override suspend fun delete(record: FoodRecord) {
        foodRecordDao.delete(record)
    }

    /**
     * @see FoodRecordRepository.getById
     */
    override suspend fun getById(id: Long): FoodRecord? = foodRecordDao.getById(id)

    /**
     * @see FoodRecordRepository.observeAll
     */
    override fun observeAll(): Flow<List<FoodRecord>> = foodRecordDao.observeAll()

    /**
     * @see FoodRecordRepository.observeByStatus
     */
    override fun observeByStatus(status: FoodRecordStatus): Flow<List<FoodRecord>> =
        foodRecordDao.observeByStatus(status)
}

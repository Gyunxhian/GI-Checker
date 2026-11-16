package com.example.diabetesassistant.data.local

import android.content.Context
import android.content.ContextWrapper
import com.example.diabetesassistant.camera.CameraRecognitionViewModel
import com.example.diabetesassistant.data.local.dao.CookingMethodDao
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.FoodRecordStatus
import com.example.diabetesassistant.data.repository.AiRepository
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.data.repository.NutritionRepository
import com.example.diabetesassistant.domain.algorithm.BloodSugarPredictor
import com.example.diabetesassistant.domain.algorithm.CarbAndGiCalculator
import com.example.diabetesassistant.domain.algorithm.FocusPeriodAnalyzer
import com.example.diabetesassistant.domain.algorithm.InsulinBandCalculator
import com.example.diabetesassistant.domain.usecase.AnalyzeFoodUseCase
import com.example.diabetesassistant.ui.screens.CarbTimeSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class FoodRecordTimestampTest {

    private lateinit var fakeRepository: FakeFoodRecordRepository
    private lateinit var viewModel: CameraRecognitionViewModel

    private inline fun <reified T : Any> createDummyProxy(): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, _, _ -> null } as T
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        fakeRepository = FakeFoodRecordRepository()

        val mockContext = object : ContextWrapper(null) {
            override fun getApplicationContext(): Context = this
        }

        val dummyNutritionRepo: NutritionRepository = createDummyProxy()
        val dummyAiRepo: AiRepository = createDummyProxy()
        val dummyCookingDao: CookingMethodDao = createDummyProxy()

        val carbCalc = CarbAndGiCalculator()
        val bsPredictor = BloodSugarPredictor(carbCalc)
        val insulinBandCalc = InsulinBandCalculator()
        val focusAnalyzer = FocusPeriodAnalyzer()

        val analyzeUseCase = AnalyzeFoodUseCase(
            nutritionRepository = dummyNutritionRepo,
            aiRepository = dummyAiRepo,
            cookingMethodDao = dummyCookingDao,
            carbAndGiCalculator = carbCalc,
            bloodSugarPredictor = bsPredictor,
            insulinBandCalculator = insulinBandCalc,
            focusPeriodAnalyzer = focusAnalyzer,
        )

        viewModel = CameraRecognitionViewModel(
            analyzeFoodUseCase = analyzeUseCase,
            foodRecordRepository = fakeRepository,
            context = mockContext,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==========================================
    // 1. Timestamp Immutability Tests
    // ==========================================

    @Test
    fun timestampImmutability_whenUpdatingRecord_preservesOriginalRecordDate() = runTest {
        // 创建历史时间戳（例如 3 天前的记录）
        val threeDaysAgoTimestamp = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        val originalRecord = FoodRecord(
            id = 1001L,
            imagePath = "content://media/external/images/media/1001",
            recordDate = threeDaysAgoTimestamp,
            dishName = "清炒西蓝花",
            aiJson = "{\"totalCarbIntake\":35.0,\"weightedGi\":45.0,\"meatRatio\":0.2}",
            status = FoodRecordStatus.COMPLETED,
        )
        fakeRepository.records[1001L] = originalRecord

        // 详情页加载历史记录
        viewModel.loadRecord(1001L)
        advanceUntilIdle()

        // 验证已正确加载历史时间戳到 UIState 中
        assertEquals(threeDaysAgoTimestamp, viewModel.uiState.value.recordDate)
        assertEquals(1001L, viewModel.uiState.value.savedFoodRecordId)

        // 模拟用户在详情页微调后点击保存重新固化
        viewModel.saveOrUpdateRecord()
        advanceUntilIdle()

        // 断言：数据库中被更新的记录时间戳必须与最初的历史时间戳完全一致，绝未被覆写为当前时间
        val updatedRecord = fakeRepository.lastUpdatedRecord
        assertNotNull("更新后的记录不得为空", updatedRecord)
        assertEquals("更新记录必须保留最初保存的时间戳", threeDaysAgoTimestamp, updatedRecord!!.recordDate)
        assertEquals(threeDaysAgoTimestamp, viewModel.uiState.value.recordDate)
        assertNotEquals("更新时间戳绝不能等于当前时间", System.currentTimeMillis(), updatedRecord.recordDate)
    }

    @Test
    fun timestampImmutability_whenCreatingNewRecord_setsInitialTimestamp() = runTest {
        val testUri = "content://media/external/images/media/2001"
        viewModel.loadRecord(9999L) // Non-existent, defaults to clean state

        val beforeCreate = System.currentTimeMillis()
        val newRecord = FoodRecord(
            id = 0L,
            imagePath = testUri,
            recordDate = beforeCreate,
            dishName = "番茄炒蛋",
            aiJson = "{\"totalCarbIntake\":20.0}",
            status = FoodRecordStatus.COMPLETED,
        )
        val createdId = fakeRepository.create(newRecord)
        val saved = fakeRepository.getById(createdId)

        assertNotNull(saved)
        assertTrue(saved!!.recordDate >= beforeCreate)
    }

    // ==========================================
    // 2. Time Slot Binning Tests (0~10, 11~17, 18~23)
    // ==========================================

    @Test
    fun timeSlotBinning_morningHours_correctlyClassified() {
        // 上午时段：0点 ~ 10点
        for (h in 0..10) {
            val slot = CarbTimeSlot.fromHour(h)
            assertEquals("Hour $h should be MORNING", CarbTimeSlot.MORNING, slot)
            assertEquals("上午", slot.label)
        }
    }

    @Test
    fun timeSlotBinning_afternoonHours_correctlyClassified() {
        // 下午时段：11点 ~ 17点
        for (h in 11..17) {
            val slot = CarbTimeSlot.fromHour(h)
            assertEquals("Hour $h should be AFTERNOON", CarbTimeSlot.AFTERNOON, slot)
            assertEquals("下午", slot.label)
        }
    }

    @Test
    fun timeSlotBinning_eveningHours_correctlyClassified() {
        // 晚上时段：18点 ~ 23点
        for (h in 18..23) {
            val slot = CarbTimeSlot.fromHour(h)
            assertEquals("Hour $h should be EVENING", CarbTimeSlot.EVENING, slot)
            assertEquals("晚上", slot.label)
        }
    }

    @Test
    fun timeSlotBinning_epochTimestampToLocalDateTime_correctlyClassified() {
        val zone = ZoneId.systemDefault()
        val morningInstant = java.time.LocalDate.now().atTime(8, 30).atZone(zone).toInstant().toEpochMilli()
        val afternoonInstant = java.time.LocalDate.now().atTime(13, 15).atZone(zone).toInstant().toEpochMilli()
        val eveningInstant = java.time.LocalDate.now().atTime(19, 45).atZone(zone).toInstant().toEpochMilli()

        val morningHour = Instant.ofEpochMilli(morningInstant).atZone(zone).hour
        val afternoonHour = Instant.ofEpochMilli(afternoonInstant).atZone(zone).hour
        val eveningHour = Instant.ofEpochMilli(eveningInstant).atZone(zone).hour

        assertEquals(CarbTimeSlot.MORNING, CarbTimeSlot.fromHour(morningHour))
        assertEquals(CarbTimeSlot.AFTERNOON, CarbTimeSlot.fromHour(afternoonHour))
        assertEquals(CarbTimeSlot.EVENING, CarbTimeSlot.fromHour(eveningHour))
    }

    // ==========================================
    // 3. Meat vs. Veg Carbs Calculation Tests
    // ==========================================

    @Test
    fun meatVsVegCarbs_balancedRatio_correctlyCalculated() {
        val totalCarbs = 60.0f
        val meatRatio = 0.5f

        val meatCarbs = totalCarbs * meatRatio.coerceIn(0f, 1f)
        val vegCarbs = totalCarbs * (1f - meatRatio.coerceIn(0f, 1f))

        assertEquals(30.0f, meatCarbs, 0.001f)
        assertEquals(30.0f, vegCarbs, 0.001f)
        assertEquals(totalCarbs, meatCarbs + vegCarbs, 0.001f)
    }

    @Test
    fun meatVsVegCarbs_pureVegetarian_correctlyCalculated() {
        val totalCarbs = 75.0f
        val meatRatio = 0.0f

        val meatCarbs = totalCarbs * meatRatio.coerceIn(0f, 1f)
        val vegCarbs = totalCarbs * (1f - meatRatio.coerceIn(0f, 1f))

        assertEquals(0.0f, meatCarbs, 0.001f)
        assertEquals(75.0f, vegCarbs, 0.001f)
        assertEquals(totalCarbs, meatCarbs + vegCarbs, 0.001f)
    }

    @Test
    fun meatVsVegCarbs_pureMeat_correctlyCalculated() {
        val totalCarbs = 40.0f
        val meatRatio = 1.0f

        val meatCarbs = totalCarbs * meatRatio.coerceIn(0f, 1f)
        val vegCarbs = totalCarbs * (1f - meatRatio.coerceIn(0f, 1f))

        assertEquals(40.0f, meatCarbs, 0.001f)
        assertEquals(0.0f, vegCarbs, 0.001f)
        assertEquals(totalCarbs, meatCarbs + vegCarbs, 0.001f)
    }

    @Test
    fun meatVsVegCarbs_customRatio_correctlyCalculated() {
        val totalCarbs = 80.0f
        val meatRatio = 0.25f

        val meatCarbs = totalCarbs * meatRatio.coerceIn(0f, 1f)
        val vegCarbs = totalCarbs * (1f - meatRatio.coerceIn(0f, 1f))

        assertEquals(20.0f, meatCarbs, 0.001f)
        assertEquals(60.0f, vegCarbs, 0.001f)
        assertEquals(totalCarbs, meatCarbs + vegCarbs, 0.001f)
    }

    @Test
    fun meatVsVegCarbs_boundaryCoerce_preventsNegativeOrOverflow() {
        val totalCarbs = 50.0f
        val negativeRatio = -0.5f
        val overflowRatio = 1.8f

        val safeNegativeMeat = totalCarbs * negativeRatio.coerceIn(0f, 1f)
        val safeNegativeVeg = totalCarbs * (1f - negativeRatio.coerceIn(0f, 1f))
        assertEquals(0.0f, safeNegativeMeat, 0.001f)
        assertEquals(50.0f, safeNegativeVeg, 0.001f)

        val safeOverflowMeat = totalCarbs * overflowRatio.coerceIn(0f, 1f)
        val safeOverflowVeg = totalCarbs * (1f - overflowRatio.coerceIn(0f, 1f))
        assertEquals(50.0f, safeOverflowMeat, 0.001f)
        assertEquals(0.0f, safeOverflowVeg, 0.001f)
    }
}

class FakeFoodRecordRepository : FoodRecordRepository {
    val records = mutableMapOf<Long, FoodRecord>()
    var lastUpdatedRecord: FoodRecord? = null
    var lastCreatedRecord: FoodRecord? = null

    override suspend fun create(record: FoodRecord): Long {
        val id = if (record.id != 0L) record.id else (records.size + 1).toLong()
        val saved = record.copy(id = id)
        records[id] = saved
        lastCreatedRecord = saved
        return id
    }

    override suspend fun update(record: FoodRecord) {
        records[record.id] = record
        lastUpdatedRecord = record
    }

    override suspend fun delete(record: FoodRecord) {
        records.remove(record.id)
    }

    override suspend fun deleteAll() {
        records.clear()
    }

    override suspend fun getById(id: Long): FoodRecord? = records[id]

    override fun observeAll(): Flow<List<FoodRecord>> = flowOf(records.values.toList())

    override fun observeByStatus(status: FoodRecordStatus): Flow<List<FoodRecord>> =
        flowOf(records.values.filter { it.status == status })
}

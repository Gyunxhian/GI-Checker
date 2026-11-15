package com.example.diabetesassistant.domain.privacy

import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.example.diabetesassistant.domain.search.MedicalSearchProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyDataMaskerTest {

    private val masker = PrivacyDataMasker()
    private val searchProvider = MedicalSearchProvider()

    @Test
    fun testMaskToPrivateContext_stripsPersonalIdentity() {
        val healthProfile = HealthProfile(
            id = 999L,
            userId = 888L,
            diabetesType = "2型糖尿病",
            insulinType = "速效餐时胰岛素",
            insulinMin = 4f,
            insulinMax = 12f,
            weightKg = 70f,
            heightCm = 175f,
        )

        val foodRecord = FoodRecord(
            id = 101L,
            imagePath = "/data/user/0/img.jpg",
            recordDate = System.currentTimeMillis(),
            dishName = "全麦低糖意面",
            aiJson = """{"totalCarbIntake":42.5,"weightedGi":48.0,"curvePoints":[{"minute":30,"bloodSugar":6.5},{"minute":60,"bloodSugar":7.2}]}""",
        )

        val masked = masker.maskToPrivateContext(
            healthProfile = healthProfile,
            dailyCarbTarget = 120f,
            todayCarbs = 42.5f,
            recentRecords = listOf(foodRecord),
        )

        // 必须抹除用户 ID
        assertFalse("不应包含任何用户内部数字 ID", masked.contains("888"))
        assertFalse("不应包含健康档案主键 ID", masked.contains("999"))
        assertFalse("不应包含文件内部物理路径", masked.contains("/data/user/0"))

        // 必须保留医学上下文
        assertTrue("应包含糖尿病分型", masked.contains("2型糖尿病"))
        assertTrue("应包含用药方案", masked.contains("速效餐时胰岛素"))
        assertTrue("应包含菜品名称", masked.contains("全麦低糖意面"))
        assertTrue("应包含碳水指标", masked.contains("42.5"))
    }

    @Test
    fun testMedicalSearchProvider_localClinicalGuidelines() = runBlocking {
        // 测试疑似低血糖症状
        val results1 = searchProvider.searchMedicalEvidence("餐后突然心慌手抖出汗")
        assertTrue("针对低血糖应返回指南指引", results1.isNotEmpty())
        val match1 = results1.firstOrNull { it.snippet.contains("双十五") }
        assertTrue("应包含双十五紧急处置原则", match1 != null)

        // 测试暴饮暴食超标
        val results2 = searchProvider.searchMedicalEvidence("这顿饭碳水严重超标吃多了")
        assertTrue("针对超标应返回餐后快走与运动阻断法", results2.any { it.snippet.contains("阻断") || it.snippet.contains("运动") })
    }
}

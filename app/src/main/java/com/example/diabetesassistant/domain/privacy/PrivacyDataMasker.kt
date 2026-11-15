package com.example.diabetesassistant.domain.privacy

import com.example.diabetesassistant.camera.AnalysisResultJson
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.HealthProfile
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地健康隐私数据脱敏器。
 * 严格抹除一切用户名、用户ID、头像、联系方式等个人识别特征，
 * 仅提炼用于临床代谢评估的匿名生理学特征与近期餐食碳水走势。
 */
@Singleton
class PrivacyDataMasker @Inject constructor() {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun maskToPrivateContext(
        healthProfile: HealthProfile?,
        dailyCarbTarget: Float,
        todayCarbs: Float,
        recentRecords: List<FoodRecord>,
    ): String {
        val sb = StringBuilder()
        sb.append("【本地匿名代谢与健康档案（已脱敏，仅供临床个性化参考）】\n")

        // 1. 匿名基础生理指标
        val diabetesType = healthProfile?.diabetesType?.ifBlank { "未指定" } ?: "2型糖尿病"
        val insulinType = healthProfile?.insulinType?.ifBlank { "无" } ?: "无"
        sb.append("- 糖尿病分型：").append(diabetesType).append("\n")
        sb.append("- 胰岛素/药物方案：").append(insulinType).append("\n")

        val weight = healthProfile?.weightKg
        val height = healthProfile?.heightCm
        if (weight != null && height != null && height > 0f) {
            val bmi = weight / ((height / 100f) * (height / 100f))
            sb.append(String.format(Locale.getDefault(), "- 体征概况：身高 %.0fcm, 体重 %.1fkg (BMI: %.1f)\n", height, weight, bmi))
        }

        sb.append(String.format(Locale.getDefault(), "- 每日碳水上限预算：%.0fg\n", dailyCarbTarget))
        sb.append(String.format(Locale.getDefault(), "- 今日已累计摄入碳水：%.1fg (剩余预算: %.1fg)\n", todayCarbs, (dailyCarbTarget - todayCarbs).coerceAtLeast(0f)))

        // 2. 近期餐食记录分析（至多取最近5条）
        if (recentRecords.isNotEmpty()) {
            sb.append("- 近期代谢餐食与波动预测：\n")
            recentRecords.take(5).forEachIndexed { idx, rec ->
                val timeStr = dateFormat.format(Date(rec.recordDate))
                var carbInfo = ""
                var peakInfo = ""
                try {
                    val parsed = gson.fromJson(rec.aiJson, AnalysisResultJson::class.java)
                    if (parsed != null) {
                        carbInfo = String.format(Locale.getDefault(), "碳水%.1fg", parsed.totalCarbIntake)
                        val peak = parsed.curvePoints?.maxByOrNull { it.bloodSugar }
                        if (peak != null) {
                            peakInfo = String.format(Locale.getDefault(), "预测峰值%.1f mmol/L(%d分)", peak.bloodSugar, peak.minute)
                        }
                    }
                } catch (_: Exception) {}

                sb.append("  ${idx + 1}. [$timeStr] ${rec.dishName.ifBlank { "未知餐食" }}")
                if (carbInfo.isNotBlank()) sb.append(" | ").append(carbInfo)
                if (peakInfo.isNotBlank()) sb.append(" | ").append(peakInfo)
                sb.append("\n")
            }
        } else {
            sb.append("- 近期暂无餐饮记录。\n")
        }

        return sb.toString().trim()
    }
}

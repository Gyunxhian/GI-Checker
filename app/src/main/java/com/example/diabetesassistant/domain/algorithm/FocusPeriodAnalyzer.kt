package com.example.diabetesassistant.domain.algorithm

import javax.inject.Inject
import kotlin.math.round

/**
 * 重点关注时段分析器。
 */
class FocusPeriodAnalyzer @Inject constructor() {
    /**
     * Step 7：分析血糖曲线并返回重点关注时段。
     */
    fun analyze(points: List<BloodSugarPoint>): List<FocusPeriod> {
        if (points.isEmpty()) return emptyList()

        val highs = extractIntervals(
            points = points,
            level = FocusLevel.HIGH,
            predicate = { it.bloodSugar >= AlgorithmConstants.HIGH_BLOOD_SUGAR_THRESHOLD },
        )
        val lows = extractIntervals(
            points = points,
            level = FocusLevel.LOW,
            predicate = { it.bloodSugar <= AlgorithmConstants.LOW_BLOOD_SUGAR_THRESHOLD },
        )
        val elevated = extractIntervals(
            points = points,
            level = FocusLevel.ELEVATED,
            predicate = {
                it.bloodSugar >= AlgorithmConstants.ELEVATED_BLOOD_SUGAR_THRESHOLD &&
                    it.bloodSugar < AlgorithmConstants.HIGH_BLOOD_SUGAR_THRESHOLD
            },
        )

        return (highs + lows + elevated)
            .sortedWith(compareBy<FocusPeriod> { levelRank(it.level) }.thenByDescending { severityDistance(it) })
    }

    /**
     * 生成提醒 Prompt。
     */
    fun buildReminderPrompt(periods: List<FocusPeriod>): String {
        val lines = periods.joinToString("\n") {
            "- 餐后${it.timeLabel}血糖约${formatOneDecimal(it.bloodSugar)}mmol/L"
        }
        return "根据以下血糖预测，为糖尿病患者生成温馨提醒。语气温和，每条≤30字，不是医疗建议:\n$lines"
    }

    private fun extractIntervals(
        points: List<BloodSugarPoint>,
        level: FocusLevel,
        predicate: (BloodSugarPoint) -> Boolean,
    ): List<FocusPeriod> {
        val periods = mutableListOf<FocusPeriod>()
        var index = 0
        while (index < points.size) {
            if (!predicate(points[index])) {
                index++
                continue
            }
            val start = index
            while (index + 1 < points.size && predicate(points[index + 1])) {
                index++
            }
            val end = index
            val window = points.subList(start, end + 1)
            val keyPoint = when (level) {
                FocusLevel.LOW -> window.minByOrNull { it.bloodSugar }
                FocusLevel.HIGH, FocusLevel.ELEVATED -> window.maxByOrNull { it.bloodSugar }
            } ?: points[start]
            val hours = round(keyPoint.minute / 60f * 10f) / 10f
            periods += FocusPeriod(
                timeLabel = "${formatHour(hours)}小时后",
                hourAfterMeal = hours,
                bloodSugar = keyPoint.bloodSugar,
                level = level,
            )
            index++
        }
        return periods
    }

    private fun formatHour(hours: Float): String {
        val rounded = formatOneDecimal(hours)
        return if (rounded.endsWith(".0")) rounded.dropLast(2) else rounded
    }

    private fun formatOneDecimal(value: Float): String {
        return String.format(java.util.Locale.US, "%.1f", value)
    }

    private fun levelRank(level: FocusLevel): Int {
        return when (level) {
            FocusLevel.HIGH -> 0
            FocusLevel.LOW -> 1
            FocusLevel.ELEVATED -> 2
        }
    }

    private fun severityDistance(period: FocusPeriod): Float {
        return when (period.level) {
            FocusLevel.HIGH -> period.bloodSugar - AlgorithmConstants.HIGH_BLOOD_SUGAR_THRESHOLD
            FocusLevel.LOW -> AlgorithmConstants.LOW_BLOOD_SUGAR_THRESHOLD - period.bloodSugar
            FocusLevel.ELEVATED -> period.bloodSugar - AlgorithmConstants.ELEVATED_BLOOD_SUGAR_THRESHOLD
        }
    }
}
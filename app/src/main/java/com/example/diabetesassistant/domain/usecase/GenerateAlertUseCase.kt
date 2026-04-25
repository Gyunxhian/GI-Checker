package com.example.diabetesassistant.domain.usecase

import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.repository.AiRepository
import com.example.diabetesassistant.domain.algorithm.FocusPeriod
import com.example.diabetesassistant.domain.algorithm.FocusPeriodAnalyzer
import javax.inject.Inject

/**
 * Step 7 温馨提醒生成用例。
 */
class GenerateAlertUseCase @Inject constructor(
    private val focusPeriodAnalyzer: FocusPeriodAnalyzer,
    private val aiRepository: AiRepository,
) {
    /**
     * 根据重点时段生成提醒文案。
     */
    suspend operator fun invoke(periods: List<FocusPeriod>): NetworkResult<List<String>> {
        if (periods.isEmpty()) return NetworkResult.Success(emptyList())
        return when (val response = aiRepository.generateReminderText(focusPeriodAnalyzer.buildReminderPrompt(periods))) {
            is NetworkResult.Success -> {
                val reminders = response.data
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()
                NetworkResult.Success(reminders)
            }
            is NetworkResult.Error -> response
        }
    }
}
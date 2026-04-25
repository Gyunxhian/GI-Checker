package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.remote.ApiKeyProvider
import com.example.diabetesassistant.data.remote.DashScopeApi
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.remote.dto.DashScopeContent
import com.example.diabetesassistant.data.remote.dto.DashScopeInput
import com.example.diabetesassistant.data.remote.dto.DashScopeMessage
import com.example.diabetesassistant.data.remote.dto.DashScopeRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

/**
 * AI 仓库接口。
 */
interface AiRepository {
    /**
     * 调用 Qwen VL 识别菜品，返回 JSON 文本。
     */
    suspend fun recognizeDishJson(imageUrl: String, prompt: String): NetworkResult<String>

    /**
     * 调用 Qwen 生成文本提醒。
     */
    suspend fun generateReminderText(prompt: String): NetworkResult<String>
}

/**
 * AI 仓库实现。
 */
@Singleton
class AiRepositoryImpl @Inject constructor(
    private val dashScopeApi: DashScopeApi,
    private val apiKeyProvider: ApiKeyProvider,
) : AiRepository {
    /**
     * @see AiRepository.recognizeDishJson
     */
    override suspend fun recognizeDishJson(imageUrl: String, prompt: String): NetworkResult<String> {
        return try {
            val request = DashScopeRequest(
                input = DashScopeInput(
                    messages = listOf(
                        DashScopeMessage(
                            role = "user",
                            content = listOf(
                                DashScopeContent(text = prompt),
                                DashScopeContent(image = imageUrl),
                            ),
                        ),
                    ),
                ),
            )

            val response = dashScopeApi.recognizeDish(
                authorization = "Bearer ${apiKeyProvider.dashScopeApiKey()}",
                request = request,
            )

            val text = response.output
                ?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.firstOrNull()
                ?.text

            if (text.isNullOrBlank()) {
                NetworkResult.Error(message = "DashScope returned empty content.")
            } else {
                NetworkResult.Success(text)
            }
        } catch (exception: HttpException) {
            NetworkResult.Error(
                code = exception.code(),
                message = "DashScope request failed: HTTP ${exception.code()}",
                throwable = exception,
            )
        } catch (exception: IOException) {
            NetworkResult.Error(message = "DashScope network error.", throwable = exception)
        } catch (exception: Exception) {
            NetworkResult.Error(message = "DashScope unknown error.", throwable = exception)
        }
    }

    /**
     * @see AiRepository.generateReminderText
     */
    override suspend fun generateReminderText(prompt: String): NetworkResult<String> {
        return try {
            val request = DashScopeRequest(
                input = DashScopeInput(
                    messages = listOf(
                        DashScopeMessage(
                            role = "user",
                            content = listOf(DashScopeContent(text = prompt)),
                        ),
                    ),
                ),
            )

            val response = dashScopeApi.recognizeDish(
                authorization = "Bearer ${apiKeyProvider.dashScopeApiKey()}",
                request = request,
            )

            val text = response.output
                ?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.firstOrNull()
                ?.text

            if (text.isNullOrBlank()) {
                NetworkResult.Error(message = "DashScope returned empty reminder content.")
            } else {
                NetworkResult.Success(text)
            }
        } catch (exception: HttpException) {
            NetworkResult.Error(
                code = exception.code(),
                message = "DashScope reminder request failed: HTTP ${exception.code()}",
                throwable = exception,
            )
        } catch (exception: IOException) {
            NetworkResult.Error(message = "DashScope reminder network error.", throwable = exception)
        } catch (exception: Exception) {
            NetworkResult.Error(message = "DashScope reminder unknown error.", throwable = exception)
        }
    }
}

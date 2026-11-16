package com.example.diabetesassistant.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.diabetesassistant.data.remote.ApiKeyProvider
import com.example.diabetesassistant.data.remote.DeepSeekApi
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.remote.dto.DeepSeekContentPart
import com.example.diabetesassistant.data.remote.dto.DeepSeekImageUrl
import com.example.diabetesassistant.data.remote.dto.DeepSeekMessage
import com.example.diabetesassistant.data.remote.dto.DeepSeekRequest
import com.example.diabetesassistant.data.remote.dto.DeepSeekStreamChunk
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

data class AiStreamChunk(
    val contentDelta: String = "",
    val reasoningDelta: String = "",
    val isDone: Boolean = false,
    val errorMessage: String? = null,
)

interface AiRepository {
    suspend fun recognizeDishJson(imageUrl: String, prompt: String): NetworkResult<String>
    suspend fun recognizeDishesJson(imageUrls: List<String>, prompt: String): NetworkResult<String>
    suspend fun generateReminderText(prompt: String): NetworkResult<String>
    suspend fun askHealthAssistant(
        userQuestion: String,
        privateContext: String,
        searchEvidences: List<com.example.diabetesassistant.domain.search.SearchEvidence>,
    ): NetworkResult<String>
    fun askHealthAssistantStream(
        userQuestion: String,
        privateContext: String,
        searchEvidences: List<com.example.diabetesassistant.domain.search.SearchEvidence>,
        enableThinking: Boolean = false,
    ): Flow<AiStreamChunk>
}

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val apiKeyProvider: ApiKeyProvider,
    @ApplicationContext private val context: Context,
) : AiRepository {

    private val gson = Gson()

    override suspend fun recognizeDishJson(imageUrl: String, prompt: String): NetworkResult<String> {
        return recognizeDishesJson(listOf(imageUrl), prompt)
    }

    override suspend fun recognizeDishesJson(imageUrls: List<String>, prompt: String): NetworkResult<String> {
        Log.i(TAG, "===== DeepSeek-flash 图像识别开始 (${imageUrls.size} 张图片) =====")
        return try {
            val contentParts = mutableListOf<DeepSeekContentPart>()
            contentParts.add(DeepSeekContentPart(type = "text", text = prompt))

            imageUrls.forEachIndexed { index, url ->
                val base64Image = uriToBase64(url)
                Log.d(TAG, "图片[$index] Base64编码完成, 长度: ${base64Image.length} 字符")
                contentParts.add(
                    DeepSeekContentPart(
                        type = "image_url",
                        imageUrl = DeepSeekImageUrl(url = "data:image/jpeg;base64,$base64Image"),
                    )
                )
            }

            val request = DeepSeekRequest(
                model = "deepseek-flash",
                messages = listOf(
                    DeepSeekMessage(role = "user", content = contentParts),
                ),
                maxTokens = 4096,
                temperature = 0.1f,
            )
            Log.d(TAG, "发送DeepSeek-flash视觉识别请求 (包含 ${imageUrls.size} 张图片)...")

            val response = deepSeekApi.chat(
                authorization = "Bearer ${apiKeyProvider.deepSeekApiKey()}",
                request = request,
            )
            Log.d(TAG, "DeepSeek响应收到 — choices数量: ${response.choices.size}")

            val choiceMsg = response.choices.firstOrNull()?.message
            val contentText = choiceMsg?.content?.trim()
            val reasoningText = choiceMsg?.reasoningContent?.trim()

            val rawText = if (!contentText.isNullOrBlank()) {
                contentText
            } else if (!reasoningText.isNullOrBlank()) {
                Log.w(TAG, "DeepSeek message.content 为空，尝试从 reasoning_content 提取合法 JSON")
                reasoningText
            } else {
                ""
            }

            if (rawText.isBlank()) {
                Log.w(TAG, "DeepSeek返回空内容 (content与reasoning_content均为空)")
                NetworkResult.Error(message = "DeepSeek returned empty content.")
            } else {
                val extracted = extractJson(rawText)
                if (extracted.startsWith("{") && extracted.endsWith("}")) {
                    Log.d(TAG, "DeepSeek识别成功 — 原始文本长度: ${rawText.length}, 提取JSON长度: ${extracted.length}")
                    Log.d(TAG, "识别JSON: $extracted")
                    NetworkResult.Success(extracted)
                } else {
                    Log.e(TAG, "未能从响应中提取合法JSON结构: $rawText")
                    NetworkResult.Error(message = "Failed to extract valid JSON from DeepSeek response.")
                }
            }
        } catch (exception: HttpException) {
            val errorBody = exception.response()?.errorBody()?.string() ?: ""
            Log.e(TAG, "DeepSeek HTTP错误: ${exception.code()} ${exception.message()} | body: $errorBody", exception)
            NetworkResult.Error(
                code = exception.code(),
                message = "DeepSeek request failed: HTTP ${exception.code()} $errorBody",
                throwable = exception,
            )
        } catch (exception: IOException) {
            Log.e(TAG, "DeepSeek 网络错误: ${exception.message}", exception)
            NetworkResult.Error(message = "DeepSeek network error: ${exception.message}", throwable = exception)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Log.e(TAG, "DeepSeek 未知错误: ${exception.message}", exception)
            NetworkResult.Error(message = "DeepSeek unknown error: ${exception.message}", throwable = exception)
        }
    }

    override suspend fun generateReminderText(prompt: String): NetworkResult<String> {
        Log.i(TAG, "===== DeepSeek-flash 提醒文案生成 =====")
        return try {
            val messages = listOf(
                DeepSeekMessage(role = "user", content = prompt),
            )
            val request = DeepSeekRequest(
                model = "deepseek-flash",
                messages = messages,
                maxTokens = 4096,
            )
            Log.d(TAG, "发送DeepSeek提醒请求 — prompt长度: ${prompt.length}")

            val response = deepSeekApi.chat(
                authorization = "Bearer ${apiKeyProvider.deepSeekApiKey()}",
                request = request,
            )

            val choiceMsg = response.choices.firstOrNull()?.message
            val text = choiceMsg?.content?.takeIf { it.isNotBlank() }
                ?: choiceMsg?.reasoningContent?.takeIf { it.isNotBlank() }

            if (text.isNullOrBlank()) {
                Log.w(TAG, "DeepSeek返回空提醒内容")
                NetworkResult.Error(message = "DeepSeek returned empty reminder content.")
            } else {
                Log.i(TAG, "DeepSeek提醒生成成功: $text")
                NetworkResult.Success(text)
            }
        } catch (exception: HttpException) {
            val errorBody = exception.response()?.errorBody()?.string() ?: ""
            Log.e(TAG, "DeepSeek提醒 HTTP错误: ${exception.code()} | body: $errorBody", exception)
            NetworkResult.Error(
                code = exception.code(),
                message = "DeepSeek reminder request failed: HTTP ${exception.code()} $errorBody",
                throwable = exception,
            )
        } catch (exception: IOException) {
            Log.e(TAG, "DeepSeek提醒 网络错误: ${exception.message}", exception)
            NetworkResult.Error(message = "DeepSeek reminder network error.", throwable = exception)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Log.e(TAG, "DeepSeek提醒 未知错误: ${exception.message}", exception)
            NetworkResult.Error(message = "DeepSeek reminder unknown error.", throwable = exception)
        }
    }

    override suspend fun askHealthAssistant(
        userQuestion: String,
        privateContext: String,
        searchEvidences: List<com.example.diabetesassistant.domain.search.SearchEvidence>,
    ): NetworkResult<String> {
        Log.i(TAG, "===== DeepSeek-chat AI健康助手问答 =====")
        return try {
            val systemPrompt = buildString {
                append("你是一位资深、严谨且富有同理心的内分泌与代谢健康智能助手（GI健康顾问）。\n")
                append("你的任务是帮助用户分析身体症状疑惑、解答饮食搭配与血糖波动问题。\n\n")
                if (privateContext.isNotBlank()) {
                    append(privateContext).append("\n\n")
                }
                if (searchEvidences.isNotEmpty()) {
                    append("【权威医学指南与临床检索依据】：\n")
                    searchEvidences.forEachIndexed { idx, ev ->
                        append("${idx + 1}. [${ev.source}] ${ev.title}：${ev.snippet}\n")
                    }
                    append("\n")
                }
                append("回答规范：\n")
                append("1. 态度温和有同理心，用通俗易懂的语言解释病理原因，结合用户的脱敏餐食/分型进行分析；\n")
                append("2. 给出立竿见影的应急步骤与生活方式调节（如进餐顺序、餐后散步、双十五测糖等）；\n")
                append("3. 严禁开具处方药或擅自调整剂量；\n")
                append("4. 末尾必须输出标准医疗免责提示：‘【温馨提示】AI 建议仅供健康管理与日常调理参考，不能替代执业医师诊断；若症状持续或加重，请立即前往医院内分泌科就医。’")
            }

            val messages = listOf(
                DeepSeekMessage(role = "system", content = systemPrompt),
                DeepSeekMessage(role = "user", content = userQuestion),
            )
            val request = DeepSeekRequest(
                model = "deepseek-chat",
                messages = messages,
                maxTokens = 4096,
                temperature = 0.3f,
            )

            val response = deepSeekApi.chat(
                authorization = "Bearer ${apiKeyProvider.deepSeekApiKey()}",
                request = request,
            )

            val choiceMsg = response.choices.firstOrNull()?.message
            val text = choiceMsg?.content?.takeIf { it.isNotBlank() }
                ?: choiceMsg?.reasoningContent?.takeIf { it.isNotBlank() }

            if (text.isNullOrBlank()) {
                Log.w(TAG, "DeepSeek返回空问答内容")
                NetworkResult.Error(message = "DeepSeek returned empty answer.")
            } else {
                Log.i(TAG, "DeepSeek健康问答生成成功")
                NetworkResult.Success(text)
            }
        } catch (exception: HttpException) {
            val errorBody = exception.response()?.errorBody()?.string() ?: ""
            Log.e(TAG, "DeepSeek问答 HTTP错误: ${exception.code()} | body: $errorBody", exception)
            NetworkResult.Error(
                code = exception.code(),
                message = "DeepSeek问答失败: HTTP ${exception.code()} $errorBody",
                throwable = exception,
            )
        } catch (exception: IOException) {
            Log.e(TAG, "DeepSeek问答 网络错误: ${exception.message}", exception)
            NetworkResult.Error(message = "网络请求失败，请检查网络连接后重试。", throwable = exception)
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            Log.e(TAG, "DeepSeek问答 未知错误: ${exception.message}", exception)
            NetworkResult.Error(message = "处理问答时发生未知异常: ${exception.message}", throwable = exception)
        }
    }

    override fun askHealthAssistantStream(
        userQuestion: String,
        privateContext: String,
        searchEvidences: List<com.example.diabetesassistant.domain.search.SearchEvidence>,
        enableThinking: Boolean,
    ): Flow<AiStreamChunk> = flow {
        Log.i(TAG, "===== DeepSeek 流式AI问答 (思考开关: $enableThinking) =====")

        val systemPrompt = buildString {
            append("你是一位资深、严谨且富有同理心的内分泌与代谢健康智能助手（GI健康顾问）。\n")
            append("你的任务是帮助用户分析身体症状疑惑、解答饮食搭配与血糖波动问题。\n\n")
            if (privateContext.isNotBlank()) {
                append(privateContext).append("\n\n")
            }
            if (searchEvidences.isNotEmpty()) {
                append("【权威医学指南与临床检索依据】：\n")
                searchEvidences.forEachIndexed { idx, ev ->
                    append("${idx + 1}. [${ev.source}] ${ev.title}：${ev.snippet}\n")
                }
                append("\n")
            }
            if (enableThinking) {
                append("【深度思考指导】：请在给出正式回答前，先在思考链中系统性梳理病理机制、鉴别潜在风险及定制化生活方式干预思路。若无原生思考字段，可使用 <think>思考过程</think> 标签包裹。\n\n")
            }
            append("回答规范：\n")
            append("1. 态度温和有同理心，用通俗易懂的语言解释病理原因，结合用户的脱敏餐食/分型进行分析；\n")
            append("2. 给出立竿见影的应急步骤与生活方式调节（如进餐顺序、餐后散步、双十五测糖等）；\n")
            append("3. 善用 Markdown 的小标题、加粗和清晰的分条列表组织要点；\n")
            append("4. 严禁开具处方药或擅自调整剂量；\n")
            append("5. 思维链指引：若开启思考推理，思考过程请紧扣核心生理机制、患者当前记录指标与鉴别诊断，精炼控制在 150~250 字以内，思考完成后立即开始输出正式解答；\n")
            append("6. 末尾必须输出标准医疗免责提示：‘【温馨提示】AI 建议仅供健康管理与日常调理参考，不能替代执业医师诊断；若症状持续或加重，请立即前往医院内分泌科就医。’")
        }

        val messages = listOf(
            DeepSeekMessage(role = "system", content = systemPrompt),
            DeepSeekMessage(role = "user", content = userQuestion),
        )

        val modelName = if (enableThinking) "deepseek-reasoner" else "deepseek-chat"
        val request = DeepSeekRequest(
            model = modelName,
            messages = messages,
            maxTokens = if (enableThinking) 4096 else 2048,
            temperature = if (enableThinking) 0.6f else 0.3f,
            stream = true,
        )

        try {
            val responseBody = deepSeekApi.chatStream(
                authorization = "Bearer ${apiKeyProvider.deepSeekApiKey()}",
                request = request,
            )

            var inThinkTag = false
            var pendingBuffer = ""

            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val raw = line?.trim() ?: continue
                    if (raw.isEmpty() || raw.startsWith(":")) continue
                    if (raw == "data: [DONE]") {
                        if (pendingBuffer.isNotEmpty()) {
                            val clean = pendingBuffer.replace("<think>", "").replace("</think>", "").trim()
                            if (clean.isNotEmpty()) {
                                if (inThinkTag) {
                                    emit(AiStreamChunk(reasoningDelta = clean))
                                } else {
                                    emit(AiStreamChunk(contentDelta = clean))
                                }
                            }
                            pendingBuffer = ""
                        }
                        emit(AiStreamChunk(isDone = true))
                        return@flow
                    }
                    if (raw.startsWith("data:")) {
                        val json = raw.substring(5).trim()
                        val chunk = try {
                            gson.fromJson(json, DeepSeekStreamChunk::class.java)
                        } catch (_: Exception) {
                            null
                        }
                        val delta = chunk?.choices?.firstOrNull()?.delta
                        val reasoning = delta?.reasoningContent ?: ""
                        val content = delta?.content ?: ""

                        if (reasoning.isNotEmpty()) {
                            emit(AiStreamChunk(reasoningDelta = reasoning))
                        }

                        if (content.isNotEmpty()) {
                            pendingBuffer += content

                            if (inThinkTag) {
                                if (pendingBuffer.contains("</think>")) {
                                    val parts = pendingBuffer.split("</think>", limit = 2)
                                    if (parts[0].isNotEmpty()) {
                                        emit(AiStreamChunk(reasoningDelta = parts[0]))
                                    }
                                    inThinkTag = false
                                    pendingBuffer = parts.getOrElse(1) { "" }
                                } else if (pendingBuffer.length > 8) {
                                    val safe = pendingBuffer.substring(0, pendingBuffer.length - 8)
                                    pendingBuffer = pendingBuffer.substring(pendingBuffer.length - 8)
                                    emit(AiStreamChunk(reasoningDelta = safe))
                                }
                            } else {
                                if (pendingBuffer.contains("<think>")) {
                                    val parts = pendingBuffer.split("<think>", limit = 2)
                                    if (parts[0].isNotEmpty()) {
                                        emit(AiStreamChunk(contentDelta = parts[0]))
                                    }
                                    inThinkTag = true
                                    pendingBuffer = parts.getOrElse(1) { "" }
                                } else if (pendingBuffer.contains("</think>")) {
                                    val parts = pendingBuffer.split("</think>", limit = 2)
                                    if (parts[0].isNotEmpty()) {
                                        emit(AiStreamChunk(reasoningDelta = parts[0]))
                                    }
                                    inThinkTag = false
                                    pendingBuffer = parts.getOrElse(1) { "" }
                                } else {
                                    val candidateTag = listOf("<", "</", "</t", "</th", "</thi", "</thin", "<t", "<th", "<thi", "<thin")
                                    val matchedSuffix = candidateTag.firstOrNull { pendingBuffer.endsWith(it) }
                                    if (matchedSuffix != null) {
                                        val splitIdx = pendingBuffer.lastIndexOf('<')
                                        if (splitIdx > 0) {
                                            val safe = pendingBuffer.substring(0, splitIdx)
                                            pendingBuffer = pendingBuffer.substring(splitIdx)
                                            emit(AiStreamChunk(contentDelta = safe))
                                        }
                                    } else {
                                        emit(AiStreamChunk(contentDelta = pendingBuffer))
                                        pendingBuffer = ""
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (pendingBuffer.isNotEmpty()) {
                val clean = pendingBuffer.replace("<think>", "").replace("</think>", "").trim()
                if (clean.isNotEmpty()) {
                    if (inThinkTag) {
                        emit(AiStreamChunk(reasoningDelta = clean))
                    } else {
                        emit(AiStreamChunk(contentDelta = clean))
                    }
                }
                pendingBuffer = ""
            }
            emit(AiStreamChunk(isDone = true))
        } catch (e: Exception) {
            Log.w(TAG, "流式响应异常 (${e.message})，启用降级缓冲流输出", e)
            val fallbackResult = askHealthAssistant(
                userQuestion = userQuestion,
                privateContext = privateContext,
                searchEvidences = searchEvidences,
            )
            when (fallbackResult) {
                is NetworkResult.Success -> {
                    val fullText = fallbackResult.data
                    val thinkStart = fullText.indexOf("<think>")
                    val thinkEnd = fullText.indexOf("</think>")
                    val (reasoning, answer) = if (thinkStart != -1 && thinkEnd > thinkStart) {
                        val r = fullText.substring(thinkStart + 7, thinkEnd).trim()
                        val a = fullText.substring(thinkEnd + 8).trim()
                        r to a
                    } else {
                        "" to fullText
                    }

                    if (reasoning.isNotEmpty()) {
                        for (chunk in reasoning.chunked(4)) {
                            emit(AiStreamChunk(reasoningDelta = chunk))
                            delay(18)
                        }
                    }
                    for (chunk in answer.chunked(5)) {
                        emit(AiStreamChunk(contentDelta = chunk))
                        delay(20)
                    }
                    emit(AiStreamChunk(isDone = true))
                }
                is NetworkResult.Error -> {
                    emit(AiStreamChunk(isDone = true, errorMessage = fallbackResult.message))
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun uriToBase64(uriString: String): String {
        val uri = android.net.Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open image: $uriString")

        val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
            ?: throw IOException("Cannot decode bitmap from: $uriString")

        val maxDim = 1280
        val width = bitmap.width
        val height = bitmap.height
        val scaledBitmap = if (width > maxDim || height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(width, height)
            val newW = (width * scale).toInt()
            val newH = (height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            text.substring(start, end + 1)
        } else {
            text
        }
    }

    companion object {
        private const val TAG = "Diabetes|AiRepo"
    }
}

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
import kotlinx.coroutines.CancellationException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

interface AiRepository {
    suspend fun recognizeDishJson(imageUrl: String, prompt: String): NetworkResult<String>
    suspend fun recognizeDishesJson(imageUrls: List<String>, prompt: String): NetworkResult<String>
    suspend fun generateReminderText(prompt: String): NetworkResult<String>
}

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val apiKeyProvider: ApiKeyProvider,
    @ApplicationContext private val context: Context,
) : AiRepository {

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
                maxTokens = 2048,
                temperature = 0.1f,
            )
            Log.d(TAG, "发送DeepSeek-flash视觉识别请求 (包含 ${imageUrls.size} 张图片)...")

            val response = deepSeekApi.chat(
                authorization = "Bearer ${apiKeyProvider.deepSeekApiKey()}",
                request = request,
            )
            Log.d(TAG, "DeepSeek响应收到 — choices数量: ${response.choices.size}")

            val text = response.choices.firstOrNull()?.message?.content
            if (text.isNullOrBlank()) {
                Log.w(TAG, "DeepSeek返回空内容")
                NetworkResult.Error(message = "DeepSeek returned empty content.")
            } else {
                val extracted = extractJson(text)
                Log.d(TAG, "DeepSeek识别成功 — 原始文本长度: ${text.length}, 提取JSON长度: ${extracted.length}")
                Log.d(TAG, "识别JSON: $extracted")
                NetworkResult.Success(extracted)
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
                maxTokens = 2048,
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

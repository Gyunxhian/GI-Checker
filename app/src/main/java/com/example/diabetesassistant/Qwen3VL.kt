package com.example.diabetesassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.concurrent.TimeUnit

class Qwen3VL(private val activity: ComponentActivity) {

    private var onResult: ((String?) -> Unit)? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val takePhotoLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val image = result.data?.extras?.get("data") as? Bitmap
                image?.let { analyzeImage(it) } ?: onResult?.invoke(null)
            } else {
                onResult?.invoke(null)
            }
        }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePhotoLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            } else {
                Toast.makeText(activity, "需要相机权限", Toast.LENGTH_SHORT).show()
                onResult?.invoke(null)
            }
        }

    fun startAnalyzePhoto(resultCallback: (String?) -> Unit) {
        this.onResult = resultCallback

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            takePhotoLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        val base64 = bitmapToBase64(bitmap)

        coroutineScope.launch(Dispatchers.IO) {
            val result = callQwenAPI(base64)
            coroutineScope.launch(Dispatchers.Main) {
                onResult?.invoke(result)
                onResult = null
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val bos = ByteArrayOutputStream()
        val small = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        small.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        return "data:image/jpeg;base64,${Base64.getEncoder().encodeToString(bos.toByteArray())}"
    }

    private fun callQwenAPI(imageBase64: String): String {
        val apiKey = "sk-1378b29a520b4c27827ed49396837aca"
        val prompt = "详细描述这张图片"

        val json = """
            {
              "model": "qwen3-vl-plus",
              "stream": false,
              "enable_thinking": false,
              "messages": [
                {
                  "role": "user",
                  "content": [
                    {"type":"image_url","image_url":{"url":"$imageBase64"}},
                    {"type":"text","text":"$prompt"}
                  ]
                }
              ]
            }
        """.trimIndent()

        val client = OkHttpClient.Builder()
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .build()

        return try {
            val request = Request.Builder()
                .url("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val body = client.newCall(request).execute().body!!.string()
            Gson().fromJson(body, ApiResponse::class.java).choices.firstOrNull()?.message?.content ?: "解析失败"
        } catch (e: Exception) {
            "错误：${e.message}"
        }
    }

    private data class ApiResponse(val choices: List<Choice>)
    private data class Choice(val message: Message)
    private data class Message(val content: String)
}
package com.example.diabetesassistant.camera

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.entity.FoodRecordStatus
import com.example.diabetesassistant.data.model.DishRecognitionResult
import com.example.diabetesassistant.data.model.InsulinType
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.domain.algorithm.BloodSugarPoint
import com.example.diabetesassistant.domain.algorithm.BloodSugarPrediction
import com.example.diabetesassistant.domain.algorithm.FocusPeriod
import com.example.diabetesassistant.domain.algorithm.InsulinBandResult
import com.example.diabetesassistant.domain.usecase.AnalyzeFoodUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "Diabetes|CameraVM"

data class ArchiveDetailUiState(
    val imageUri: String = "",
    val stagedImages: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRecalculating: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val totalCarbIntake: Float = 0f,
    val weightedGi: Float = 0f,
    val bloodSugarPrediction: BloodSugarPrediction? = null,
    val insulinBand: InsulinBandResult? = null,
    val focusPeriods: List<FocusPeriod> = emptyList(),
    val reminders: List<String> = emptyList(),
    val meatRatio: Float = 0.5f,
    val intakePercent: Float = 0.6f,
    val bloodSugarStart: Float = 5.0f,
    val insulinType: InsulinType = InsulinType.RAPID,
    val savedFoodRecordId: Long? = null,
    val recordDate: Long? = null,
    val recognitionResult: DishRecognitionResult? = null,
    val ingredientCarbsMap: Map<String, Float> = emptyMap(),
)

@HiltViewModel
class CameraRecognitionViewModel @Inject constructor(
    private val analyzeFoodUseCase: AnalyzeFoodUseCase,
    private val foodRecordRepository: FoodRecordRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveDetailUiState())
    val uiState: StateFlow<ArchiveDetailUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    private var cachedRecognitionResult: DishRecognitionResult? = null
    private var recalcDebounceJob: Job? = null

    fun setCapturedImage(uri: String) {
        Log.i(TAG, "设置图片URI: $uri")
        _uiState.update { it.copy(imageUri = uri, stagedImages = listOf(uri)) }
    }

    fun addStagedImage(uri: String) {
        if (uri.isBlank()) return
        _uiState.update { current ->
            val updated = (current.stagedImages + uri).distinct().take(6)
            current.copy(stagedImages = updated, imageUri = updated.joinToString("|"))
        }
    }

    fun addStagedImages(uris: List<String>) {
        if (uris.isEmpty()) return
        _uiState.update { current ->
            val updated = (current.stagedImages + uris).distinct().take(6)
            current.copy(stagedImages = updated, imageUri = updated.joinToString("|"))
        }
    }

    fun removeStagedImage(uri: String) {
        _uiState.update { current ->
            val updated = current.stagedImages.filter { it != uri }
            current.copy(stagedImages = updated, imageUri = updated.joinToString("|"))
        }
    }

    fun clearStagedImages() {
        _uiState.update { it.copy(stagedImages = emptyList(), imageUri = "") }
    }

    suspend fun startAnalysis() {
        val state = _uiState.value
        val images = if (state.stagedImages.isNotEmpty()) state.stagedImages else listOf(state.imageUri).filter { it.isNotBlank() }
        Log.i(TAG, "===== 开始分析 (${images.size}张图片) =====")
        if (images.isEmpty()) {
            Log.w(TAG, "图片列表为空，跳过分析")
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            when (val result = analyzeFoodUseCase(
                imageUrls = images,
                meatRatio = state.meatRatio,
                intakePercent = state.intakePercent,
                insulinType = state.insulinType,
                bloodSugarStart = state.bloodSugarStart,
            )) {
                is NetworkResult.Success -> {
                    cachedRecognitionResult = result.data.recognitionResult
                    Log.i(TAG, "分析成功: carb=${result.data.totalCarbIntake}g, GI=${result.data.weightedGi}, periods=${result.data.focusPeriods.size}")
                    _uiState.update {
                        it.copy(isLoading = false, error = null,
                            totalCarbIntake = result.data.totalCarbIntake,
                            weightedGi = result.data.weightedGi,
                            bloodSugarPrediction = result.data.bloodSugarPrediction,
                            insulinBand = result.data.insulinBand,
                            focusPeriods = result.data.focusPeriods,
                            reminders = result.data.reminders,
                            recognitionResult = result.data.recognitionResult,
                            ingredientCarbsMap = result.data.ingredientCarbsMap,
                        )
                    }
                    autoSaveRecord()
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "分析失败: ${result.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "分析异常: ${e.message}", e)
            _uiState.update { it.copy(isLoading = false, error = e.message ?: "分析失败") }
        }
    }

    fun updateMeatRatio(ratio: Float) {
        val clamped = ratio.coerceIn(0f, 1f)
        if (clamped == _uiState.value.meatRatio) return
        Log.i(TAG, "荤素比变更: $clamped")
        _uiState.update { it.copy(meatRatio = clamped) }
        debounceRecalculation()
    }

    fun updateIntakePercent(percent: Float) {
        val clamped = percent.coerceIn(0f, 1f)
        if (clamped == _uiState.value.intakePercent) return
        Log.i(TAG, "摄入比变更: $clamped")
        _uiState.update { it.copy(intakePercent = clamped) }
        debounceRecalculation()
    }

    fun updateBloodSugarStart(bg: Float) {
        val clamped = bg.coerceIn(2.0f, 20.0f)
        if (clamped == _uiState.value.bloodSugarStart) return
        Log.i(TAG, "血糖变更: $clamped")
        _uiState.update { it.copy(bloodSugarStart = clamped) }
        debounceRecalculation()
    }

    fun updateInsulinType(type: InsulinType) {
        if (type == _uiState.value.insulinType) return
        Log.i(TAG, "胰岛素类型变更: $type")
        _uiState.update { it.copy(insulinType = type) }
        debounceRecalculation()
    }

    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                val record = foodRecordRepository.getById(recordId) ?: return@launch
                val parsed = try {
                    gson.fromJson(record.aiJson, AnalysisResultJson::class.java)
                } catch (e: Exception) {
                    null
                }
                cachedRecognitionResult = parsed?.recognitionResult
                val staged = record.imagePath.split("|").filter { it.isNotBlank() }
                _uiState.update {
                    it.copy(
                        imageUri = record.imagePath,
                        stagedImages = staged,
                        savedFoodRecordId = record.id,
                        recordDate = record.recordDate,
                        totalCarbIntake = parsed?.totalCarbIntake ?: 0f,
                        weightedGi = parsed?.weightedGi ?: 0f,
                        meatRatio = parsed?.meatRatio ?: 0.5f,
                        intakePercent = parsed?.intakePercent ?: 0.6f,
                        bloodSugarStart = parsed?.bloodSugarStart ?: 5.0f,
                        insulinType = try {
                            InsulinType.valueOf(parsed?.insulinType ?: "RAPID")
                        } catch (e: Exception) {
                            InsulinType.RAPID
                        },
                        bloodSugarPrediction = if (parsed?.curvePoints != null) {
                            val peak = parsed.curvePoints.maxOfOrNull { pt -> pt.bloodSugar } ?: 5.0f
                            val peakMin = parsed.curvePoints.maxByOrNull { pt -> pt.bloodSugar }?.minute ?: 60
                            BloodSugarPrediction(
                                points = parsed.curvePoints,
                                gammaParams = com.example.diabetesassistant.domain.algorithm.GammaParams(1.5f, 20f),
                                peakMinute = peakMin,
                                peakBloodSugar = peak,
                            )
                        } else null,
                        insulinBand = parsed?.insulinBand,
                        focusPeriods = parsed?.focusPeriods ?: emptyList(),
                        reminders = parsed?.reminders ?: emptyList(),
                        recognitionResult = parsed?.recognitionResult,
                        ingredientCarbsMap = parsed?.ingredientCarbsMap ?: emptyMap(),
                        isLoading = false,
                        error = null,
                    )
                }
                Log.i(TAG, "已加载真实档案: id=$recordId, carb=${parsed?.totalCarbIntake}g")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "加载档案异常: ${e.message}", e)
            }
        }
    }

    fun saveOrUpdateRecord(onComplete: () -> Unit = {}) {
        val state = _uiState.value
        val uri = state.imageUri
        if (uri.isBlank()) {
            Log.w(TAG, "保存跳过: URI为空")
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val realDishName = cachedRecognitionResult?.dishes?.firstOrNull()?.dishName
                    ?.takeIf { it.isNotBlank() }
                    ?: "餐食分析"
                val json = buildSaveJson(state)
                val existingId = state.savedFoodRecordId
                if (existingId != null) {
                    val originalDate = state.recordDate
                        ?: foodRecordRepository.getById(existingId)?.recordDate
                        ?: System.currentTimeMillis()
                    val updated = FoodRecord(
                        id = existingId,
                        imagePath = uri,
                        recordDate = originalDate,
                        dishName = realDishName,
                        aiJson = json,
                        status = FoodRecordStatus.COMPLETED,
                    )
                    foodRecordRepository.update(updated)
                    _uiState.update { it.copy(recordDate = originalDate) }
                    Log.i(TAG, "记录已更新(保留原始时间戳): id=$existingId, recordDate=$originalDate")
                } else {
                    val now = System.currentTimeMillis()
                    val record = FoodRecord(
                        imagePath = uri,
                        recordDate = now,
                        dishName = realDishName,
                        aiJson = json,
                        status = FoodRecordStatus.COMPLETED,
                    )
                    val id = foodRecordRepository.create(record)
                    _uiState.update { it.copy(savedFoodRecordId = id, recordDate = now) }
                    Log.i(TAG, "记录已创建: id=$id, recordDate=$now")
                }
                _uiState.update { it.copy(isSaving = false) }
                Toast.makeText(context, "已成功记录至健康档案", Toast.LENGTH_SHORT).show()
                onComplete()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "保存失败: ${e.message}", e)
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetFlow() {
        cachedRecognitionResult = null
        _uiState.value = ArchiveDetailUiState()
    }

    private suspend fun autoSaveRecord() {
        val state = _uiState.value
        val uri = state.imageUri
        if (uri.isBlank() || state.savedFoodRecordId != null) return
        try {
            val realDishName = cachedRecognitionResult?.dishes?.firstOrNull()?.dishName
                ?.takeIf { it.isNotBlank() }
                ?: "餐食分析"
            val json = buildSaveJson(state)
            val now = System.currentTimeMillis()
            val record = FoodRecord(
                imagePath = uri,
                recordDate = now,
                dishName = realDishName,
                aiJson = json,
                status = FoodRecordStatus.COMPLETED,
            )
            val id = foodRecordRepository.create(record)
            _uiState.update { it.copy(savedFoodRecordId = id, recordDate = now) }
            Log.i(TAG, "自动保存完成: id=$id, recordDate=$now, carb=${state.totalCarbIntake}g")
        } catch (e: Exception) {
            Log.e(TAG, "自动保存失败: ${e.message}", e)
        }
    }

    private fun buildSaveJson(state: ArchiveDetailUiState): String {
        return gson.toJson(
            AnalysisResultJson(
                totalCarbIntake = state.totalCarbIntake,
                weightedGi = state.weightedGi,
                meatRatio = state.meatRatio,
                intakePercent = state.intakePercent,
                insulinType = state.insulinType.name,
                bloodSugarStart = state.bloodSugarStart,
                recognitionResult = cachedRecognitionResult,
                curvePoints = state.bloodSugarPrediction?.points,
                insulinBand = state.insulinBand,
                focusPeriods = state.focusPeriods,
                reminders = state.reminders,
                ingredientCarbsMap = state.ingredientCarbsMap,
            )
        )
    }

    private fun debounceRecalculation() {
        recalcDebounceJob?.cancel()
        recalcDebounceJob = viewModelScope.launch {
            delay(250L)
            runRecalculation()
        }
    }

    private suspend fun runRecalculation() {
        val parsed = cachedRecognitionResult ?: run {
            Log.w(TAG, "重算跳过: 无缓存识别结果")
            return
        }
        val currentMeatRatio = _uiState.value.meatRatio
        val currentIntakePercent = _uiState.value.intakePercent
        val currentBloodSugar = _uiState.value.bloodSugarStart
        Log.i(TAG, "===== 重算: meatRatio=$currentMeatRatio, intake=$currentIntakePercent, bg=$currentBloodSugar =====")
        val state = _uiState.value
        _uiState.update { it.copy(isRecalculating = true) }
        try {
            when (val result = analyzeFoodUseCase.recalculate(
                recognitionResult = parsed,
                meatRatio = currentMeatRatio,
                intakePercent = currentIntakePercent,
                insulinType = state.insulinType,
                bloodSugarStart = currentBloodSugar,
            )) {
                is NetworkResult.Success -> {
                    Log.i(TAG, "重算完成: carb=${result.data.totalCarbIntake}g, GI=${result.data.weightedGi}")
                    _uiState.update {
                        it.copy(isRecalculating = false, error = null,
                            totalCarbIntake = result.data.totalCarbIntake,
                            weightedGi = result.data.weightedGi,
                            bloodSugarPrediction = result.data.bloodSugarPrediction,
                            insulinBand = result.data.insulinBand,
                            focusPeriods = result.data.focusPeriods,
                            reminders = result.data.reminders,
                            recognitionResult = result.data.recognitionResult,
                            ingredientCarbsMap = result.data.ingredientCarbsMap,
                        )
                    }
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "重算失败: ${result.message}")
                    _uiState.update { it.copy(isRecalculating = false, error = result.message) }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "重算异常: ${e.message}", e)
            _uiState.update { it.copy(isRecalculating = false, error = e.message ?: "重新计算失败") }
        }
    }
}

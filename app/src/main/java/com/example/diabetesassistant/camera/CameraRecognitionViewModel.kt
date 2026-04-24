package com.example.diabetesassistant.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecognizedIngredient(
    val name: String,
    val portionGrams: Int,
    val caloriesPer100g: Int,
    val sugarPer100g: Float,
    val cookingMethod: CookingMethod,
)

class CameraRecognitionViewModel : ViewModel() {

    private val _pendingImageUri = MutableStateFlow<String?>(null)
    val pendingImageUri: StateFlow<String?> = _pendingImageUri.asStateFlow()

    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients.asStateFlow()

    private val _recognizedIngredients = MutableStateFlow<List<RecognizedIngredient>>(emptyList())
    val recognizedIngredients: StateFlow<List<RecognizedIngredient>> = _recognizedIngredients.asStateFlow()

    private val _estimatedSugarGrams = MutableStateFlow(0f)
    val estimatedSugarGrams: StateFlow<Float> = _estimatedSugarGrams.asStateFlow()

    private val _suggestedInsulinUnits = MutableStateFlow(0f)
    val suggestedInsulinUnits: StateFlow<Float> = _suggestedInsulinUnits.asStateFlow()

    fun setPendingCaptureUri(uriString: String) {
        _pendingImageUri.value = uriString
    }

    fun updateIngredient(index: Int, value: String) {
        _ingredients.update { list ->
            list.mapIndexed { i, item -> if (i == index) value else item }
        }
        _recognizedIngredients.update { list ->
            list.mapIndexed { i, item -> if (i == index) item.copy(name = value) else item }
        }
        recomputeEstimates()
    }

    fun removeIngredient(index: Int) {
        _ingredients.update { list -> list.filterIndexed { i, _ -> i != index } }
        _recognizedIngredients.update { list -> list.filterIndexed { i, _ -> i != index } }
        recomputeEstimates()
    }

    fun addIngredient(name: String = "") {
        _ingredients.update { it + name }
        _recognizedIngredients.update {
            it + RecognizedIngredient(
                name = name.ifBlank { "新增食材" },
                portionGrams = 100,
                caloriesPer100g = 80,
                sugarPer100g = 8f,
                cookingMethod = CookingMethod.Other,
            )
        }
        recomputeEstimates()
    }

    fun adjustIngredientWeight(index: Int, deltaGrams: Int) {
        _recognizedIngredients.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) {
                    item.copy(portionGrams = (item.portionGrams + deltaGrams).coerceIn(10, 600))
                } else {
                    item
                }
            }
        }
        recomputeEstimates()
    }

    fun updateIngredientCookingMethod(index: Int, method: CookingMethod) {
        _recognizedIngredients.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(cookingMethod = method) else item
            }
        }
        recomputeEstimates()
    }

    fun splitIngredientCooking(index: Int) {
        _recognizedIngredients.update { list ->
            val item = list.getOrNull(index) ?: return@update list
            if (item.portionGrams <= 20) return@update list
            val firstPortion = item.portionGrams / 2
            val secondPortion = item.portionGrams - firstPortion
            list.flatMapIndexed { i, current ->
                if (i == index) {
                    listOf(
                        current.copy(portionGrams = firstPortion),
                        current.copy(
                            portionGrams = secondPortion,
                            cookingMethod = CookingMethod.Other,
                        ),
                    )
                } else {
                    listOf(current)
                }
            }
        }
        syncLegacyIngredientNames()
        recomputeEstimates()
    }

    suspend fun runSimulatedRecognition() {
        delay(2400L)
        val recognized = listOf(
            RecognizedIngredient(
                name = "米饭（白米）",
                portionGrams = 150,
                caloriesPer100g = 130,
                sugarPer100g = 28.5f,
                cookingMethod = CookingMethod.Steam,
            ),
            RecognizedIngredient(
                name = "煎鸡胸肉",
                portionGrams = 120,
                caloriesPer100g = 165,
                sugarPer100g = 0.2f,
                cookingMethod = CookingMethod.StirFry,
            ),
            RecognizedIngredient(
                name = "水煮西兰花",
                portionGrams = 80,
                caloriesPer100g = 34,
                sugarPer100g = 4.0f,
                cookingMethod = CookingMethod.Boil,
            ),
        )
        _recognizedIngredients.value = recognized
        _ingredients.value = recognized.map { it.name }
        recomputeEstimates()
    }

    private fun recomputeEstimates() {
        val base = _recognizedIngredients.value.sumOf { ingredient ->
            (
                ingredient.sugarPer100g *
                    ingredient.portionGrams /
                    100f *
                    cookingMethodFactor(ingredient.cookingMethod)
                ).toDouble()
        }.toFloat()
        val sugar = base.coerceIn(4f, 85f)
        _estimatedSugarGrams.value = sugar
        _suggestedInsulinUnits.value = (sugar / 15f).coerceIn(0.5f, 14f)
    }

    fun ingredientSugarGrams(ingredient: RecognizedIngredient): Float {
        return ingredient.sugarPer100g *
            ingredient.portionGrams /
            100f *
            cookingMethodFactor(ingredient.cookingMethod)
    }

    private fun cookingMethodFactor(method: CookingMethod): Float {
        return when (method) {
            CookingMethod.Braise -> 1.18f
            CookingMethod.Roast -> 1.12f
            CookingMethod.StirFry -> 1.08f
            CookingMethod.Boil -> 0.95f
            CookingMethod.ColdDish -> 1.05f
            CookingMethod.Steam -> 1f
            CookingMethod.Other -> 1.1f
        }
    }

    fun resetFlow() {
        _pendingImageUri.value = null
        _ingredients.value = emptyList()
        _recognizedIngredients.value = emptyList()
        _estimatedSugarGrams.value = 0f
        _suggestedInsulinUnits.value = 0f
    }

    fun estimatedCalories(): Int {
        return _recognizedIngredients.value.sumOf { ingredient ->
            ingredient.caloriesPer100g * ingredient.portionGrams / 100
        }
    }

    private fun syncLegacyIngredientNames() {
        _ingredients.value = _recognizedIngredients.value.map { it.name }
    }
}

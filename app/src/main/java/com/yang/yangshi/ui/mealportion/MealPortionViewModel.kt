package com.yang.yangshi.ui.mealportion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yang.yangshi.data.repository.DietRepository
import com.yang.yangshi.domain.calculator.FoodNutrientDensity
import com.yang.yangshi.domain.calculator.FoodPortionAllocationEngine
import com.yang.yangshi.domain.calculator.FoodPortionInput
import com.yang.yangshi.domain.calculator.FoodPortionResult
import com.yang.yangshi.domain.calculator.MacroTarget
import com.yang.yangshi.domain.calculator.MacroTargetCalculator
import com.yang.yangshi.domain.calculator.MacroType
import com.yang.yangshi.domain.model.FoodItem
import com.yang.yangshi.domain.model.MealLogItem
import com.yang.yangshi.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MealPortionUiState(
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val selectedMealType: MealType = MealType.LUNCH,
    val searchQuery: String = "",
    val searchResults: List<FoodItem> = emptyList(),
    val isSearching: Boolean = false,
    val selectedFoodInputs: List<FoodPortionInput> = emptyList(),
    val mealTarget: MacroTarget = MacroTarget(84.0, 56.0, 22.4, 762.0),
    val calculationResult: com.yang.yangshi.domain.calculator.MultiPortionResult? = null,
    val singleFoodTargetMacro: MacroType = MacroType.CARBS,
    val singleFoodTargetGramsText: String = "30.0",
    val singleFoodResult: FoodPortionResult? = null,
    val addSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class MealPortionViewModel(
    private val repository: DietRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPortionUiState())
    val uiState: StateFlow<MealPortionUiState> = _uiState.asStateFlow()

    init {
        loadMealTarget()
    }

    fun setMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType, addSuccessMessage = null) }
        loadMealTarget()
    }

    private fun loadMealTarget() {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce()
            val dailyTarget = MacroTargetCalculator.calculateDailyTarget(profile)
            val mealRatio = when (_uiState.value.selectedMealType) {
                MealType.BREAKFAST -> profile.breakfastRatio
                MealType.LUNCH -> profile.lunchRatio
                MealType.DINNER -> profile.dinnerRatio
                MealType.SNACK -> profile.snackRatio
            }
            val target = MacroTargetCalculator.calculateMealTarget(dailyTarget, mealRatio * 100)
            _uiState.update { it.copy(mealTarget = target) }
            recalculateIfHasInputs()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchFood() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            val results = repository.searchChinaNutriFoods(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun addFoodToCalculation(foodItem: FoodItem) {
        val density = FoodNutrientDensity(
            foodId = foodItem.id,
            name = foodItem.name,
            carbsPer100g = foodItem.carbsG,
            proteinPer100g = foodItem.proteinG,
            fatPer100g = foodItem.fatG,
            edibleRatio = foodItem.edibleRatio
        )
        val newInput = FoodPortionInput(density = density)
        val currentInputs = _uiState.value.selectedFoodInputs.toMutableList()
        currentInputs.add(newInput)

        _uiState.update { it.copy(selectedFoodInputs = currentInputs, addSuccessMessage = null) }
        recalculateMultiPortions()
    }

    fun removeFoodFromCalculation(index: Int) {
        val current = _uiState.value.selectedFoodInputs.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.update { it.copy(selectedFoodInputs = current, addSuccessMessage = null) }
            recalculateMultiPortions()
        }
    }

    fun toggleLockFood(index: Int, isLocked: Boolean, fixedWeightGrams: Double?) {
        val current = _uiState.value.selectedFoodInputs.toMutableList()
        if (index in current.indices) {
            val old = current[index]
            current[index] = old.copy(isLocked = isLocked, fixedWeightGrams = fixedWeightGrams)
            _uiState.update { it.copy(selectedFoodInputs = current, addSuccessMessage = null) }
            recalculateMultiPortions()
        }
    }

    fun recalculateMultiPortions() {
        val state = _uiState.value
        if (state.selectedFoodInputs.isEmpty()) {
            _uiState.update { it.copy(calculationResult = null) }
            return
        }

        val result = FoodPortionAllocationEngine.calculateMultiFoodPortions(
            mealTarget = state.mealTarget,
            inputs = state.selectedFoodInputs
        )
        _uiState.update { it.copy(calculationResult = result) }
    }

    private fun recalculateIfHasInputs() {
        if (_uiState.value.selectedFoodInputs.isNotEmpty()) {
            recalculateMultiPortions()
        }
    }

    fun calculateSingleFoodPortion(foodItem: FoodItem) {
        val targetGrams = _uiState.value.singleFoodTargetGramsText.toDoubleOrNull() ?: 30.0
        val density = FoodNutrientDensity(
            foodId = foodItem.id,
            name = foodItem.name,
            carbsPer100g = foodItem.carbsG,
            proteinPer100g = foodItem.proteinG,
            fatPer100g = foodItem.fatG,
            edibleRatio = foodItem.edibleRatio
        )
        val res = FoodPortionAllocationEngine.calculateSingleFoodPortion(
            targetGrams = targetGrams,
            targetMacro = _uiState.value.singleFoodTargetMacro,
            density = density
        )
        _uiState.update { it.copy(singleFoodResult = res) }
    }

    fun onSingleFoodTargetGramsChanged(text: String) {
        _uiState.update { it.copy(singleFoodTargetGramsText = text) }
    }

    fun onSingleFoodTargetMacroChanged(macroType: MacroType) {
        _uiState.update { it.copy(singleFoodTargetMacro = macroType) }
    }

    fun addToMealLog() {
        val state = _uiState.value
        val calcResult = state.calculationResult

        if (calcResult == null || calcResult.portions.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请先添加食物并自动计算配平重量") }
            return
        }

        viewModelScope.launch {
            val logItems = calcResult.portions.map { portion ->
                MealLogItem(
                    foodItemId = portion.foodId,
                    foodName = portion.foodName,
                    foodWeightG = portion.weightGrams,
                    actualCarbs = portion.actualCarbs,
                    actualProtein = portion.actualProtein,
                    actualFat = portion.actualFat,
                    actualEnergy = portion.actualCalories,
                    isLocked = portion.isLocked
                )
            }

            repository.saveMealLogWithItems(
                date = state.selectedDate,
                mealType = state.selectedMealType,
                target = state.mealTarget,
                items = logItems
            )

            _uiState.update {
                it.copy(
                    addSuccessMessage = "成功添加至 ${state.selectedDate} ${state.selectedMealType.displayName} 打卡日志！",
                    errorMessage = null
                )
            }
        }
    }
}

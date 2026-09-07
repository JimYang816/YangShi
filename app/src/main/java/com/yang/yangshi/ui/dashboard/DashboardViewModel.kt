package com.yang.yangshi.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yang.yangshi.data.repository.DietRepository
import com.yang.yangshi.domain.calculator.MacroConstants
import com.yang.yangshi.domain.calculator.MacroTarget
import com.yang.yangshi.domain.calculator.MacroTargetCalculator
import com.yang.yangshi.domain.model.MealLog
import com.yang.yangshi.domain.model.MealType
import com.yang.yangshi.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val userProfile: UserProfile = UserProfile(),
    val dailyTarget: MacroTarget = MacroTarget(210.0, 140.0, 56.0, 1904.0),
    val totalConsumed: MacroTarget = MacroTarget(0.0, 0.0, 0.0, 0.0),
    val remainingTarget: MacroTarget = MacroTarget(210.0, 140.0, 56.0, 1904.0),
    val carbsProgress: Float = 0.0f,
    val proteinProgress: Float = 0.0f,
    val fatProgress: Float = 0.0f,
    val caloriesProgress: Float = 0.0f,
    val mealSummaries: Map<MealType, MealLog?> = mapOf(
        MealType.BREAKFAST to null,
        MealType.LUNCH to null,
        MealType.DINNER to null,
        MealType.SNACK to null
    ),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: DietRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getUserProfile(),
                _uiState
            ) { profile, currentState ->
                Pair(profile, currentState.selectedDate)
            }.collect { (profile, date) ->
                loadDataForDate(profile, date)
            }
        }
    }

    private fun loadDataForDate(profile: UserProfile, date: String) {
        viewModelScope.launch {
            repository.getMealLogsForDate(date).collect { logs ->
                val dailyTarget = MacroTargetCalculator.calculateDailyTarget(profile)
                
                var consumedCarbs = 0.0
                var consumedProtein = 0.0
                var consumedFat = 0.0

                val mealMap = mutableMapOf<MealType, MealLog?>()
                MealType.entries.forEach { mealMap[it] = null }

                logs.forEach { log ->
                    mealMap[log.mealType] = log
                    consumedCarbs += log.totalActualCarbs
                    consumedProtein += log.totalActualProtein
                    consumedFat += log.totalActualFat
                }

                val consumedCalories = consumedCarbs * MacroConstants.KCAL_PER_G_CARBS +
                        consumedProtein * MacroConstants.KCAL_PER_G_PROTEIN +
                        consumedFat * MacroConstants.KCAL_PER_G_FAT

                val totalConsumed = MacroTarget(
                    carbsGrams = MacroTargetCalculator.roundToOneDecimal(consumedCarbs),
                    proteinGrams = MacroTargetCalculator.roundToOneDecimal(consumedProtein),
                    fatGrams = MacroTargetCalculator.roundToOneDecimal(consumedFat),
                    caloriesKcal = MacroTargetCalculator.roundToInteger(consumedCalories)
                )

                val remCarbs = (dailyTarget.carbsGrams - totalConsumed.carbsGrams).coerceAtLeast(0.0)
                val remProtein = (dailyTarget.proteinGrams - totalConsumed.proteinGrams).coerceAtLeast(0.0)
                val remFat = (dailyTarget.fatGrams - totalConsumed.fatGrams).coerceAtLeast(0.0)
                val remCal = (dailyTarget.caloriesKcal - totalConsumed.caloriesKcal).coerceAtLeast(0.0)

                val remainingTarget = MacroTarget(
                    carbsGrams = MacroTargetCalculator.roundToOneDecimal(remCarbs),
                    proteinGrams = MacroTargetCalculator.roundToOneDecimal(remProtein),
                    fatGrams = MacroTargetCalculator.roundToOneDecimal(remFat),
                    caloriesKcal = MacroTargetCalculator.roundToInteger(remCal)
                )

                _uiState.update { state ->
                    state.copy(
                        selectedDate = date,
                        userProfile = profile,
                        dailyTarget = dailyTarget,
                        totalConsumed = totalConsumed,
                        remainingTarget = remainingTarget,
                        carbsProgress = if (dailyTarget.carbsGrams > 0) (totalConsumed.carbsGrams / dailyTarget.carbsGrams).toFloat().coerceIn(0f, 1f) else 0f,
                        proteinProgress = if (dailyTarget.proteinGrams > 0) (totalConsumed.proteinGrams / dailyTarget.proteinGrams).toFloat().coerceIn(0f, 1f) else 0f,
                        fatProgress = if (dailyTarget.fatGrams > 0) (totalConsumed.fatGrams / dailyTarget.fatGrams).toFloat().coerceIn(0f, 1f) else 0f,
                        caloriesProgress = if (dailyTarget.caloriesKcal > 0) (totalConsumed.caloriesKcal / dailyTarget.caloriesKcal).toFloat().coerceIn(0f, 1f) else 0f,
                        mealSummaries = mealMap
                    )
                }
            }
        }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadDataForDate(_uiState.value.userProfile, date)
    }

    fun navigatePreviousDay() {
        changeDay(-1)
    }

    fun navigateNextDay() {
        changeDay(1)
    }

    private fun changeDay(offset: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = sdf.parse(_uiState.value.selectedDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_MONTH, offset)
            }
            onDateSelected(sdf.format(cal.time))
        } catch (e: Exception) {
            // ignore
        }
    }
}

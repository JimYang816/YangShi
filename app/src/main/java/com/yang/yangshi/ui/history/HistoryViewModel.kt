package com.yang.yangshi.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yang.yangshi.data.repository.DietRepository
import com.yang.yangshi.domain.calculator.MacroConstants
import com.yang.yangshi.domain.calculator.MacroTarget
import com.yang.yangshi.domain.calculator.MacroTargetCalculator
import com.yang.yangshi.domain.model.MealLog
import com.yang.yangshi.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val dailyTarget: MacroTarget = MacroTarget(210.0, 140.0, 56.0, 1904.0),
    val totalConsumed: MacroTarget = MacroTarget(0.0, 0.0, 0.0, 0.0),
    val mealLogs: List<MealLog> = emptyList(),
    val expandedMealTypes: Set<MealType> = setOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val repository: DietRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistoryForDate(_uiState.value.selectedDate)
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadHistoryForDate(date)
    }

    fun loadHistoryForDate(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = repository.getUserProfileOnce()
            val dailyTarget = MacroTargetCalculator.calculateDailyTarget(profile)

            repository.getMealLogsForDate(date).collect { logs ->
                var c = 0.0
                var p = 0.0
                var f = 0.0
                logs.forEach { log ->
                    c += log.totalActualCarbs
                    p += log.totalActualProtein
                    f += log.totalActualFat
                }
                val cal = c * MacroConstants.KCAL_PER_G_CARBS + p * MacroConstants.KCAL_PER_G_PROTEIN + f * MacroConstants.KCAL_PER_G_FAT
                val totalConsumed = MacroTarget(
                    carbsGrams = MacroTargetCalculator.roundToOneDecimal(c),
                    proteinGrams = MacroTargetCalculator.roundToOneDecimal(p),
                    fatGrams = MacroTargetCalculator.roundToOneDecimal(f),
                    caloriesKcal = MacroTargetCalculator.roundToInteger(cal)
                )

                _uiState.update {
                    it.copy(
                        dailyTarget = dailyTarget,
                        totalConsumed = totalConsumed,
                        mealLogs = logs,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleMealExpanded(mealType: MealType) {
        _uiState.update { state ->
            val current = state.expandedMealTypes.toMutableSet()
            if (current.contains(mealType)) {
                current.remove(mealType)
            } else {
                current.add(mealType)
            }
            state.copy(expandedMealTypes = current)
        }
    }

    fun deleteLogItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteMealLogItem(itemId)
            loadHistoryForDate(_uiState.value.selectedDate)
        }
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

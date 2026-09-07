package com.yang.yangshi.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yang.yangshi.data.repository.DietRepository
import com.yang.yangshi.domain.calculator.MacroTarget
import com.yang.yangshi.domain.calculator.MacroTargetCalculator
import com.yang.yangshi.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val weightKgText: String = "70.0",
    val carbGPerKgText: String = "3.0",
    val proteinGPerKgText: String = "2.0",
    val fatGPerKgText: String = "0.8",
    val breakfastRatioText: String = "30",
    val lunchRatioText: String = "40",
    val dinnerRatioText: String = "30",
    val snackRatioText: String = "0",
    val dailyTargetPreview: MacroTarget = MacroTarget(210.0, 140.0, 56.0, 1904.0),
    val breakfastTargetPreview: MacroTarget = MacroTarget(63.0, 42.0, 16.8, 571.0),
    val lunchTargetPreview: MacroTarget = MacroTarget(84.0, 56.0, 22.4, 762.0),
    val dinnerTargetPreview: MacroTarget = MacroTarget(63.0, 42.0, 16.8, 571.0),
    val snackTargetPreview: MacroTarget = MacroTarget(0.0, 0.0, 0.0, 0.0),
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: DietRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            repository.getUserProfile().collect { profile ->
                _uiState.update { state ->
                    val newState = state.copy(
                        weightKgText = profile.weightKg.toString(),
                        carbGPerKgText = profile.carbGPerKg.toString(),
                        proteinGPerKgText = profile.proteinGPerKg.toString(),
                        fatGPerKgText = profile.fatGPerKg.toString(),
                        breakfastRatioText = (profile.breakfastRatio * 100).toInt().toString(),
                        lunchRatioText = (profile.lunchRatio * 100).toInt().toString(),
                        dinnerRatioText = (profile.dinnerRatio * 100).toInt().toString(),
                        snackRatioText = (profile.snackRatio * 100).toInt().toString()
                    )
                    recalculatePreviews(newState, profile)
                }
            }
        }
    }

    fun onWeightChanged(value: String) {
        _uiState.update { it.copy(weightKgText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onCarbRatioChanged(value: String) {
        _uiState.update { it.copy(carbGPerKgText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onProteinRatioChanged(value: String) {
        _uiState.update { it.copy(proteinGPerKgText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onFatRatioChanged(value: String) {
        _uiState.update { it.copy(fatGPerKgText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onBreakfastRatioChanged(value: String) {
        _uiState.update { it.copy(breakfastRatioText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onLunchRatioChanged(value: String) {
        _uiState.update { it.copy(lunchRatioText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onDinnerRatioChanged(value: String) {
        _uiState.update { it.copy(dinnerRatioText = value, saveMessage = null) }
        updatePreviews()
    }

    fun onSnackRatioChanged(value: String) {
        _uiState.update { it.copy(snackRatioText = value, saveMessage = null) }
        updatePreviews()
    }

    private fun updatePreviews() {
        val current = _uiState.value
        val weight = current.weightKgText.toDoubleOrNull() ?: 70.0
        val carb = current.carbGPerKgText.toDoubleOrNull() ?: 3.0
        val protein = current.proteinGPerKgText.toDoubleOrNull() ?: 2.0
        val fat = current.fatGPerKgText.toDoubleOrNull() ?: 0.8
        val bRatio = (current.breakfastRatioText.toDoubleOrNull() ?: 30.0) / 100.0
        val lRatio = (current.lunchRatioText.toDoubleOrNull() ?: 40.0) / 100.0
        val dRatio = (current.dinnerRatioText.toDoubleOrNull() ?: 30.0) / 100.0
        val sRatio = (current.snackRatioText.toDoubleOrNull() ?: 0.0) / 100.0

        val tempProfile = UserProfile(
            weightKg = weight,
            carbGPerKg = carb,
            proteinGPerKg = protein,
            fatGPerKg = fat,
            breakfastRatio = bRatio,
            lunchRatio = lRatio,
            dinnerRatio = dRatio,
            snackRatio = sRatio
        )
        _uiState.update { recalculatePreviews(it, tempProfile) }
    }

    private fun recalculatePreviews(state: ProfileUiState, profile: UserProfile): ProfileUiState {
        val daily = MacroTargetCalculator.calculateDailyTarget(profile)
        val bTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.breakfastRatio * 100)
        val lTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.lunchRatio * 100)
        val dTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.dinnerRatio * 100)
        val sTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.snackRatio * 100)

        return state.copy(
            dailyTargetPreview = daily,
            breakfastTargetPreview = bTarget,
            lunchTargetPreview = lTarget,
            dinnerTargetPreview = dTarget,
            snackTargetPreview = sTarget
        )
    }

    fun saveProfile() {
        val current = _uiState.value
        val weight = current.weightKgText.toDoubleOrNull()
        val carb = current.carbGPerKgText.toDoubleOrNull()
        val protein = current.proteinGPerKgText.toDoubleOrNull()
        val fat = current.fatGPerKgText.toDoubleOrNull()
        val bRatio = current.breakfastRatioText.toDoubleOrNull()
        val lRatio = current.lunchRatioText.toDoubleOrNull()
        val dRatio = current.dinnerRatioText.toDoubleOrNull()
        val sRatio = current.snackRatioText.toDoubleOrNull() ?: 0.0

        if (weight == null || weight <= 0) {
            _uiState.update { it.copy(errorMessage = "请输入有效的体重(kg)") }
            return
        }
        if (carb == null || protein == null || fat == null || carb < 0 || protein < 0 || fat < 0) {
            _uiState.update { it.copy(errorMessage = "请输入有效的碳蛋脂系数(g/kg/天)") }
            return
        }
        if (bRatio == null || lRatio == null || dRatio == null) {
            _uiState.update { it.copy(errorMessage = "请输入有效的餐别比例(%)") }
            return
        }

        val totalPct = bRatio + lRatio + dRatio + sRatio
        if (kotlin.math.abs(totalPct - 100.0) > 0.1) {
            _uiState.update { it.copy(errorMessage = "餐别比例之和必须等于 100% (当前为 ${totalPct.toInt()}%)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val profile = UserProfile(
                id = 1,
                weightKg = weight,
                carbGPerKg = carb,
                proteinGPerKg = protein,
                fatGPerKg = fat,
                breakfastRatio = bRatio / 100.0,
                lunchRatio = lRatio / 100.0,
                dinnerRatio = dRatio / 100.0,
                snackRatio = sRatio / 100.0
            )
            repository.saveUserProfile(profile)
            _uiState.update { it.copy(isSaving = false, saveMessage = "身体指标与目标系数保存成功！") }
        }
    }
}

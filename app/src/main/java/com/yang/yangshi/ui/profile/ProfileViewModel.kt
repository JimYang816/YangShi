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
import java.util.Locale

data class ProfileUiState(
    val heightCmText: String = "170.0",
    val weightKgText: String = "70.0",
    val bmiText: String = "24.2",
    val carbGPerKgText: String = "3.0",
    val proteinGPerKgText: String = "2.0",
    val fatGPerKgText: String = "0.8",
    val breakfastRatioText: String = "30",
    val lunchRatioText: String = "40",
    val dinnerRatioText: String = "30",
    val snackRatioText: String = "0",
    val isDirty: Boolean = false,
    val savedProfile: UserProfile? = null,
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
                        savedProfile = profile,
                        heightCmText = profile.heightCm.toString(),
                        weightKgText = profile.weightKg.toString(),
                        carbGPerKgText = profile.carbGPerKg.toString(),
                        proteinGPerKgText = profile.proteinGPerKg.toString(),
                        fatGPerKgText = profile.fatGPerKg.toString(),
                        breakfastRatioText = (profile.breakfastRatio * 100).toInt().toString(),
                        lunchRatioText = (profile.lunchRatio * 100).toInt().toString(),
                        dinnerRatioText = (profile.dinnerRatio * 100).toInt().toString(),
                        snackRatioText = (profile.snackRatio * 100).toInt().toString(),
                        isDirty = false
                    )
                    recalculatePreviews(newState, profile)
                }
            }
        }
    }

    fun onHeightChanged(value: String) {
        _uiState.update { it.copy(heightCmText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onWeightChanged(value: String) {
        _uiState.update { it.copy(weightKgText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onCarbRatioChanged(value: String) {
        _uiState.update { it.copy(carbGPerKgText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onProteinRatioChanged(value: String) {
        _uiState.update { it.copy(proteinGPerKgText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onFatRatioChanged(value: String) {
        _uiState.update { it.copy(fatGPerKgText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onBreakfastRatioChanged(value: String) {
        _uiState.update { it.copy(breakfastRatioText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onLunchRatioChanged(value: String) {
        _uiState.update { it.copy(lunchRatioText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onDinnerRatioChanged(value: String) {
        _uiState.update { it.copy(dinnerRatioText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun onSnackRatioChanged(value: String) {
        _uiState.update { it.copy(snackRatioText = value, saveMessage = null) }
        updatePreviewsAndDirty()
    }

    fun cancelChanges() {
        val saved = _uiState.value.savedProfile ?: return
        _uiState.update { state ->
            val newState = state.copy(
                heightCmText = saved.heightCm.toString(),
                weightKgText = saved.weightKg.toString(),
                carbGPerKgText = saved.carbGPerKg.toString(),
                proteinGPerKgText = saved.proteinGPerKg.toString(),
                fatGPerKgText = saved.fatGPerKg.toString(),
                breakfastRatioText = (saved.breakfastRatio * 100).toInt().toString(),
                lunchRatioText = (saved.lunchRatio * 100).toInt().toString(),
                dinnerRatioText = (saved.dinnerRatio * 100).toInt().toString(),
                snackRatioText = (saved.snackRatio * 100).toInt().toString(),
                isDirty = false,
                errorMessage = null,
                saveMessage = null
            )
            recalculatePreviews(newState, saved)
        }
    }

    private fun updatePreviewsAndDirty() {
        val current = _uiState.value
        val saved = current.savedProfile

        val height = current.heightCmText.toDoubleOrNull() ?: 170.0
        val weight = current.weightKgText.toDoubleOrNull() ?: 70.0
        val carb = current.carbGPerKgText.toDoubleOrNull() ?: 3.0
        val protein = current.proteinGPerKgText.toDoubleOrNull() ?: 2.0
        val fat = current.fatGPerKgText.toDoubleOrNull() ?: 0.8
        val bRatio = (current.breakfastRatioText.toDoubleOrNull() ?: 30.0) / 100.0
        val lRatio = (current.lunchRatioText.toDoubleOrNull() ?: 40.0) / 100.0
        val dRatio = (current.dinnerRatioText.toDoubleOrNull() ?: 30.0) / 100.0
        val sRatio = (current.snackRatioText.toDoubleOrNull() ?: 0.0) / 100.0

        val tempProfile = UserProfile(
            heightCm = height,
            weightKg = weight,
            carbGPerKg = carb,
            proteinGPerKg = protein,
            fatGPerKg = fat,
            breakfastRatio = bRatio,
            lunchRatio = lRatio,
            dinnerRatio = dRatio,
            snackRatio = sRatio
        )

        val dirty = if (saved == null) true else {
            current.heightCmText != saved.heightCm.toString() ||
            current.weightKgText != saved.weightKg.toString() ||
            current.carbGPerKgText != saved.carbGPerKg.toString() ||
            current.proteinGPerKgText != saved.proteinGPerKg.toString() ||
            current.fatGPerKgText != saved.fatGPerKg.toString() ||
            current.breakfastRatioText != (saved.breakfastRatio * 100).toInt().toString() ||
            current.lunchRatioText != (saved.lunchRatio * 100).toInt().toString() ||
            current.dinnerRatioText != (saved.dinnerRatio * 100).toInt().toString() ||
            current.snackRatioText != (saved.snackRatio * 100).toInt().toString()
        }

        _uiState.update { recalculatePreviews(it.copy(isDirty = dirty), tempProfile) }
    }

    private fun recalculatePreviews(state: ProfileUiState, profile: UserProfile): ProfileUiState {
        val daily = MacroTargetCalculator.calculateDailyTarget(profile)
        val bTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.breakfastRatio * 100)
        val lTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.lunchRatio * 100)
        val dTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.dinnerRatio * 100)
        val sTarget = MacroTargetCalculator.calculateMealTarget(daily, profile.snackRatio * 100)

        val bmiVal = profile.bmi
        val formattedBmi = String.format(Locale.US, "%.1f", bmiVal)

        return state.copy(
            bmiText = formattedBmi,
            dailyTargetPreview = daily,
            breakfastTargetPreview = bTarget,
            lunchTargetPreview = lTarget,
            dinnerTargetPreview = dTarget,
            snackTargetPreview = sTarget
        )
    }

    fun saveProfile() {
        val current = _uiState.value
        val height = current.heightCmText.toDoubleOrNull()
        val weight = current.weightKgText.toDoubleOrNull()
        val carb = current.carbGPerKgText.toDoubleOrNull()
        val protein = current.proteinGPerKgText.toDoubleOrNull()
        val fat = current.fatGPerKgText.toDoubleOrNull()
        val bRatio = current.breakfastRatioText.toDoubleOrNull()
        val lRatio = current.lunchRatioText.toDoubleOrNull()
        val dRatio = current.dinnerRatioText.toDoubleOrNull()
        val sRatio = current.snackRatioText.toDoubleOrNull() ?: 0.0

        if (height == null || height <= 0) {
            _uiState.update { it.copy(errorMessage = "请输入有效的身高(cm)") }
            return
        }
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
                heightCm = height,
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
            _uiState.update {
                it.copy(
                    savedProfile = profile,
                    isDirty = false,
                    isSaving = false,
                    saveMessage = "身体指标与目标系数保存成功！"
                )
            }
        }
    }
}

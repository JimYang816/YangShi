package com.yang.yangshi.domain.calculator

import com.yang.yangshi.domain.model.UserProfile
import kotlin.math.round

/**
 * 营养素热量系数常数 (Atwater Factors)
 */
object MacroConstants {
    const val KCAL_PER_G_CARBS = 4.0
    const val KCAL_PER_G_PROTEIN = 4.0
    const val KCAL_PER_G_FAT = 9.0
}

/**
 * 营养素类型
 */
enum class MacroType(val displayName: String) {
    CARBS("碳水化合物"),
    PROTEIN("蛋白质"),
    FAT("脂肪"),
    CALORIES("总热量")
}

/**
 * 营养目标结果 (绝对克数与热量)
 */
data class MacroTarget(
    val carbsGrams: Double,
    val proteinGrams: Double,
    val fatGrams: Double,
    val caloriesKcal: Double = carbsGrams * MacroConstants.KCAL_PER_G_CARBS +
            proteinGrams * MacroConstants.KCAL_PER_G_PROTEIN +
            fatGrams * MacroConstants.KCAL_PER_G_FAT
)

/**
 * 营养目标计算器
 */
object MacroTargetCalculator {

    /**
     * 根据 UserProfile 计算每日营养目标
     */
    fun calculateDailyTarget(userProfile: UserProfile): MacroTarget {
        val carbs = userProfile.dailyCarbsG
        val protein = userProfile.dailyProteinG
        val fat = userProfile.dailyFatG
        val calories = userProfile.dailyEnergyKcal

        return MacroTarget(
            carbsGrams = roundToOneDecimal(carbs),
            proteinGrams = roundToOneDecimal(protein),
            fatGrams = roundToOneDecimal(fat),
            caloriesKcal = roundToInteger(calories)
        )
    }

    /**
     * 计算特定餐别目标
     */
    fun calculateMealTarget(dailyTarget: MacroTarget, mealPercentage: Double): MacroTarget {
        require(mealPercentage in 0.0..100.0) { "Meal percentage must be between 0 and 100" }
        val factor = mealPercentage / 100.0

        val carbs = dailyTarget.carbsGrams * factor
        val protein = dailyTarget.proteinGrams * factor
        val fat = dailyTarget.fatGrams * factor
        val calories = dailyTarget.caloriesKcal * factor

        return MacroTarget(
            carbsGrams = roundToOneDecimal(carbs),
            proteinGrams = roundToOneDecimal(protein),
            fatGrams = roundToOneDecimal(fat),
            caloriesKcal = roundToInteger(calories)
        )
    }

    fun roundToOneDecimal(value: Double): Double =
        round(value * 10.0) / 10.0

    fun roundToInteger(value: Double): Double =
        round(value)
}

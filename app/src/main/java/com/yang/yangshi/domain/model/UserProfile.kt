package com.yang.yangshi.domain.model

/**
 * 用户配置领域模型
 */
data class UserProfile(
    val id: Int = 1,
    val weightKg: Double = 70.0,
    val carbGPerKg: Double = 3.0,
    val proteinGPerKg: Double = 2.0,
    val fatGPerKg: Double = 0.8,
    val breakfastRatio: Double = 0.30,
    val lunchRatio: Double = 0.40,
    val dinnerRatio: Double = 0.30,
    val snackRatio: Double = 0.0
) {
    /**
     * 每日总碳水化合物目标 (g)
     */
    val dailyCarbsG: Double
        get() = weightKg * carbGPerKg

    /**
     * 每日总蛋白质目标 (g)
     */
    val dailyProteinG: Double
        get() = weightKg * proteinGPerKg

    /**
     * 每日总脂肪目标 (g)
     */
    val dailyFatG: Double
        get() = weightKg * fatGPerKg

    /**
     * 每日总能量目标 (kcal)
     */
    val dailyEnergyKcal: Double
        get() = dailyCarbsG * 4.0 + dailyProteinG * 4.0 + dailyFatG * 9.0
}

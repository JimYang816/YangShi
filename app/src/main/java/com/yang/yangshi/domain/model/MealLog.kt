package com.yang.yangshi.domain.model

/**
 * 餐别记录领域模型 (对应某一天某一餐的日志与食物列表)
 */
data class MealLog(
    val id: Long = 0L,
    val date: String,             // 格式: YYYY-MM-DD
    val mealType: MealType,       // BREAKFAST | LUNCH | DINNER | SNACK
    val targetCarbs: Double,      // 目标碳水 (g)
    val targetProtein: Double,    // 目标蛋白质 (g)
    val targetFat: Double,        // 目标脂肪 (g)
    val targetEnergy: Double,     // 目标热量 (kcal)
    val items: List<MealLogItem> = emptyList()
) {
    /**
     * 实际总碳水 (g)
     */
    val totalActualCarbs: Double
        get() = items.sumOf { it.actualCarbs }

    /**
     * 实际总蛋白质 (g)
     */
    val totalActualProtein: Double
        get() = items.sumOf { it.actualProtein }

    /**
     * 实际总脂肪 (g)
     */
    val totalActualFat: Double
        get() = items.sumOf { it.actualFat }

    /**
     * 实际总热量 (kcal)
     */
    val totalActualEnergy: Double
        get() = items.sumOf { it.actualEnergy }
}

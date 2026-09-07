package com.yang.yangshi.domain.model

/**
 * 餐别记录项领域模型 (记录具体的食物与克数)
 */
data class MealLogItem(
    val id: Long = 0L,
    val mealLogId: Long = 0L,
    val foodItemId: Long? = null,
    val foodName: String,
    val foodWeightG: Double,
    val actualCarbs: Double,
    val actualProtein: Double,
    val actualFat: Double,
    val actualEnergy: Double,
    val isLocked: Boolean = false
)

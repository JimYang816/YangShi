package com.yang.yangshi.domain.model

/**
 * 食物领域模型
 * 所有营养成分基准均为每 100g 可食部
 */
data class FoodItem(
    val id: Long = 0L,
    val remoteFoodId: Long? = null,
    val name: String,
    val edibleRatio: Double = 1.0, // 可食部比例 (0.0 ~ 1.0, 如 0.76 表示 76%)
    val energyKcal: Double,        // 每 100g 能量 (千卡)
    val proteinG: Double,          // 每 100g 蛋白质 (g)
    val fatG: Double,              // 每 100g 脂肪 (g)
    val carbsG: Double,            // 每 100g 碳水化合物 (g)
    val isCustom: Boolean = false, // 是否为自定义食物
    val isFavorite: Boolean = false// 是否收藏
)

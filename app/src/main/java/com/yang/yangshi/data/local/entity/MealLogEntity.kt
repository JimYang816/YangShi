package com.yang.yangshi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yang.yangshi.domain.model.MealType

/**
 * 特定日期与餐别日志主表实体
 */
@Entity(
    tableName = "meal_logs",
    indices = [
        Index(value = ["date", "mealType"], unique = true),
        Index(value = ["date"])
    ]
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String,             // 格式: YYYY-MM-DD
    val mealType: MealType,       // BREAKFAST | LUNCH | DINNER | SNACK
    val targetCarbs: Double,      // 该餐目标碳水 (g)
    val targetProtein: Double,    // 该餐目标蛋白质 (g)
    val targetFat: Double,        // 该餐目标脂肪 (g)
    val targetEnergy: Double      // 该餐目标热量 (kcal)
)

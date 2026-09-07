package com.yang.yangshi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 餐别打卡日志中的食物条目实体
 */
@Entity(
    tableName = "meal_log_items",
    foreignKeys = [
        ForeignKey(
            entity = MealLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealLogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["mealLogId"]),
        Index(value = ["foodItemId"])
    ]
)
data class MealLogItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mealLogId: Long,
    val foodItemId: Long?,
    val foodName: String,
    val foodWeightG: Double,
    val actualCarbs: Double,
    val actualProtein: Double,
    val actualFat: Double,
    val actualEnergy: Double,
    val isLocked: Boolean = false
)

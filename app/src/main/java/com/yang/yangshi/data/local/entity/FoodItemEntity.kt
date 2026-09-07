package com.yang.yangshi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 食物成分数据库实体
 * 基础基准：每 100g 可食部
 */
@Entity(
    tableName = "food_items",
    indices = [
        Index(value = ["remoteFoodId"]),
        Index(value = ["name"]),
        Index(value = ["isFavorite"])
    ]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val remoteFoodId: Long? = null,
    val name: String,
    val edibleRatio: Double = 1.0,
    val energyKcal: Double,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)

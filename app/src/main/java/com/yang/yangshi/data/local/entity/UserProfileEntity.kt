package com.yang.yangshi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户身体数据与营养配比数据库实体
 * 单用户方案：固定 id 为 1
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val weightKg: Double,
    val heightCm: Double = 170.0,
    val carbGPerKg: Double,
    val proteinGPerKg: Double,
    val fatGPerKg: Double,
    val breakfastRatio: Double,
    val lunchRatio: Double,
    val dinnerRatio: Double,
    val snackRatio: Double
)

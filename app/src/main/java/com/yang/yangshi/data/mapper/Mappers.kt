package com.yang.yangshi.data.mapper

import com.yang.yangshi.data.local.entity.FoodItemEntity
import com.yang.yangshi.data.local.entity.MealLogEntity
import com.yang.yangshi.data.local.entity.MealLogItemEntity
import com.yang.yangshi.data.local.entity.MealLogWithItems
import com.yang.yangshi.data.local.entity.UserProfileEntity
import com.yang.yangshi.domain.model.FoodItem
import com.yang.yangshi.domain.model.MealLog
import com.yang.yangshi.domain.model.MealLogItem
import com.yang.yangshi.domain.model.UserProfile

/**
 * 数据库 Entity 与 Domain Model 转换映射器
 */

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        weightKg = weightKg,
        carbGPerKg = carbGPerKg,
        proteinGPerKg = proteinGPerKg,
        fatGPerKg = fatGPerKg,
        breakfastRatio = breakfastRatio,
        lunchRatio = lunchRatio,
        dinnerRatio = dinnerRatio,
        snackRatio = snackRatio
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        weightKg = weightKg,
        carbGPerKg = carbGPerKg,
        proteinGPerKg = proteinGPerKg,
        fatGPerKg = fatGPerKg,
        breakfastRatio = breakfastRatio,
        lunchRatio = lunchRatio,
        dinnerRatio = dinnerRatio,
        snackRatio = snackRatio
    )
}

fun FoodItemEntity.toDomain(): FoodItem {
    return FoodItem(
        id = id,
        remoteFoodId = remoteFoodId,
        name = name,
        edibleRatio = edibleRatio,
        energyKcal = energyKcal,
        proteinG = proteinG,
        fatG = fatG,
        carbsG = carbsG,
        isCustom = isCustom,
        isFavorite = isFavorite
    )
}

fun FoodItem.toEntity(): FoodItemEntity {
    return FoodItemEntity(
        id = id,
        remoteFoodId = remoteFoodId,
        name = name,
        edibleRatio = edibleRatio,
        energyKcal = energyKcal,
        proteinG = proteinG,
        fatG = fatG,
        carbsG = carbsG,
        isCustom = isCustom,
        isFavorite = isFavorite
    )
}

fun MealLogItemEntity.toDomain(): MealLogItem {
    return MealLogItem(
        id = id,
        mealLogId = mealLogId,
        foodItemId = foodItemId,
        foodName = foodName,
        foodWeightG = foodWeightG,
        actualCarbs = actualCarbs,
        actualProtein = actualProtein,
        actualFat = actualFat,
        actualEnergy = actualEnergy,
        isLocked = isLocked
    )
}

fun MealLogItem.toEntity(parentMealLogId: Long = mealLogId): MealLogItemEntity {
    return MealLogItemEntity(
        id = id,
        mealLogId = parentMealLogId,
        foodItemId = foodItemId,
        foodName = foodName,
        foodWeightG = foodWeightG,
        actualCarbs = actualCarbs,
        actualProtein = actualProtein,
        actualFat = actualFat,
        actualEnergy = actualEnergy,
        isLocked = isLocked
    )
}

fun MealLogWithItems.toDomain(): MealLog {
    return MealLog(
        id = mealLog.id,
        date = mealLog.date,
        mealType = mealLog.mealType,
        targetCarbs = mealLog.targetCarbs,
        targetProtein = mealLog.targetProtein,
        targetFat = mealLog.targetFat,
        targetEnergy = mealLog.targetEnergy,
        items = items.map { it.toDomain() }
    )
}

fun MealLog.toEntity(): MealLogEntity {
    return MealLogEntity(
        id = id,
        date = date,
        mealType = mealType,
        targetCarbs = targetCarbs,
        targetProtein = targetProtein,
        targetFat = targetFat,
        targetEnergy = targetEnergy
    )
}

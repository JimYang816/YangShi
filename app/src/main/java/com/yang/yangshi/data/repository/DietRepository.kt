package com.yang.yangshi.data.repository

import com.yang.yangshi.data.local.DietDatabase
import com.yang.yangshi.data.local.entity.MealLogEntity
import com.yang.yangshi.data.local.entity.MealLogItemEntity
import com.yang.yangshi.data.mapper.toDomain
import com.yang.yangshi.data.mapper.toEntity
import com.yang.yangshi.domain.calculator.MacroTarget
import com.yang.yangshi.domain.model.FoodItem
import com.yang.yangshi.domain.model.MealLog
import com.yang.yangshi.domain.model.MealLogItem
import com.yang.yangshi.domain.model.MealType
import com.yang.yangshi.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DietRepository(
    private val database: DietDatabase
) {
    private val userProfileDao = database.userProfileDao()
    private val foodDao = database.foodDao()
    private val mealLogDao = database.mealLogDao()

    // --- User Profile ---

    fun getUserProfile(): Flow<UserProfile> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.toDomain() ?: UserProfile()
        }
    }

    suspend fun getUserProfileOnce(): UserProfile {
        return userProfileDao.getUserProfileOnce()?.toDomain() ?: UserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.upsertUserProfile(profile.toEntity())
    }

    // --- Food Items ---

    fun searchFoodsLocal(query: String): Flow<List<FoodItem>> {
        return foodDao.searchFoodsByName(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getFavoriteFoods(): Flow<List<FoodItem>> {
        return foodDao.getFavoriteFoods().map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * 模拟网络抓取/API 搜索中国食物成分表 (https://nlc.chinanutri.cn/fq/)
     */
    suspend fun searchChinaNutriFoods(query: String): List<FoodItem> {
        if (query.isBlank()) return emptyList()
        // 预设核心食材库与关键词匹配
        val sampleDatabase = listOf(
            FoodItem(id = 101, remoteFoodId = 613, name = "苹果(均值)", edibleRatio = 0.76, energyKcal = 54.7, proteinG = 0.2, fatG = 0.2, carbsG = 13.5),
            FoodItem(id = 102, remoteFoodId = 701, name = "米饭(蒸)", edibleRatio = 1.0, energyKcal = 116.0, proteinG = 2.6, fatG = 0.3, carbsG = 25.9),
            FoodItem(id = 103, remoteFoodId = 802, name = "鸡胸肉(生)", edibleRatio = 1.0, energyKcal = 133.0, proteinG = 19.4, fatG = 5.0, carbsG = 2.5),
            FoodItem(id = 104, remoteFoodId = 803, name = "西兰花(熟)", edibleRatio = 1.0, energyKcal = 27.0, proteinG = 2.5, fatG = 0.6, carbsG = 4.3),
            FoodItem(id = 105, remoteFoodId = 901, name = "鸡蛋(煮)", edibleRatio = 0.88, energyKcal = 144.0, proteinG = 13.3, fatG = 8.8, carbsG = 2.8),
            FoodItem(id = 106, remoteFoodId = 902, name = "牛奶(全脂)", edibleRatio = 1.0, energyKcal = 54.0, proteinG = 3.0, fatG = 3.2, carbsG = 3.4),
            FoodItem(id = 107, remoteFoodId = 903, name = "燕麦片", edibleRatio = 1.0, energyKcal = 367.0, proteinG = 15.0, fatG = 6.7, carbsG = 61.6),
            FoodItem(id = 108, remoteFoodId = 904, name = "牛排(瘦肉)", edibleRatio = 1.0, energyKcal = 142.0, proteinG = 20.2, fatG = 6.8, carbsG = 0.0),
            FoodItem(id = 109, remoteFoodId = 905, name = "全麦面包", edibleRatio = 1.0, energyKcal = 246.0, proteinG = 8.5, fatG = 3.2, carbsG = 45.8),
            FoodItem(id = 110, remoteFoodId = 906, name = "红薯(蒸)", edibleRatio = 0.90, energyKcal = 86.0, proteinG = 1.6, fatG = 0.2, carbsG = 20.1)
        )
        val matches = sampleDatabase.filter { it.name.contains(query, ignoreCase = true) }
        // 自动缓存入库
        matches.forEach { foodDao.insertFood(it.toEntity()) }
        return matches.ifEmpty {
            listOf(
                FoodItem(
                    id = System.currentTimeMillis(),
                    name = "$query(自定义/查无精准)",
                    edibleRatio = 1.0,
                    energyKcal = 100.0,
                    proteinG = 5.0,
                    fatG = 2.0,
                    carbsG = 15.0,
                    isCustom = true
                )
            )
        }
    }

    suspend fun saveFoodItem(food: FoodItem): Long {
        return foodDao.insertFood(food.toEntity())
    }

    suspend fun setFoodFavorite(foodId: Long, isFavorite: Boolean) {
        foodDao.setFavorite(foodId, isFavorite)
    }

    // --- Meal Logs ---

    fun getMealLogsForDate(date: String): Flow<List<MealLog>> {
        return mealLogDao.getMealLogsWithItemsForDate(date).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getMealLog(date: String, mealType: MealType): Flow<MealLog?> {
        return mealLogDao.getMealLogWithItems(date, mealType).map { it?.toDomain() }
    }

    suspend fun saveMealLogWithItems(
        date: String,
        mealType: MealType,
        target: MacroTarget,
        items: List<MealLogItem>
    ) {
        val existingEntity = mealLogDao.getMealLogEntity(date, mealType)
        val mealLogId = if (existingEntity != null) {
            val updated = existingEntity.copy(
                targetCarbs = target.carbsGrams,
                targetProtein = target.proteinGrams,
                targetFat = target.fatGrams,
                targetEnergy = target.caloriesKcal
            )
            mealLogDao.upsertMealLog(updated)
            existingEntity.id
        } else {
            val newEntity = MealLogEntity(
                date = date,
                mealType = mealType,
                targetCarbs = target.carbsGrams,
                targetProtein = target.proteinGrams,
                targetFat = target.fatGrams,
                targetEnergy = target.caloriesKcal
            )
            mealLogDao.insertMealLog(newEntity)
        }

        val itemEntities = items.map { domainItem ->
            MealLogItemEntity(
                id = domainItem.id,
                mealLogId = mealLogId,
                foodItemId = domainItem.foodItemId,
                foodName = domainItem.foodName,
                foodWeightG = domainItem.foodWeightG,
                actualCarbs = domainItem.actualCarbs,
                actualProtein = domainItem.actualProtein,
                actualFat = domainItem.actualFat,
                actualEnergy = domainItem.actualEnergy,
                isLocked = domainItem.isLocked
            )
        }
        mealLogDao.replaceMealLogItems(mealLogId, itemEntities)
    }

    suspend fun deleteMealLogItem(itemId: Long) {
        mealLogDao.deleteMealLogItemById(itemId)
    }
}

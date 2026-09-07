package com.yang.yangshi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yang.yangshi.data.local.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * 食物库 DAO 数据访问接口
 */
@Dao
interface FoodDao {

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY isFavorite DESC, id DESC")
    fun searchFoodsByName(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteFoods(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: Long): FoodItemEntity?

    @Query("SELECT * FROM food_items WHERE remoteFoodId = :remoteFoodId LIMIT 1")
    suspend fun getFoodByRemoteId(remoteFoodId: Long): FoodItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodItemEntity>): List<Long>

    @Update
    suspend fun updateFood(food: FoodItemEntity)

    @Delete
    suspend fun deleteFood(food: FoodItemEntity)

    @Query("UPDATE food_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}

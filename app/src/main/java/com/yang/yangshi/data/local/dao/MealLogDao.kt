package com.yang.yangshi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.yang.yangshi.data.local.entity.MealLogEntity
import com.yang.yangshi.data.local.entity.MealLogItemEntity
import com.yang.yangshi.data.local.entity.MealLogWithItems
import com.yang.yangshi.domain.model.MealType
import kotlinx.coroutines.flow.Flow

/**
 * 餐别打卡日志 DAO 数据访问接口
 */
@Dao
interface MealLogDao {

    @Transaction
    @Query("SELECT * FROM meal_logs WHERE date = :date AND mealType = :mealType LIMIT 1")
    fun getMealLogWithItems(date: String, mealType: MealType): Flow<MealLogWithItems?>

    @Transaction
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY mealType ASC")
    fun getMealLogsWithItemsForDate(date: String): Flow<List<MealLogWithItems>>

    @Query("SELECT * FROM meal_logs WHERE date = :date AND mealType = :mealType LIMIT 1")
    suspend fun getMealLogEntity(date: String, mealType: MealType): MealLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLogEntity): Long

    @Upsert
    suspend fun upsertMealLog(mealLog: MealLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLogItem(item: MealLogItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLogItems(items: List<MealLogItemEntity>): List<Long>

    @Update
    suspend fun updateMealLogItem(item: MealLogItemEntity)

    @Delete
    suspend fun deleteMealLogItem(item: MealLogItemEntity)

    @Query("DELETE FROM meal_log_items WHERE id = :itemId")
    suspend fun deleteMealLogItemById(itemId: Long)

    @Query("DELETE FROM meal_log_items WHERE mealLogId = :mealLogId")
    suspend fun clearMealLogItems(mealLogId: Long)

    /**
     * 事务操作：全量替换特定 MealLog 的食物明细项
     */
    @Transaction
    suspend fun replaceMealLogItems(mealLogId: Long, items: List<MealLogItemEntity>) {
        clearMealLogItems(mealLogId)
        val itemsWithLogId = items.map { it.copy(mealLogId = mealLogId) }
        insertMealLogItems(itemsWithLogId)
    }
}

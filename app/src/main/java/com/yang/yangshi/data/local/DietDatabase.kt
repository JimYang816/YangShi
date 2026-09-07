package com.yang.yangshi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yang.yangshi.data.local.converter.Converters
import com.yang.yangshi.data.local.dao.FoodDao
import com.yang.yangshi.data.local.dao.MealLogDao
import com.yang.yangshi.data.local.dao.UserProfileDao
import com.yang.yangshi.data.local.entity.FoodItemEntity
import com.yang.yangshi.data.local.entity.MealLogEntity
import com.yang.yangshi.data.local.entity.MealLogItemEntity
import com.yang.yangshi.data.local.entity.UserProfileEntity

/**
 * Diet App 核心数据库定义
 */
@Database(
    entities = [
        UserProfileEntity::class,
        FoodItemEntity::class,
        MealLogEntity::class,
        MealLogItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DietDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodDao(): FoodDao
    abstract fun mealLogDao(): MealLogDao

    companion object {
        const val DATABASE_NAME = "diet_tracker.db"

        @Volatile
        private var INSTANCE: DietDatabase? = null

        fun getInstance(context: Context): DietDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DietDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

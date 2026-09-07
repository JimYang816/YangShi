package com.yang.yangshi.data.local.converter

import androidx.room.TypeConverter
import com.yang.yangshi.domain.model.MealType

/**
 * Room 数据库类型转换器
 */
class Converters {

    @TypeConverter
    fun fromMealType(mealType: MealType?): String? {
        return mealType?.name
    }

    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let {
            try {
                MealType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                MealType.BREAKFAST
            }
        }
    }
}

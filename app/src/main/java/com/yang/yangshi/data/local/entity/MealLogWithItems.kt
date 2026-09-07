package com.yang.yangshi.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 餐别日志与其所有已添加食物条目的 1:N 一对多关联类
 */
data class MealLogWithItems(
    @Embedded
    val mealLog: MealLogEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "mealLogId"
    )
    val items: List<MealLogItemEntity>
)

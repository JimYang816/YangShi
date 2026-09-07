package com.yang.yangshi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yang.yangshi.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户配置 DAO 数据访问接口
 */
@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Upsert
    suspend fun upsertUserProfile(userProfile: UserProfileEntity)
}

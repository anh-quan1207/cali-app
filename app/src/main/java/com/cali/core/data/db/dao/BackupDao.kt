package com.cali.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

/** Đọc/ghi toàn bộ bảng cho sao lưu (04 §5) và Xoá toàn bộ (S45). Gọi trong transaction. */
@Dao
interface BackupDao {
    @Query("SELECT * FROM user_profile") suspend fun profiles(): List<UserProfileEntity>
    @Query("SELECT * FROM goal_snapshot ORDER BY effective_from") suspend fun goals(): List<GoalSnapshotEntity>
    @Query("SELECT * FROM weight_entry ORDER BY date") suspend fun weights(): List<WeightEntryEntity>
    @Query("SELECT * FROM food_entry ORDER BY id") suspend fun foods(): List<FoodEntryEntity>
    @Query("SELECT * FROM my_food ORDER BY id") suspend fun myFoods(): List<MyFoodEntity>
    @Query("SELECT * FROM water_entry ORDER BY id") suspend fun waters(): List<WaterEntryEntity>
    @Query("SELECT * FROM chat_message ORDER BY id") suspend fun chats(): List<ChatMessageEntity>

    @Insert suspend fun insertProfiles(items: List<UserProfileEntity>)
    @Insert suspend fun insertGoals(items: List<GoalSnapshotEntity>)
    @Insert suspend fun insertWeights(items: List<WeightEntryEntity>)
    @Insert suspend fun insertFoods(items: List<FoodEntryEntity>)
    @Insert suspend fun insertMyFoods(items: List<MyFoodEntity>)
    @Insert suspend fun insertWaters(items: List<WaterEntryEntity>)
    @Insert suspend fun insertChats(items: List<ChatMessageEntity>)

    @Query("DELETE FROM food_entry") suspend fun clearFoods()
    @Query("DELETE FROM chat_message") suspend fun clearChats()
    @Query("DELETE FROM weekly_review") suspend fun clearReviews()
    @Query("DELETE FROM ai_call_log") suspend fun clearAiCalls()
    @Query("DELETE FROM my_food") suspend fun clearMyFoods()
    @Query("DELETE FROM water_entry") suspend fun clearWaters()
    @Query("DELETE FROM weight_entry") suspend fun clearWeights()
    @Query("DELETE FROM goal_snapshot") suspend fun clearGoals()
    @Query("DELETE FROM user_profile") suspend fun clearProfiles()

    @Query("SELECT COUNT(DISTINCT date) FROM food_entry") fun observeDaysLogged(): Flow<Int>
    @Query("SELECT COUNT(*) FROM weight_entry") fun observeWeightCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM my_food") fun observeMyFoodCount(): Flow<Int>
}

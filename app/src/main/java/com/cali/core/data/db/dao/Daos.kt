package com.cali.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.cali.core.data.db.entity.AiCallLogEntity
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WeeklyReviewEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// DAO tối thiểu cho GĐ1; truy vấn tổng hợp (04 §4) thêm ở GĐ3/GĐ6.

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observe(): Flow<UserProfileEntity?>

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)
}

@Dao
interface GoalSnapshotDao {
    /** Ghi đè snapshot cùng ngày (unique effective_from). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(snapshot: GoalSnapshotEntity): Long

    /** Mục tiêu của ngày D = snapshot có effective_from lớn nhất ≤ D. */
    @Query("SELECT * FROM goal_snapshot WHERE effective_from <= :date ORDER BY effective_from DESC LIMIT 1")
    suspend fun forDate(date: LocalDate): GoalSnapshotEntity?

    @Query("SELECT * FROM goal_snapshot WHERE effective_from <= :date ORDER BY effective_from DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<GoalSnapshotEntity?>
}

@Dao
interface WeightEntryDao {
    /** Một bản ghi mỗi ngày (unique date). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: WeightEntryEntity): Long

    @Query("SELECT * FROM weight_entry ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM weight_entry WHERE date BETWEEN :from AND :to ORDER BY date")
    suspend fun between(from: LocalDate, to: LocalDate): List<WeightEntryEntity>

    @Delete
    suspend fun delete(entry: WeightEntryEntity)
}

@Dao
interface FoodEntryDao {
    @Insert
    suspend fun insert(entry: FoodEntryEntity): Long

    @Insert
    suspend fun insertAll(entries: List<FoodEntryEntity>): List<Long>

    @Update
    suspend fun update(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query("SELECT * FROM food_entry WHERE id = :id")
    suspend fun byId(id: Long): FoodEntryEntity?

    /** Món của một ngày theo giờ ăn (05 §4.4). */
    @Query("SELECT * FROM food_entry WHERE date = :date ORDER BY eaten_at, sort_order, id")
    fun observeByDate(date: LocalDate): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entry WHERE date BETWEEN :from AND :to ORDER BY eaten_at, sort_order, id")
    suspend fun between(from: LocalDate, to: LocalDate): List<FoodEntryEntity>
}

@Dao
interface MyFoodDao {
    @Insert
    suspend fun insert(food: MyFoodEntity): Long

    @Update
    suspend fun update(food: MyFoodEntity)

    @Delete
    suspend fun delete(food: MyFoodEntity)

    /** [normalizedQuery] đã qua TextNormalizer. */
    @Query(
        "SELECT * FROM my_food WHERE name_normalized LIKE '%' || :normalizedQuery || '%' " +
            "ORDER BY is_favorite DESC, use_count DESC, name",
    )
    suspend fun search(normalizedQuery: String): List<MyFoodEntity>

    @Query("SELECT * FROM my_food ORDER BY use_count DESC, last_used_at DESC LIMIT :limit")
    suspend fun top(limit: Int): List<MyFoodEntity>

    @Query("UPDATE my_food SET use_count = use_count + 1, last_used_at = :at, updated_at = :at WHERE id = :id")
    suspend fun markUsed(id: Long, at: Long)
}

@Dao
interface WaterEntryDao {
    @Insert
    suspend fun insert(entry: WaterEntryEntity): Long

    @Delete
    suspend fun delete(entry: WaterEntryEntity)

    @Query("SELECT * FROM water_entry WHERE date = :date ORDER BY created_at")
    fun observeByDate(date: LocalDate): Flow<List<WaterEntryEntity>>

    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_entry WHERE date = :date")
    fun observeTotal(date: LocalDate): Flow<Int>
}

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity): Long

    @Update
    suspend fun update(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE date = :date ORDER BY created_at, id")
    fun observeByDate(date: LocalDate): Flow<List<ChatMessageEntity>>
}

@Dao
interface AiCallLogDao {
    @Insert
    suspend fun insert(log: AiCallLogEntity): Long
}

@Dao
interface WeeklyReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(review: WeeklyReviewEntity): Long

    @Query("SELECT * FROM weekly_review WHERE week_start = :weekStart")
    suspend fun byWeek(weekStart: LocalDate): WeeklyReviewEntity?
}

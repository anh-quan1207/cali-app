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

    /**
     * GĐ6 (09 §8 G6): mục tiêu dùng cho ngày D = snapshot hiệu lực (lớn nhất ≤ D);
     * ngày trước mọi snapshot (món nhập từ sao lưu, ghi lùi ngày) lấy snapshot sớm nhất.
     */
    @Query(
        "SELECT * FROM goal_snapshot ORDER BY (effective_from <= :date) DESC, " +
            "CASE WHEN effective_from <= :date THEN effective_from END DESC, effective_from ASC LIMIT 1",
    )
    fun observeEffective(date: LocalDate): Flow<GoalSnapshotEntity?>

    // GĐ6: mục tiêu từng ngày của một khoảng (S30–S35).
    @Query("SELECT * FROM goal_snapshot ORDER BY effective_from")
    suspend fun all(): List<GoalSnapshotEntity>

    @Query("SELECT * FROM goal_snapshot ORDER BY effective_from")
    fun observeAll(): Flow<List<GoalSnapshotEntity>>
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

    @Query("SELECT * FROM weight_entry ORDER BY date DESC")
    fun observeAll(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entry WHERE date = :date")
    suspend fun byDate(date: LocalDate): WeightEntryEntity?

    /** Lần cân gần nhất trước ngày [date] (không gồm ngày đó). */
    @Query("SELECT * FROM weight_entry WHERE date < :date ORDER BY date DESC LIMIT 1")
    suspend fun latestBefore(date: LocalDate): WeightEntryEntity?

    @Insert
    suspend fun insert(entry: WeightEntryEntity): Long

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

    // GĐ6
    @Query("SELECT * FROM food_entry WHERE date BETWEEN :from AND :to ORDER BY eaten_at, sort_order, id")
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<FoodEntryEntity>>

    @Query("SELECT MIN(date) FROM food_entry")
    suspend fun firstDate(): LocalDate?

    @Query("SELECT MIN(date) FROM food_entry")
    fun observeFirstDate(): Flow<LocalDate?>

    /** Món ghi gần đây cho tab Gần đây (S13). */
    @Query("SELECT * FROM food_entry ORDER BY eaten_at DESC, id DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<FoodEntryEntity>

    @Query("SELECT COUNT(*) FROM food_entry WHERE my_food_id = :myFoodId")
    suspend fun countForMyFood(myFoodId: Long): Int

    @Query("SELECT id FROM food_entry WHERE my_food_id = :myFoodId")
    suspend fun idsForMyFood(myFoodId: Long): List<Long>

    @Query("UPDATE food_entry SET my_food_id = :myFoodId WHERE id IN (:ids)")
    suspend fun relinkMyFood(myFoodId: Long, ids: List<Long>)
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

    @Query("SELECT * FROM my_food WHERE id = :id")
    suspend fun byId(id: Long): MyFoodEntity?

    /** S34: yêu thích ghim đầu, rồi Hay dùng. */
    @Query("SELECT * FROM my_food ORDER BY is_favorite DESC, use_count DESC, last_used_at DESC, name")
    fun observeAll(): Flow<List<MyFoodEntity>>

    @Query("SELECT * FROM my_food WHERE is_favorite = 1 ORDER BY use_count DESC, name")
    fun observeFavorites(): Flow<List<MyFoodEntity>>

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

    // GĐ5: sửa lần uống (S15), lần uống gần nhất cho lịch nhắc (06 §4), cột tuần của S15.
    @Update
    suspend fun update(entry: WaterEntryEntity)

    @Query("SELECT * FROM water_entry WHERE id = :id")
    suspend fun byId(id: Long): WaterEntryEntity?

    @Query("SELECT * FROM water_entry ORDER BY created_at DESC LIMIT 1")
    suspend fun latest(): WaterEntryEntity?

    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_entry WHERE date = :date")
    suspend fun total(date: LocalDate): Int

    @Query("SELECT date, SUM(amount_ml) AS total FROM water_entry WHERE date BETWEEN :from AND :to GROUP BY date")
    fun observeTotalsBetween(from: LocalDate, to: LocalDate): Flow<List<WaterDayTotal>>

    @Query("SELECT date, SUM(amount_ml) AS total FROM water_entry WHERE date BETWEEN :from AND :to GROUP BY date")
    suspend fun totalsBetween(from: LocalDate, to: LocalDate): List<WaterDayTotal>
}

/** Tổng nước một ngày (cột tuần S15). */
data class WaterDayTotal(val date: LocalDate, val total: Int)

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity): Long

    @Update
    suspend fun update(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE date = :date ORDER BY created_at, id")
    fun observeByDate(date: LocalDate): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_message WHERE id = :id")
    suspend fun byId(id: Long): ChatMessageEntity?
}

@Dao
interface AiCallLogDao {
    @Insert
    suspend fun insert(log: AiCallLogEntity): Long

    /** Thống kê sử dụng (F16, S42) từ mốc [fromMillis]. */
    @Query(
        "SELECT COUNT(*) AS calls, COALESCE(SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END), 0) AS failures, " +
            "COALESCE(SUM(input_tokens), 0) AS inputTokens, COALESCE(SUM(output_tokens), 0) AS outputTokens, " +
            "COALESCE(CAST(AVG(duration_ms) AS INTEGER), 0) AS avgDurationMs FROM ai_call_log WHERE created_at >= :fromMillis",
    )
    suspend fun usageSince(fromMillis: Long): AiUsage

    @Query(
        "SELECT COUNT(*) AS calls, COALESCE(SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END), 0) AS failures, " +
            "COALESCE(SUM(input_tokens), 0) AS inputTokens, COALESCE(SUM(output_tokens), 0) AS outputTokens, " +
            "COALESCE(CAST(AVG(duration_ms) AS INTEGER), 0) AS avgDurationMs FROM ai_call_log WHERE created_at >= :fromMillis",
    )
    fun observeUsageSince(fromMillis: Long): Flow<AiUsage>
}

data class AiUsage(val calls: Int, val failures: Int, val inputTokens: Long, val outputTokens: Long, val avgDurationMs: Long)

@Dao
interface WeeklyReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(review: WeeklyReviewEntity): Long

    @Query("SELECT * FROM weekly_review WHERE week_start = :weekStart")
    suspend fun byWeek(weekStart: LocalDate): WeeklyReviewEntity?

    @Query("SELECT * FROM weekly_review WHERE week_start = :weekStart")
    fun observeByWeek(weekStart: LocalDate): Flow<WeeklyReviewEntity?>
}

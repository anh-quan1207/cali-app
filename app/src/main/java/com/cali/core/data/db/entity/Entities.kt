@file:UseSerializers(LocalDateSerializer::class)

package com.cali.core.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.backup.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

// Bảng theo 04 §2. Ngày: LocalDate → ISO yyyy-MM-dd; thời điểm: epoch millis.

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val sex: Sex,
    @ColumnInfo(name = "birth_year") val birthYear: Int,
    @ColumnInfo(name = "height_cm") val heightCm: Double,
    @ColumnInfo(name = "activity_level") val activityLevel: ActivityLevel,
    @ColumnInfo(name = "goal_type") val goalType: GoalType,
    @ColumnInfo(name = "target_weight_kg") val targetWeightKg: Double?,
    @ColumnInfo(name = "weekly_rate_kg") val weeklyRateKg: Double,
    @ColumnInfo(name = "auto_recalc_on_weigh_in", defaultValue = "0") val autoRecalcOnWeighIn: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Serializable
@Entity(
    tableName = "goal_snapshot",
    indices = [Index(value = ["effective_from"], unique = true)],
)
data class GoalSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "effective_from") val effectiveFrom: LocalDate,
    @ColumnInfo(name = "weight_kg_used") val weightKgUsed: Double,
    val bmr: Double,
    val tdee: Double,
    @ColumnInfo(name = "target_kcal") val targetKcal: Int,
    @ColumnInfo(name = "protein_g") val proteinG: Int,
    @ColumnInfo(name = "carb_g") val carbG: Int,
    @ColumnInfo(name = "fat_g") val fatG: Int,
    @ColumnInfo(name = "water_ml") val waterMl: Int,
    @ColumnInfo(name = "is_kcal_overridden") val isKcalOverridden: Boolean,
    @ColumnInfo(name = "is_macro_overridden") val isMacroOverridden: Boolean,
    @ColumnInfo(name = "clamp_reason") val clampReason: ClampReason?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Serializable
@Entity(
    tableName = "weight_entry",
    indices = [Index(value = ["date"], unique = true)],
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Serializable
@Entity(
    tableName = "food_entry",
    foreignKeys = [
        ForeignKey(
            entity = AiCallLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["ai_call_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = MyFoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["my_food_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("date"),
        Index("eaten_at"),
        Index("ai_call_id"),
        Index("my_food_id"),
        Index("batch_id"),
    ],
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    @ColumnInfo(name = "eaten_at") val eatenAt: Long,
    @ColumnInfo(name = "meal_type") val mealType: MealType,
    @ColumnInfo(name = "meal_type_manual") val mealTypeManual: Boolean = false,
    val name: String,
    @ColumnInfo(name = "portion_desc") val portionDesc: String? = null,
    val grams: Double? = null,
    val kcal: Double,
    @ColumnInfo(name = "protein_g", defaultValue = "0") val proteinG: Double = 0.0,
    @ColumnInfo(name = "carb_g", defaultValue = "0") val carbG: Double = 0.0,
    @ColumnInfo(name = "fat_g", defaultValue = "0") val fatG: Double = 0.0,
    val source: FoodSource,
    @ColumnInfo(name = "ai_confidence") val aiConfidence: Double? = null,
    @ColumnInfo(name = "ai_call_id") val aiCallId: Long? = null,
    @ColumnInfo(name = "my_food_id") val myFoodId: Long? = null,
    @ColumnInfo(name = "batch_id") val batchId: String? = null,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Serializable
@Entity(
    tableName = "my_food",
    indices = [Index("name"), Index("name_normalized")],
)
data class MyFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "name_normalized") val nameNormalized: String,
    @ColumnInfo(name = "portion_desc") val portionDesc: String? = null,
    val grams: Double? = null,
    val kcal: Double,
    @ColumnInfo(name = "protein_g", defaultValue = "0") val proteinG: Double = 0.0,
    @ColumnInfo(name = "carb_g", defaultValue = "0") val carbG: Double = 0.0,
    @ColumnInfo(name = "fat_g", defaultValue = "0") val fatG: Double = 0.0,
    @ColumnInfo(name = "is_favorite", defaultValue = "0") val isFavorite: Boolean = false,
    @ColumnInfo(name = "use_count", defaultValue = "0") val useCount: Int = 0,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Serializable
@Entity(tableName = "water_entry", indices = [Index("date")])
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    @ColumnInfo(name = "amount_ml") val amountMl: Int,
    val source: WaterSource,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Serializable
@Entity(
    tableName = "chat_message",
    foreignKeys = [
        ForeignKey(
            entity = AiCallLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["ai_call_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("date"), Index("ai_call_id")],
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val role: ChatRole,
    val text: String? = null,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String? = null,
    @ColumnInfo(name = "food_log_json") val foodLogJson: String? = null,
    @ColumnInfo(name = "food_log_status") val foodLogStatus: FoodLogStatus? = null,
    @ColumnInfo(name = "ai_call_id") val aiCallId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Serializable
@Entity(tableName = "ai_call_log")
data class AiCallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: AiMode,
    val provider: String,
    val model: String,
    @ColumnInfo(name = "input_tokens") val inputTokens: Int? = null,
    @ColumnInfo(name = "output_tokens") val outputTokens: Int? = null,
    @ColumnInfo(name = "cache_read_tokens") val cacheReadTokens: Int? = null,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    val success: Boolean,
    @ColumnInfo(name = "error_type") val errorType: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Serializable
@Entity(
    tableName = "weekly_review",
    foreignKeys = [
        ForeignKey(
            entity = AiCallLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["ai_call_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["week_start"], unique = true), Index("ai_call_id")],
)
data class WeeklyReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "week_start") val weekStart: LocalDate,
    val score: Int,
    @ColumnInfo(name = "stats_hash") val statsHash: String,
    @ColumnInfo(name = "review_json") val reviewJson: String,
    @ColumnInfo(name = "is_partial_week") val isPartialWeek: Boolean,
    @ColumnInfo(name = "ai_call_id") val aiCallId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

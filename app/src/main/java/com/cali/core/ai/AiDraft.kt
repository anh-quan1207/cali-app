package com.cali.core.ai

import com.cali.core.calc.AmberFlag
import com.cali.core.calc.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Kết quả AI đã đọc lại từ `chat_message.food_log_json`, kèm giờ / bữa đã tính (có thể người dùng đã sửa). */
data class DecodedFoodLog(val log: ValidatedFoodLog, val timing: ResolvedTiming, val mealManual: Boolean)

/**
 * `food_log_json` của thẻ món trong chat (04 §2): lưu kết quả **đã qua Validator** chứ không lưu JSON thô của AI,
 * cùng giờ ăn + bữa đã tính để thẻ hiện "Tối · Hôm qua · 19:00" mà không phải tính lại (09 §8 G4-19).
 */
object AiFoodLogJson {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    fun encode(log: ValidatedFoodLog, timing: ResolvedTiming, mealManual: Boolean = false): String =
        json.encodeToString(Stored.serializer(), Stored.of(log, timing, mealManual))

    fun decode(text: String?): DecodedFoodLog? {
        if (text.isNullOrBlank()) return null
        return runCatching { json.decodeFromString(Stored.serializer(), text).toDecoded() }.getOrNull()
    }

    @Serializable
    private data class Stored(
        val items: List<Item>,
        @SerialName("meal_type") val mealType: MealType? = null,
        @SerialName("eaten_time") val eatenTime: String? = null,
        @SerialName("eaten_date") val eatenDate: String? = null,
        @SerialName("clarifying_question") val clarifyingQuestion: String? = null,
        val comment: String? = null,
        @SerialName("resolved_at") val resolvedAt: String,
        @SerialName("resolved_meal") val resolvedMeal: MealType,
        @SerialName("meal_manual") val mealManual: Boolean = false,
    ) {
        fun toDecoded() = DecodedFoodLog(
            log = ValidatedFoodLog(
                items = items.map { it.toItem() },
                mealType = mealType,
                eatenTime = eatenTime?.let(LocalTime::parse),
                eatenDate = eatenDate?.let(LocalDate::parse),
                clarifyingQuestion = clarifyingQuestion,
                comment = comment,
            ),
            timing = ResolvedTiming(LocalDateTime.parse(resolvedAt), resolvedMeal),
            mealManual = mealManual,
        )

        companion object {
            fun of(log: ValidatedFoodLog, t: ResolvedTiming, manual: Boolean) = Stored(
                items = log.items.map(Item::of),
                mealType = log.mealType,
                eatenTime = log.eatenTime?.toString(),
                eatenDate = log.eatenDate?.toString(),
                clarifyingQuestion = log.clarifyingQuestion,
                comment = log.comment,
                resolvedAt = t.eatenAt.toString(),
                resolvedMeal = t.mealType,
                mealManual = manual,
            )
        }
    }

    @Serializable
    private data class Item(
        val name: String,
        @SerialName("portion_desc") val portionDesc: String,
        val grams: Double,
        val kcal: Double,
        @SerialName("protein_g") val proteinG: Double,
        @SerialName("carb_g") val carbG: Double,
        @SerialName("fat_g") val fatG: Double,
        val confidence: Double,
        val assumptions: String? = null,
        val flags: Set<AmberFlag> = emptySet(),
    ) {
        fun toItem() = ValidatedFoodItem(name, portionDesc, grams, kcal, proteinG, carbG, fatG, confidence, assumptions, flags)

        companion object {
            fun of(i: ValidatedFoodItem) =
                Item(i.name, i.portionDesc, i.grams, i.kcal, i.proteinG, i.carbG, i.fatG, i.confidence, i.assumptions, i.flags)
        }
    }
}

/** Quy tắc hiển thị kết quả AI trên Thẻ xác nhận (S12) và thẻ chat (S20). */
object AiReview {
    /** 03 §6: confidence < 0,5 khi không chắc món gì → nhãn "Cần kiểm tra" (hổ phách, DESIGN Amber Check). */
    const val LOW_CONFIDENCE = 0.5

    fun needsCheck(item: ValidatedFoodItem): Boolean = item.confidence < LOW_CONFIDENCE

    /** Mockup s12 f3: món cần kiểm tra hoặc có cờ số liệu lên đầu; còn lại giữ thứ tự AI trả. */
    fun reviewOrder(items: List<ValidatedFoodItem>): List<ValidatedFoodItem> =
        items.sortedBy { if (needsCheck(it) || it.flags.isNotEmpty()) 0 else 1 }
}

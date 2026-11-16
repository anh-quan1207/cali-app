package com.cali.core.ai

import com.cali.core.calc.AmberCheck
import com.cali.core.calc.AmberFlag
import com.cali.core.calc.FoodValues
import com.cali.core.calc.model.MealType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * Kiểm tra input của tool `record_food_items` (03 §5).
 * - Sai schema / thiếu trường / tên rỗng → [Outcome.Invalid] (gọi lại 1 lần).
 * - `items` rỗng → hợp lệ, UI hiện "Không nhận ra món ăn nào".
 * - Số liệu từng món qua [AmberCheck]: âm/NaN → 0, lệch macro, > 2.500 kcal, làm tròn.
 * - `meal_type` / `eaten_time` / `eaten_date` sai hoặc ngày ở tương lai → bỏ qua (null).
 */
object FoodLogValidator {

    sealed interface Outcome {
        data class Valid(val log: ValidatedFoodLog) : Outcome
        data class Invalid(val reason: String) : Outcome
    }

    private val json = Json { isLenient = false }
    private val timeFormat = DateTimeFormatter.ofPattern("H:mm")

    fun validate(raw: String, today: LocalDate): Outcome {
        val element = try {
            json.parseToJsonElement(raw)
        } catch (e: Exception) {
            return Outcome.Invalid("JSON hỏng: ${e.message?.take(80)}")
        }
        return validate(element, today)
    }

    fun validate(input: JsonElement?, today: LocalDate): Outcome {
        val root = input as? JsonObject ?: return Outcome.Invalid("input không phải object")
        val rawItems = root["items"] as? JsonArray ?: return Outcome.Invalid("thiếu items hoặc không phải mảng")

        val items = rawItems.mapIndexed { i, el ->
            val obj = el as? JsonObject ?: return Outcome.Invalid("items[$i] không phải object")
            parseItem(obj) ?: return Outcome.Invalid("items[$i] thiếu trường hoặc sai kiểu")
        }

        return Outcome.Valid(
            ValidatedFoodLog(
                items = items,
                mealType = root.optString("meal_type")?.let { s -> MealType.entries.firstOrNull { it.name == s } },
                eatenTime = root.optString("eaten_time")?.let(::parseTime),
                eatenDate = root.optString("eaten_date")?.let(::parseDate)?.takeIf { !it.isAfter(today) },
                clarifyingQuestion = root.optString("clarifying_question"),
                comment = root.optString("comment"),
            ),
        )
    }

    private fun parseItem(o: JsonObject): ValidatedFoodItem? {
        val name = o.string("name")?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val portion = o.string("portion_desc") ?: return null
        val grams = o.number("grams") ?: return null
        val kcal = o.number("kcal") ?: return null
        val p = o.number("protein_g") ?: return null
        val c = o.number("carb_g") ?: return null
        val f = o.number("fat_g") ?: return null
        val confidence = o.number("confidence") ?: return null

        val amber = AmberCheck.check(FoodValues(kcal, p, c, f))
        val flags = amber.flags.toMutableSet()
        val gramsClean = if (grams.isNaN() || grams.isInfinite() || grams < 0) {
            flags += AmberFlag.INVALID_NUMBER_FIXED
            0.0
        } else {
            Math.round(grams * 10) / 10.0
        }

        return ValidatedFoodItem(
            name = name,
            portionDesc = portion.trim(),
            grams = gramsClean,
            kcal = amber.values.kcal,
            proteinG = amber.values.proteinG,
            carbG = amber.values.carbG,
            fatG = amber.values.fatG,
            confidence = if (confidence.isNaN()) 0.0 else confidence.coerceIn(0.0, 1.0),
            assumptions = o.optString("assumptions"),
            flags = flags,
        )
    }

    /** Chuỗi bắt buộc: phải là JSON string. */
    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

    /** Số bắt buộc: phải là JSON number (không nhận "120" dạng chuỗi). */
    private fun JsonObject.number(key: String): Double? =
        (this[key] as? JsonPrimitive)?.takeIf { !it.isString && it !is JsonNull }?.doubleOrNull

    /** Chuỗi tuỳ chọn: rỗng / khoảng trắng / sai kiểu → null. */
    private fun JsonObject.optString(key: String): String? = string(key)?.trim()?.takeIf { it.isNotEmpty() }

    private fun parseTime(s: String): LocalTime? = runCatching { LocalTime.parse(s, timeFormat) }.getOrNull()

    private fun parseDate(s: String): LocalDate? = runCatching { LocalDate.parse(s) }.getOrNull()
}

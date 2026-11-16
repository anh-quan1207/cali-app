package com.cali.core.ai

import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MealType
import java.time.LocalDateTime

data class ResolvedTiming(val eatenAt: LocalDateTime, val mealType: MealType)

/**
 * Thời điểm ăn và bữa mặc định cho kết quả AI (05 §4.1, 4.3), trước khi người dùng sửa trên Thẻ xác nhận.
 * Người dùng tự chọn bữa (ưu tiên 1) xử lý ở UI bằng [MealTypeResolver.resolve] với `manual`.
 */
class FoodLogTiming(private val resolver: MealTypeResolver) {

    /**
     * @param now lúc gửi.
     * @param defaultAt thời điểm mặc định theo cách ghi (lúc bấm chụp, EXIF của ảnh thư viện); null = [now].
     */
    fun resolve(log: ValidatedFoodLog, now: LocalDateTime, defaultAt: LocalDateTime? = null): ResolvedTiming {
        val base = defaultAt ?: now
        val date = log.eatenDate ?: base.toLocalDate()
        val otherDay = log.eatenDate != null && date != base.toLocalDate()
        val time = log.eatenTime
            ?: log.mealType?.takeIf { otherDay }?.let(resolver::defaultTimeFor)
            ?: base.toLocalTime()
        return ResolvedTiming(
            eatenAt = LocalDateTime.of(date, time),
            mealType = resolver.resolve(time, manual = null, fromAi = log.mealType),
        )
    }
}

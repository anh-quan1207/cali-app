package com.cali.core.data.prefs

import com.cali.core.calc.MealBoundary
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MacroPct
import java.time.LocalTime

/** Toàn bộ khóa DataStore ở 04 §3. API key KHÔNG ở đây (xem SecureKeyStore). */
data class Settings(
    val onboardingDone: Boolean = false,
    val aiProvider: String = "neuralwatt",
    /** ID cụ thể, không dùng bí danh nw-large / nw-flash (09 §8). */
    val aiModelVision: String = "kimi-k3.1-pro",
    val aiModelChat: String = "deepseek-v4.1-flash",
    /** null = dùng bản mặc định trong assets. */
    val systemPromptOverride: String? = null,
    val systemPromptDefaultVersion: Int = 1,
    val personalNotes: String = "",
    val macroPctOverride: MacroPct? = null,
    val kcalOverride: Int? = null,
    val waterEnabled: Boolean = true,
    val waterStart: LocalTime = LocalTime.of(7, 0),
    val waterEnd: LocalTime = LocalTime.of(22, 0),
    val waterIntervalMin: Int = 90,
    val waterCupSmallMl: Int = 250,
    val waterCupLargeMl: Int = 500,
    /** Mục tiêu nước tùy chỉnh (S43); null = tự động theo goal_snapshot. Khóa thêm ở GĐ5 (G5-1); mặc định 1.500 ml theo yêu cầu người dùng (G5-8). */
    val waterGoalOverrideMl: Int? = DEFAULT_WATER_GOAL_ML,
    val mealTimes: List<MealBoundary> = MealTypeResolver.DEFAULT,
    val saveThumbnails: Boolean = true,
    val weeklyNotifyEnabled: Boolean = true,
    val weeklyNotifyTime: LocalTime = LocalTime.of(8, 0),
    val weeklyAiAuto: Boolean = true,
    /** Lần xuất sao lưu gần nhất (S45). Khóa thêm ở GĐ3, ghi ở 09 §8. */
    val lastBackupAt: Long? = null,
    val lastBackupFile: String? = null,
) {
    companion object {
        /** Mục tiêu nước mặc định khi chưa chọn "Tự động" (G5-8). */
        const val DEFAULT_WATER_GOAL_ML = 1500
    }
}

package com.cali.core.data.prefs

import com.cali.core.calc.MealBoundary
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MacroPct
import java.time.LocalTime

/** Toàn bộ khóa DataStore ở 04 §3. API key KHÔNG ở đây (xem SecureKeyStore). */
data class Settings(
    val onboardingDone: Boolean = false,
    val aiProvider: String = "anthropic",
    val aiModelVision: String = "claude-sonnet-5-5",
    val aiModelChat: String = "claude-haiku-5-5",
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
    val mealTimes: List<MealBoundary> = MealTypeResolver.DEFAULT,
    val saveThumbnails: Boolean = true,
    val weeklyNotifyEnabled: Boolean = true,
    val weeklyNotifyTime: LocalTime = LocalTime.of(8, 0),
    val weeklyAiAuto: Boolean = true,
)

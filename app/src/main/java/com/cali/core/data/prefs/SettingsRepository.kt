package com.cali.core.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cali.core.calc.MealBoundary
import com.cali.core.calc.model.MacroPct
import com.cali.core.calc.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** Đọc/ghi Settings (04 §3). Không ném exception ra ngoài: lỗi trả qua Result. */
@Singleton
class SettingsRepository @Inject constructor(
    @SettingsStore private val store: DataStore<Preferences>,
) {
    val settings: Flow<Settings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map(::read)

    suspend fun current(): Settings = settings.first()

    suspend fun update(transform: (Settings) -> Settings): Result<Settings> = runCatching {
        var result: Settings? = null
        store.edit { prefs ->
            val next = transform(read(prefs))
            write(prefs, next)
            result = next
        }
        result!!
    }

    /**
     * Toàn bộ khóa dạng JSON cho sao lưu (04 §5). Giữ nguyên kiểu (bool / số / chuỗi) để nhập lại đúng khóa.
     * Kho này không chứa API key (key ở secure_prefs).
     */
    suspend fun exportRaw(): JsonObject = buildJsonObject {
        store.data.first().asMap().entries.sortedBy { it.key.name }.forEach { (k, v) ->
            when (v) {
                is Boolean -> put(k.name, JsonPrimitive(v))
                is Number -> put(k.name, JsonObject(mapOf(TYPE to JsonPrimitive(if (v is Long) "long" else "int"), VALUE to JsonPrimitive(v))))
                is String -> put(k.name, JsonPrimitive(v))
                else -> Unit
            }
        }
    }

    /** Thay toàn bộ khóa bằng [raw] (khôi phục). */
    suspend fun replaceAll(raw: JsonObject): Result<Unit> = runCatching {
        store.edit { p ->
            p.clear()
            raw.forEach { (name, el) ->
                when {
                    el is JsonObject -> {
                        val v = el.getValue(VALUE).jsonPrimitive
                        if (el[TYPE]?.jsonPrimitive?.content == "long") p[longPreferencesKey(name)] = v.long
                        else p[intPreferencesKey(name)] = v.int
                    }
                    el is JsonPrimitive && el.isString -> p[stringPreferencesKey(name)] = el.content
                    el is JsonPrimitive -> el.booleanOrNull?.let { p[booleanPreferencesKey(name)] = it }
                }
            }
        }
    }

    /** Xoá mọi khóa (Xoá toàn bộ dữ liệu). */
    suspend fun clear(): Result<Unit> = runCatching { store.edit { it.clear() } }

    private fun read(p: Preferences): Settings {
        val d = Settings()
        return Settings(
            onboardingDone = p[K.onboardingDone] ?: d.onboardingDone,
            aiProvider = p[K.aiProvider]?.takeUnless { it == LEGACY_PROVIDER } ?: d.aiProvider,
            aiModelVision = p[K.aiModelVision]?.takeUnless(::isLegacyModel) ?: d.aiModelVision,
            aiModelChat = p[K.aiModelChat]?.takeUnless(::isLegacyModel) ?: d.aiModelChat,
            systemPromptOverride = p[K.systemPromptOverride],
            systemPromptDefaultVersion = p[K.systemPromptDefaultVersion] ?: d.systemPromptDefaultVersion,
            personalNotes = p[K.personalNotes] ?: d.personalNotes,
            macroPctOverride = p[K.macroPctOverride]?.let(::decodeMacro),
            kcalOverride = p[K.kcalOverride],
            waterEnabled = p[K.waterEnabled] ?: d.waterEnabled,
            waterStart = p[K.waterStart]?.let(::parseTime) ?: d.waterStart,
            waterEnd = p[K.waterEnd]?.let(::parseTime) ?: d.waterEnd,
            waterIntervalMin = p[K.waterIntervalMin] ?: d.waterIntervalMin,
            waterCupSmallMl = p[K.waterCupSmallMl] ?: d.waterCupSmallMl,
            waterCupLargeMl = p[K.waterCupLargeMl] ?: d.waterCupLargeMl,
            waterGoalOverrideMl = p[K.waterGoalOverrideMl].let { if (it == null) d.waterGoalOverrideMl else it.takeUnless { v -> v == WATER_GOAL_AUTO } },
            mealTimes = p[K.mealTimes]?.let(::decodeMeals) ?: d.mealTimes,
            saveThumbnails = p[K.saveThumbnails] ?: d.saveThumbnails,
            weeklyNotifyEnabled = p[K.weeklyNotifyEnabled] ?: d.weeklyNotifyEnabled,
            weeklyNotifyTime = p[K.weeklyNotifyTime]?.let(::parseTime) ?: d.weeklyNotifyTime,
            weeklyAiAuto = p[K.weeklyAiAuto] ?: d.weeklyAiAuto,
            lastBackupAt = p[K.lastBackupAt],
            lastBackupFile = p[K.lastBackupFile],
        )
    }

    private fun write(p: MutablePreferences, s: Settings) {
        p[K.onboardingDone] = s.onboardingDone
        p[K.aiProvider] = s.aiProvider
        p[K.aiModelVision] = s.aiModelVision
        p[K.aiModelChat] = s.aiModelChat
        p.setOrRemove(K.systemPromptOverride, s.systemPromptOverride)
        p[K.systemPromptDefaultVersion] = s.systemPromptDefaultVersion
        p[K.personalNotes] = s.personalNotes
        p.setOrRemove(K.macroPctOverride, s.macroPctOverride?.let(::encodeMacro))
        p.setOrRemove(K.kcalOverride, s.kcalOverride)
        p[K.waterEnabled] = s.waterEnabled
        p[K.waterStart] = s.waterStart.toString()
        p[K.waterEnd] = s.waterEnd.toString()
        p[K.waterIntervalMin] = s.waterIntervalMin
        p[K.waterCupSmallMl] = s.waterCupSmallMl
        p[K.waterCupLargeMl] = s.waterCupLargeMl
        p[K.waterGoalOverrideMl] = s.waterGoalOverrideMl ?: WATER_GOAL_AUTO
        p[K.mealTimes] = encodeMeals(s.mealTimes)
        p[K.saveThumbnails] = s.saveThumbnails
        p[K.weeklyNotifyEnabled] = s.weeklyNotifyEnabled
        p[K.weeklyNotifyTime] = s.weeklyNotifyTime.toString()
        p[K.weeklyAiAuto] = s.weeklyAiAuto
        p.setOrRemove(K.lastBackupAt, s.lastBackupAt)
        p.setOrRemove(K.lastBackupFile, s.lastBackupFile)
    }

    private fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
        if (value == null) remove(key) else this[key] = value
    }

    @Serializable private data class MacroJson(val p: Int, val c: Int, val f: Int)

    @Serializable private data class MealJson(val start: String, val meal: MealType)

    private val json = Json { ignoreUnknownKeys = true }

    private fun encodeMacro(m: MacroPct) = json.encodeToString(MacroJson(m.protein, m.carb, m.fat))
    private fun decodeMacro(s: String): MacroPct? =
        runCatching { json.decodeFromString<MacroJson>(s).let { MacroPct(it.p, it.c, it.f) } }.getOrNull()

    private fun encodeMeals(list: List<MealBoundary>) =
        json.encodeToString(list.map { MealJson(it.start.toString(), it.meal) })
    private fun decodeMeals(s: String): List<MealBoundary>? = runCatching {
        json.decodeFromString<List<MealJson>>(s).map { MealBoundary(LocalTime.parse(it.start), it.meal) }
    }.getOrNull()?.takeIf { it.isNotEmpty() }

    private fun parseTime(s: String): LocalTime? = runCatching { LocalTime.parse(s) }.getOrNull()

    /** Tên khóa đúng như 04 §3. */
    private companion object {
        const val TYPE = "type"
        const val VALUE = "value"

        /** Bản cài GĐ1–GĐ3 có thể đã ghi giá trị Claude vào DataStore; đổi sang Neuralwatt (09 §8). */
        const val LEGACY_PROVIDER = "anthropic"

        /** Lưu "mục tiêu nước tự động" thành 0, vì thiếu khóa nghĩa là mặc định 1.500 (G5-8). */
        const val WATER_GOAL_AUTO = 0

        fun isLegacyModel(id: String) = id.startsWith("claude-")
    }

    private object K {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val aiProvider = stringPreferencesKey("ai_provider")
        val aiModelVision = stringPreferencesKey("ai_model_vision")
        val aiModelChat = stringPreferencesKey("ai_model_chat")
        val systemPromptOverride = stringPreferencesKey("system_prompt_override")
        val systemPromptDefaultVersion = intPreferencesKey("system_prompt_default_version")
        val personalNotes = stringPreferencesKey("personal_notes")
        val macroPctOverride = stringPreferencesKey("macro_pct_override")
        val kcalOverride = intPreferencesKey("kcal_override")
        val waterEnabled = booleanPreferencesKey("water_enabled")
        val waterStart = stringPreferencesKey("water_start")
        val waterEnd = stringPreferencesKey("water_end")
        val waterIntervalMin = intPreferencesKey("water_interval_min")
        val waterCupSmallMl = intPreferencesKey("water_cup_small_ml")
        val waterCupLargeMl = intPreferencesKey("water_cup_large_ml")
        val waterGoalOverrideMl = intPreferencesKey("water_goal_override_ml")
        val mealTimes = stringPreferencesKey("meal_times")
        val saveThumbnails = booleanPreferencesKey("save_thumbnails")
        val weeklyNotifyEnabled = booleanPreferencesKey("weekly_notify_enabled")
        val weeklyNotifyTime = stringPreferencesKey("weekly_notify_time")
        val weeklyAiAuto = booleanPreferencesKey("weekly_ai_auto")
        val lastBackupAt = longPreferencesKey("last_backup_at")
        val lastBackupFile = stringPreferencesKey("last_backup_file")
    }
}

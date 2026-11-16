package com.cali.core.data.repo

import androidx.room.withTransaction
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream
import java.io.OutputStream
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** File sao lưu theo 04 §5. Không có API key, thumbnail, ai_call_log, weekly_review. */
@Serializable
data class BackupFile(
    val app: String = APP,
    @SerialName("schema_version") val schemaVersion: Int = SCHEMA_VERSION,
    @SerialName("exported_at") val exportedAt: String,
    val profile: UserProfileEntity? = null,
    @SerialName("goal_snapshots") val goalSnapshots: List<GoalSnapshotEntity> = emptyList(),
    val weights: List<WeightEntryEntity> = emptyList(),
    @SerialName("food_entries") val foodEntries: List<FoodEntryEntity> = emptyList(),
    @SerialName("my_foods") val myFoods: List<MyFoodEntity> = emptyList(),
    @SerialName("water_entries") val waterEntries: List<WaterEntryEntity> = emptyList(),
    @SerialName("chat_messages") val chatMessages: List<ChatMessageEntity> = emptyList(),
    val settings: JsonObject = JsonObject(emptyMap()),
) {
    companion object {
        const val APP = "Cali"
        const val SCHEMA_VERSION = 1
    }
}

/** Ảnh chụp toàn bộ dữ liệu trong DB, để so sánh (test) và đếm. */
data class DataDump(
    val profiles: List<UserProfileEntity>,
    val goals: List<GoalSnapshotEntity>,
    val weights: List<WeightEntryEntity>,
    val foodEntries: List<FoodEntryEntity>,
    val myFoods: List<MyFoodEntity>,
    val waters: List<WaterEntryEntity>,
    val chats: List<ChatMessageEntity>,
)

data class BackupCounts(val foodEntries: Int, val weights: Int, val myFoods: Int, val days: Int)

data class DeviceStats(val daysLogged: Int, val weights: Int, val myFoods: Int)

sealed class BackupError(message: String) : Exception(message) {
    class Unreadable(cause: Throwable?) : BackupError("File không đọc được") { init { cause?.let(::initCause) } }
    class NotCali : BackupError("Không phải file sao lưu của Cali")
    class NewerVersion(val version: Int) : BackupError("File từ bản app mới hơn ($version)")
}

/** Sao lưu / khôi phục JSON (F13, 04 §5) và Xoá toàn bộ dữ liệu (S45). */
@Singleton
class BackupRepository @Inject constructor(
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    private val dao = db.backupDao()

    fun stats(): Flow<DeviceStats> =
        combine(dao.observeDaysLogged(), dao.observeWeightCount(), dao.observeMyFoodCount()) { d, w, m -> DeviceStats(d, w, m) }

    suspend fun dump(): DataDump = DataDump(
        dao.profiles(), dao.goals(), dao.weights(), dao.foods(), dao.myFoods(), dao.waters(), dao.chats(),
    )

    /** Ghi file. Lần xuất gần nhất được ghi vào cài đặt trước, nên có mặt trong chính file đó. */
    suspend fun export(out: OutputStream, fileName: String? = null): Result<BackupCounts> = runCatching {
        val now = clock.millis()
        settings.update { it.copy(lastBackupAt = now, lastBackupFile = fileName) }.getOrThrow()
        val d = db.withTransaction { dump() }
        val file = BackupFile(
            exportedAt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(now), clock.zone).toString(),
            profile = d.profiles.firstOrNull(),
            goalSnapshots = d.goals,
            weights = d.weights,
            // ai_call_log không xuất → bỏ liên kết
            foodEntries = d.foodEntries.map { it.copy(aiCallId = null, thumbnailPath = null) },
            myFoods = d.myFoods,
            waterEntries = d.waters,
            chatMessages = d.chats.map { it.copy(aiCallId = null, thumbnailPath = null) },
            settings = settings.exportRaw(),
        )
        withContext(Dispatchers.IO) {
            out.write(json.encodeToString(BackupFile.serializer(), file).toByteArray(Charsets.UTF_8))
            out.flush()
        }
        BackupCounts(d.foodEntries.size, d.weights.size, d.myFoods.size, d.foodEntries.map { it.date }.distinct().size)
    }

    /** Đọc và kiểm tra trước, rồi thay toàn bộ trong một transaction. Lỗi → dữ liệu cũ nguyên vẹn. */
    suspend fun import(input: InputStream): Result<BackupCounts> {
        val file = try {
            val text = withContext(Dispatchers.IO) { input.readBytes().toString(Charsets.UTF_8) }
            val root = json.parseToJsonElement(text).jsonObject
            if (root["app"]?.jsonPrimitive?.content != BackupFile.APP) return Result.failure(BackupError.NotCali())
            val version = root["schema_version"]?.jsonPrimitive?.int ?: return Result.failure(BackupError.NotCali())
            if (version > BackupFile.SCHEMA_VERSION) return Result.failure(BackupError.NewerVersion(version))
            // schema_version thấp hơn: chưa có bản cũ nào → chưa cần chuyển đổi.
            json.decodeFromJsonElement(BackupFile.serializer(), root)
        } catch (e: Exception) {
            return Result.failure(BackupError.Unreadable(e))
        }
        return runCatching {
            db.withTransaction {
                clearTables()
                file.profile?.let { dao.insertProfiles(listOf(it)) }
                dao.insertGoals(file.goalSnapshots)
                dao.insertWeights(file.weights)
                dao.insertMyFoods(file.myFoods)
                dao.insertFoods(file.foodEntries.map { it.copy(aiCallId = null) })
                dao.insertWaters(file.waterEntries)
                dao.insertChats(file.chatMessages.map { it.copy(aiCallId = null) })
            }
            settings.replaceAll(file.settings).getOrThrow()
            if (file.profile != null) settings.update { it.copy(onboardingDone = true) }.getOrThrow()
            BackupCounts(file.foodEntries.size, file.weights.size, file.myFoods.size, file.foodEntries.map { it.date }.distinct().size)
        }
    }

    /** Xoá toàn bộ DB + cài đặt (API key do SecureKeyStore xoá riêng). App về màn chào. */
    suspend fun wipeAll(): Result<Unit> = runCatching {
        db.withTransaction { clearTables() }
        settings.clear().getOrThrow()
    }

    private suspend fun clearTables() {
        dao.clearFoods()
        dao.clearChats()
        dao.clearReviews()
        dao.clearAiCalls()
        dao.clearMyFoods()
        dao.clearWaters()
        dao.clearWeights()
        dao.clearGoals()
        dao.clearProfiles()
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }
}

package com.cali.core.data.repo

import androidx.room.withTransaction
import com.cali.core.calc.DayFood
import com.cali.core.calc.DaySummary
import com.cali.core.calc.DaySummaryCalculator
import com.cali.core.calc.FoodValues
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.RecentCandidate
import com.cali.core.calc.RecentFoods
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.TextNormalizer
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.MyFoodEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Một món chuẩn bị lưu (S12). Giá trị tuyệt đối, đã nhân hệ số khẩu phần. */
data class NewFood(
    val name: String,
    val portionDesc: String?,
    val grams: Double?,
    val values: FoodValues,
    val source: FoodSource,
    val myFoodId: Long? = null,
    /** Dấu trang "Lưu vào Món của tôi" trên thẻ. */
    val saveAsMyFood: Boolean = false,
)

data class DayData(
    val date: LocalDate,
    val entries: List<FoodEntryEntity>,
    val goal: GoalSnapshotEntity?,
    val summary: DaySummary,
)

/** Nhật ký ăn (04 §2 food_entry, §4 observeDaySummary). Ngày của món = ngày của `eaten_at`. */
@Singleton
class DiaryRepository @Inject constructor(
    private val db: AppDatabase,
    private val clock: Clock,
) {
    private val foodDao = db.foodEntryDao()
    private val goalDao = db.goalSnapshotDao()
    private val myFoodDao = db.myFoodDao()

    var resolver: MealTypeResolver = MealTypeResolver()

    fun today(): LocalDate = LocalDate.now(clock)
    fun now(): LocalDateTime = LocalDateTime.now(clock)

    fun mealFor(time: LocalTime): MealType = resolver.fromTime(time)

    fun toMillis(at: LocalDateTime): Long = at.atZone(clock.zone).toInstant().toEpochMilli()
    fun toLocal(millis: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), clock.zone)

    fun observeDay(date: LocalDate): Flow<DayData> =
        combine(foodDao.observeByDate(date), goalDao.observeForDate(date)) { entries, goal ->
            DayData(date, entries, goal, DaySummaryCalculator.summarize(entries.map(::toDayFood), goal?.targetKcal))
        }

    fun toDayFood(e: FoodEntryEntity): DayFood {
        val t = toLocal(e.eatenAt).toLocalTime()
        return DayFood(e.id, t.hour * 60 + t.minute, e.mealType, e.kcal, e.proteinG, e.carbG, e.fatG)
    }

    /** Lưu một lần xác nhận: cùng batch_id, tăng use_count món từ Món của tôi, tạo món mới nếu đánh dấu. */
    suspend fun add(foods: List<NewFood>, eatenAt: LocalDateTime, meal: MealType, mealManual: Boolean): Result<List<Long>> = runCatching {
        require(foods.isNotEmpty())
        val now = clock.millis()
        val batch = UUID.randomUUID().toString()
        db.withTransaction {
            foods.mapIndexed { i, f ->
                var myId = f.myFoodId
                if (myId == null && f.saveAsMyFood) {
                    myId = myFoodDao.insert(
                        MyFoodEntity(
                            name = f.name.trim(), nameNormalized = TextNormalizer.normalize(f.name),
                            portionDesc = f.portionDesc, grams = f.grams,
                            kcal = f.values.kcal, proteinG = f.values.proteinG, carbG = f.values.carbG, fatG = f.values.fatG,
                            createdAt = now, updatedAt = now,
                        ),
                    )
                }
                if (myId != null) myFoodDao.markUsed(myId, now)
                foodDao.insert(
                    FoodEntryEntity(
                        date = eatenAt.toLocalDate(),
                        eatenAt = toMillis(eatenAt),
                        mealType = meal,
                        mealTypeManual = mealManual,
                        name = f.name.trim(),
                        portionDesc = f.portionDesc,
                        grams = f.grams,
                        kcal = f.values.kcal,
                        proteinG = f.values.proteinG,
                        carbG = f.values.carbG,
                        fatG = f.values.fatG,
                        source = if (f.source == FoodSource.MANUAL && f.myFoodId != null) FoodSource.MY_FOOD else f.source,
                        myFoodId = myId,
                        batchId = batch,
                        sortOrder = i,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            }
        }
    }

    /** Sửa món (S14). Đổi giờ sang ngày khác thì món chuyển ngày. */
    suspend fun update(
        entry: FoodEntryEntity,
        newEatenAt: LocalDateTime,
        meal: MealType,
        mealManual: Boolean,
        values: FoodValues,
        name: String,
        portionDesc: String?,
        grams: Double?,
    ): Result<Unit> = runCatching {
        foodDao.update(
            entry.copy(
                date = newEatenAt.toLocalDate(),
                eatenAt = toMillis(newEatenAt),
                mealType = meal,
                mealTypeManual = mealManual,
                name = name.trim(),
                portionDesc = portionDesc,
                grams = grams,
                kcal = values.kcal,
                proteinG = values.proteinG,
                carbG = values.carbG,
                fatG = values.fatG,
                updatedAt = clock.millis(),
            ),
        )
    }

    /** Chuyển bữa (nhấn giữ trên S10): bữa do người dùng chọn. */
    suspend fun moveMeal(id: Long, meal: MealType): Result<Unit> = runCatching {
        val e = requireNotNull(foodDao.byId(id))
        foodDao.update(e.copy(mealType = meal, mealTypeManual = true, updatedAt = clock.millis()))
    }

    suspend fun byId(id: Long): FoodEntryEntity? = foodDao.byId(id)

    /** Ghi lại nguyên bản ghi (Hoàn tác chuyển bữa / sửa). */
    suspend fun put(entry: FoodEntryEntity): Result<Unit> = runCatching { foodDao.update(entry) }

    /** Xoá; trả lại bản ghi để Hoàn tác. */
    suspend fun delete(id: Long): Result<FoodEntryEntity> = runCatching {
        val e = requireNotNull(foodDao.byId(id)) { "Không có món $id" }
        foodDao.delete(e)
        e
    }

    suspend fun restore(entry: FoodEntryEntity): Result<Unit> = runCatching {
        // Món đã lưu có thể vừa bị xoá trong lúc chờ Hoàn tác → bỏ liên kết.
        val linked = entry.myFoodId?.let { myFoodDao.byId(it) }?.id
        foodDao.insert(entry.copy(myFoodId = linked))
    }

    /** Lưu một món trong nhật ký vào Món của tôi (nhấn giữ trên S10). */
    suspend fun saveToMyFoods(id: Long): Result<Long> = runCatching {
        val e = requireNotNull(foodDao.byId(id))
        e.myFoodId?.let { return@runCatching it }
        val now = clock.millis()
        db.withTransaction {
            val myId = myFoodDao.insert(
                MyFoodEntity(
                    name = e.name, nameNormalized = TextNormalizer.normalize(e.name), portionDesc = e.portionDesc,
                    grams = e.grams, kcal = e.kcal, proteinG = e.proteinG, carbG = e.carbG, fatG = e.fatG,
                    useCount = 1, lastUsedAt = e.eatenAt, createdAt = now, updatedAt = now,
                ),
            )
            foodDao.update(e.copy(myFoodId = myId, updatedAt = now))
            myId
        }
    }

    /** Tab Gần đây (S13): nhóm theo ngày, bỏ trùng. */
    suspend fun recent(limit: Int = 80): List<Pair<LocalDate, List<FoodEntryEntity>>> {
        val rows = foodDao.recent(limit)
        val byId = rows.associateBy { it.id }
        return RecentFoods.group(rows.map { RecentCandidate(it.id, it.date, it.eatenAt, it.name, it.kcal) })
            .map { (d, list) -> d to list.map { byId.getValue(it.id) } }
    }
}

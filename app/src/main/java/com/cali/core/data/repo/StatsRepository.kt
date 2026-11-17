package com.cali.core.data.repo

import com.cali.core.calc.DayInput
import com.cali.core.calc.FoodContribution
import com.cali.core.calc.WeightPoint
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.dao.WaterDayTotal
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Dữ liệu thô của một khoảng ngày cho S30–S35 (đã gộp theo ngày, mục tiêu theo snapshot của từng ngày). */
data class RangeData(
    val from: LocalDate,
    val to: LocalDate,
    val today: LocalDate,
    /** Ngày bắt đầu dùng app = min(ngày tạo hồ sơ, ngày món sớm nhất); null khi chưa có hồ sơ. */
    val firstUseDate: LocalDate?,
    /** Mỗi ngày trong [from]..[to] một dòng. */
    val days: Map<LocalDate, DayInput>,
    val foods: List<FoodContribution>,
    /** Mọi lần cân (không chỉ trong khoảng), cũ → mới. */
    val weights: List<WeightPoint>,
    val profile: UserProfileEntity?,
) {
    fun daysIn(from: LocalDate, to: LocalDate): List<DayInput> = days.values.filter { it.date in from..to }
}

/** GĐ6 (04 §4): tổng hợp nhật ký, nước, cân, mục tiêu theo ngày. */
@Singleton
class StatsRepository @Inject constructor(
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    fun today(): LocalDate = LocalDate.now(clock)

    private val foodDao = db.foodEntryDao()
    private val waterDao = db.waterEntryDao()
    private val goalDao = db.goalSnapshotDao()
    private val weightDao = db.weightEntryDao()
    private val profileDao = db.userProfileDao()

    suspend fun load(from: LocalDate, to: LocalDate): RangeData = assemble(
        from, to,
        foodDao.between(from, to),
        waterDao.totalsBetween(from, to),
        goalDao.all(),
        weightDao.observeAll().first(),
        settings.current().waterGoalOverrideMl,
        profileDao.observe().first(),
        foodDao.firstDate(),
    )

    fun observe(from: LocalDate, to: LocalDate): Flow<RangeData> {
        val meta = combine(settings.settings, profileDao.observe(), foodDao.observeFirstDate()) { s, p, first ->
            Triple(s.waterGoalOverrideMl, p, first)
        }
        return combine(
            foodDao.observeBetween(from, to),
            waterDao.observeTotalsBetween(from, to),
            goalDao.observeAll(),
            weightDao.observeAll(),
            meta,
        ) { foods, water, goals, weights, m -> assemble(from, to, foods, water, goals, weights, m.first, m.second, m.third) }
    }

    /**
     * Dấu vân tay số liệu một tuần để biết nhận xét AI đã cũ (08 §5.1): chỉ gồm ngày có món hoặc nước,
     * nên một ngày trống trôi qua không làm nhận xét "đã đổi".
     */
    fun hashOf(data: RangeData, weekStart: LocalDate): String {
        val week = weekStart..weekStart.plusDays(6)
        val text = buildString {
            data.days.values.filter { it.date in week && (it.entryCount > 0 || it.waterMl > 0) }.sortedBy { it.date }.forEach { d ->
                append(d.date).append('|').append(d.entryCount).append('|').append(Math.round(d.kcal))
                    .append('|').append(Math.round(d.proteinG)).append('|').append(Math.round(d.carbG))
                    .append('|').append(Math.round(d.fatG)).append('|').append(d.waterMl)
                    .append('|').append(d.targetKcal).append(';')
            }
            data.foods.filter { it.date in week }.map { "${it.date}:${it.name}:${Math.round(it.kcal)}" }.sorted().forEach { append(it).append(';') }
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(32)
    }

    private fun assemble(
        from: LocalDate,
        to: LocalDate,
        foods: List<FoodEntryEntity>,
        water: List<WaterDayTotal>,
        goals: List<GoalSnapshotEntity>,
        weights: List<WeightEntryEntity>,
        waterOverride: Int?,
        profile: UserProfileEntity?,
        firstFood: LocalDate?,
    ): RangeData {
        val sortedGoals = goals.sortedBy { it.effectiveFrom }
        fun goalFor(date: LocalDate) = sortedGoals.lastOrNull { !it.effectiveFrom.isAfter(date) } ?: sortedGoals.firstOrNull()
        val foodsByDate = foods.groupBy { it.date }
        val waterByDate = water.associate { it.date to it.total }
        val days = if (to.isBefore(from)) emptyMap() else
            (0..ChronoUnit.DAYS.between(from, to)).map { from.plusDays(it) }.associateWith { date ->
                val items = foodsByDate[date].orEmpty()
                val g = goalFor(date)
                DayInput(
                    date = date,
                    entryCount = items.size,
                    kcal = items.sumOf { it.kcal },
                    proteinG = items.sumOf { it.proteinG },
                    carbG = items.sumOf { it.carbG },
                    fatG = items.sumOf { it.fatG },
                    waterMl = waterByDate[date] ?: 0,
                    targetKcal = g?.targetKcal ?: 0,
                    targetProteinG = g?.proteinG ?: 0,
                    targetCarbG = g?.carbG ?: 0,
                    targetFatG = g?.fatG ?: 0,
                    targetWaterMl = if (g == null) 0 else waterOverride ?: g.waterMl,
                    tdee = g?.tdee ?: 0.0,
                )
            }
        val created = profile?.let { Instant.ofEpochMilli(it.createdAt).atZone(clock.zone).toLocalDate() }
        val firstUse = when {
            created == null -> null
            firstFood != null && firstFood.isBefore(created) -> firstFood
            else -> created
        }
        return RangeData(
            from = from,
            to = to,
            today = today(),
            firstUseDate = firstUse,
            days = days,
            foods = foods.map { FoodContribution(it.name, it.kcal, it.mealType, it.date) },
            weights = weights.sortedBy { it.date }.map { WeightPoint(it.date, it.weightKg) },
            profile = profile,
        )
    }
}

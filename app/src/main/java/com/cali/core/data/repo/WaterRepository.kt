package com.cali.core.data.repo

import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** Đặt lại lịch nhắc nước sau mỗi lần ghi (06 §4). Cài đặt ở core/notify. */
fun interface ReminderRescheduler {
    suspend fun reschedule()
}

/** Nước của một ngày: các lần uống theo giờ, tổng, mục tiêu đang dùng. */
data class WaterDay(
    val date: LocalDate,
    val entries: List<WaterEntryEntity>,
    val totalMl: Int,
    /** Mục tiêu dùng = tùy chỉnh ?: tự động. 0 khi chưa có hồ sơ. */
    val goalMl: Int,
    /** Mục tiêu tự động theo goal_snapshot của ngày (02 §8). */
    val autoGoalMl: Int,
    val goalAuto: Boolean,
)

data class WaterWeekDay(val date: LocalDate, val totalMl: Int, val goalMl: Int)

/** Nước (04 §2 water_entry, F10). Ngày của lần uống = `date`; giờ = `created_at`. */
@Singleton
class WaterRepository @Inject constructor(
    db: AppDatabase,
    private val settings: SettingsRepository,
    private val clock: Clock,
    private val rescheduler: ReminderRescheduler,
) {
    private val dao = db.waterEntryDao()
    private val goalDao = db.goalSnapshotDao()

    fun today(): LocalDate = LocalDate.now(clock)

    fun toInstant(date: LocalDate, time: LocalTime): Instant = date.atTime(time).atZone(clock.zone).toInstant()
    fun timeOf(e: WaterEntryEntity): LocalTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(e.createdAt), clock.zone).toLocalTime().withSecond(0).withNano(0)

    fun observeDay(date: LocalDate): Flow<WaterDay> =
        combine(dao.observeByDate(date), goalDao.observeForDate(date), settings.settings) { list, goal, s ->
            val auto = goal?.waterMl ?: 0
            WaterDay(
                date = date,
                entries = list,
                totalMl = list.sumOf { it.amountMl },
                goalMl = s.waterGoalOverrideMl ?: auto,
                autoGoalMl = auto,
                goalAuto = s.waterGoalOverrideMl == null,
            )
        }

    /** 7 ngày từ [weekStart] (Thứ Hai). Mục tiêu từng ngày theo snapshot của ngày đó. */
    fun observeWeek(weekStart: LocalDate): Flow<List<WaterWeekDay>> {
        val days = (0L..6L).map { weekStart.plusDays(it) }
        val goals = days.map { goalDao.observeForDate(it) }
        return combine(
            dao.observeTotalsBetween(days.first(), days.last()),
            settings.settings,
            combine(goals) { it.toList() },
        ) { totals, s, snaps ->
            val byDate = totals.associate { it.date to it.total }
            days.mapIndexed { i, d -> WaterWeekDay(d, byDate[d] ?: 0, s.waterGoalOverrideMl ?: snaps[i]?.waterMl ?: 0) }
        }
    }

    suspend fun byId(id: Long): WaterEntryEntity? = dao.byId(id)
    suspend fun totalOn(date: LocalDate): Int = dao.total(date)

    /** Thời điểm của lần uống gần nhất (mọi ngày), cho lịch nhắc. */
    suspend fun latestDrinkAt(): Instant? = dao.latest()?.let { Instant.ofEpochMilli(it.createdAt) }

    /**
     * Thêm một lần uống. [time] null: hôm nay = bây giờ, ngày cũ = 14:30 (giữa khung nhắc, mockup s15 f9).
     */
    suspend fun add(date: LocalDate, ml: Int, time: LocalTime?, source: WaterSource): Result<Long> = runCatching {
        require(ml in MIN_ML..MAX_ML) { "Ngoài khoảng $MIN_ML–$MAX_ML ml" }
        val at = when {
            time != null -> toInstant(date, time)
            date == today() -> clock.instant()
            else -> toInstant(date, PAST_DAY_TIME)
        }
        val id = dao.insert(WaterEntryEntity(date = date, amountMl = ml, source = source, createdAt = at.toEpochMilli()))
        rescheduler.reschedule()
        id
    }

    /** Sửa lượng và giờ; ngày và nguồn giữ nguyên. */
    suspend fun update(id: Long, ml: Int, time: LocalTime): Result<Unit> = runCatching {
        require(ml in MIN_ML..MAX_ML) { "Ngoài khoảng $MIN_ML–$MAX_ML ml" }
        val e = requireNotNull(dao.byId(id)) { "Không còn lần uống $id" }
        dao.update(e.copy(amountMl = ml, createdAt = toInstant(e.date, time).toEpochMilli()))
        rescheduler.reschedule()
    }

    /** Xoá, trả bản ghi cũ để Hoàn tác. */
    suspend fun delete(id: Long): Result<WaterEntryEntity> = runCatching {
        val e = requireNotNull(dao.byId(id)) { "Không còn lần uống $id" }
        dao.delete(e)
        rescheduler.reschedule()
        e
    }

    suspend fun restore(entry: WaterEntryEntity): Result<Unit> = runCatching {
        dao.insert(entry)
        rescheduler.reschedule()
    }

    companion object {
        const val MIN_ML = 10
        const val MAX_ML = 3000
        val PAST_DAY_TIME: LocalTime = LocalTime.of(14, 30)
    }
}

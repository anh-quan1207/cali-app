package com.cali.feature.water

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.WaterDayMath
import com.cali.core.calc.WaterDayStats
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.repo.WaterDay
import com.cali.core.data.repo.WaterRepository
import com.cali.core.data.repo.WaterWeekDay
import com.cali.core.notify.NotificationPermission
import com.cali.core.notify.WaterReminderScheduler
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.hm
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/** Một hàng "Các lần uống". */
data class WaterRow(val entry: WaterEntryEntity, val time: LocalTime, val reachedAt: Int?)

/** Hàng nhắc của thẻ đầu (chỉ hôm nay). */
sealed interface ReminderLine {
    data class Next(val time: LocalTime, val tomorrow: Boolean) : ReminderLine
    data class GoalMet(val time: LocalTime) : ReminderLine
    data object NoPermission : ReminderLine
    data object Off : ReminderLine
}

/** Số cho sheet "Mục tiêu nước". */
data class GoalExplain(val weightKg: Double, val since: LocalDate, val activity: ActivityLevel?, val autoMl: Int)

data class WaterUiState(
    val today: LocalDate,
    val day: WaterDay,
    val stats: WaterDayStats,
    val rows: List<WaterRow>,
    val week: List<WaterWeekDay>,
    val reminder: ReminderLine?,
    val cupSmall: Int,
    val cupLarge: Int,
    val explain: GoalExplain?,
    val nowTime: LocalTime,
) {
    val isToday get() = day.date == today
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WaterViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val water: WaterRepository,
    private val scheduler: WaterReminderScheduler,
    settings: SettingsRepository,
    profile: ProfileRepository,
    private val undo: UndoCenter,
    private val clock: Clock,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val selected = MutableStateFlow(handle.get<String>("date")?.let(LocalDate::parse) ?: water.today())
    /** Đổi khi màn quay lại (quyền thông báo có thể vừa đổi ở Cài đặt hệ thống). */
    private val tick = MutableStateFlow(0)

    val state: StateFlow<WaterUiState?> = combine(
        selected.flatMapLatest { d -> water.observeDay(d) },
        selected.flatMapLatest { d -> water.observeWeek(d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))) },
        settings.settings,
        combine(profile.profile, selected.flatMapLatest { profile.observeGoal(it) }) { p, g -> p?.activityLevel to g },
        tick,
    ) { day, week, s, (activity, goal), _ ->
        Snapshot(day, week, s.waterCupSmallMl, s.waterCupLargeMl, s.waterEnabled, s.waterStart, activity, goal)
    }.mapLatest { snap -> build(snap) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private data class Snapshot(
        val day: WaterDay, val week: List<WaterWeekDay>, val cupSmall: Int, val cupLarge: Int,
        val enabled: Boolean, val start: LocalTime, val activity: ActivityLevel?, val goal: GoalSnapshotEntity?,
    )

    private suspend fun build(s: Snapshot): WaterUiState {
        val today = water.today()
        val stats = WaterDayMath.of(s.day.entries.map { it.amountMl }, s.day.goalMl, s.cupSmall)
        val rows = s.day.entries.mapIndexed { i, e ->
            WaterRow(e, water.timeOf(e), if (i == stats.reachedIndex) stats.cumulative[i] else null)
        }
        val reminder = if (s.day.date != today) null else when {
            !s.enabled -> ReminderLine.Off
            !NotificationPermission.canPostWater(context) -> ReminderLine.NoPermission
            else -> scheduler.preview()?.let { at ->
                val local = at.atZone(clock.zone).toLocalDateTime()
                val t = local.toLocalTime()
                if (stats.total >= s.day.goalMl && s.day.goalMl > 0) ReminderLine.GoalMet(t)
                else ReminderLine.Next(t, local.toLocalDate().isAfter(today))
            } ?: ReminderLine.Off
        }
        return WaterUiState(
            today = today, day = s.day, stats = stats, rows = rows, week = s.week, reminder = reminder,
            cupSmall = s.cupSmall, cupLarge = s.cupLarge,
            explain = s.goal?.let { GoalExplain(it.weightKgUsed, it.effectiveFrom, s.activity, s.day.autoGoalMl) },
            nowTime = LocalTime.now(clock),
        )
    }

    fun refresh() { tick.value++ }

    fun goTo(date: LocalDate) {
        if (!date.isAfter(water.today())) selected.value = date
    }

    fun shift(days: Long) = goTo(selected.value.plusDays(days))

    /** Nút +nhỏ / +lớn ở chân: hôm nay = bây giờ, ngày cũ = 14:30. */
    fun quickAdd(ml: Int) = viewModelScope.launch { water.add(selected.value, ml, null, WaterSource.APP) }

    /** Nút xoá ở chân: bỏ lần vừa thêm của ngày đang xem, có Hoàn tác (G5-8). */
    fun deleteLast() = viewModelScope.launch {
        water.deleteLatest(selected.value).onSuccess { removed ->
            undo.post(UiMessage(R.string.s15_deleted, listOf(com.cali.core.ui.format.VnFormat.int(removed.amountMl), water.timeOf(removed).hm())) { water.restore(removed) })
        }
    }

    fun add(ml: Int, time: LocalTime?) = viewModelScope.launch { water.add(selected.value, ml, time, WaterSource.APP) }

    fun update(id: Long, ml: Int, time: LocalTime) = viewModelScope.launch {
        val old = water.byId(id) ?: return@launch
        water.update(id, ml, time).onSuccess {
            undo.post(UiMessage(R.string.s15_saved, listOf(time.hm())) { water.update(id, old.amountMl, water.timeOf(old)) })
        }
    }

    fun delete(id: Long) = viewModelScope.launch {
        water.delete(id).onSuccess { removed ->
            undo.post(UiMessage(R.string.s15_deleted, listOf(com.cali.core.ui.format.VnFormat.int(removed.amountMl), water.timeOf(removed).hm())) { water.restore(removed) })
        }
    }

    val messages = undo.messages

    companion object {
        const val MIN_ML = WaterRepository.MIN_ML
        const val MAX_ML = WaterRepository.MAX_ML
        val PAST_DAY_TIME: LocalTime = WaterRepository.PAST_DAY_TIME
    }
}

package com.cali.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.prefs.Settings
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.notify.NotificationPermission
import com.cali.core.notify.WaterReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class WaterSettingsUi(
    val settings: Settings,
    val goal: GoalSnapshotEntity?,
    val permitted: Boolean,
    /** Lần nhắc kế tiếp theo giờ địa phương; null khi tắt / thiếu quyền. */
    val next: LocalDateTime?,
    val today: LocalDate,
    /** Thứ Hai gửi tổng kết kế tiếp (chỉ hiển thị; GĐ6 mới đặt lịch). */
    val nextMonday: LocalDate,
) {
    val autoGoal get() = goal?.waterMl ?: 0
    val goalMl get() = settings.waterGoalOverrideMl ?: autoGoal
}

/** S43 Nước & nhắc nhở. Mọi thay đổi lưu ngay và đặt lại lịch nhắc. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WaterSettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    profile: ProfileRepository,
    private val scheduler: WaterReminderScheduler,
    private val clock: Clock,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val tick = MutableStateFlow(0)

    val ui: StateFlow<WaterSettingsUi?> = combine(settings.settings, profile.observeGoal(), tick) { s, g, _ -> s to g }
        .mapLatest { (s, g) ->
            val today = LocalDate.now(clock)
            val now = LocalDateTime.now(clock)
            var monday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            if (monday == today && !now.toLocalTime().isBefore(s.weeklyNotifyTime)) monday = monday.plusWeeks(1)
            WaterSettingsUi(
                settings = s,
                goal = g,
                permitted = NotificationPermission.canPostWater(context),
                next = scheduler.preview()?.atZone(clock.zone)?.toLocalDateTime(),
                today = today,
                nextMonday = monday,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Quay lại màn (vừa cấp quyền ở hệ thống) → đọc lại quyền và đặt lại lịch. */
    fun refresh() = viewModelScope.launch {
        scheduler.reschedule()
        tick.value++
    }

    private fun save(transform: (Settings) -> Settings) = viewModelScope.launch {
        settings.update(transform).onSuccess { scheduler.reschedule(); tick.value++ }
    }

    fun setGoalAuto(auto: Boolean, current: Int) = save { it.copy(waterGoalOverrideMl = if (auto) null else current) }
    fun setGoalCustom(ml: Int) = save { it.copy(waterGoalOverrideMl = ml) }
    fun setCupSmall(ml: Int) = save { it.copy(waterCupSmallMl = ml) }
    fun setCupLarge(ml: Int) = save { it.copy(waterCupLargeMl = ml) }
    fun setEnabled(v: Boolean) = save { it.copy(waterEnabled = v) }
    fun setStart(t: LocalTime) = save { it.copy(waterStart = t) }
    fun setEnd(t: LocalTime) = save { it.copy(waterEnd = t) }
    fun setInterval(min: Int) = save { it.copy(waterIntervalMin = min) }

    // Tổng kết tuần: GĐ5 chỉ lưu khoá, GĐ6 đặt lịch (09 §4 GĐ5).
    fun setWeeklyEnabled(v: Boolean) = viewModelScope.launch { settings.update { it.copy(weeklyNotifyEnabled = v) } }
    fun setWeeklyTime(t: LocalTime) = viewModelScope.launch { settings.update { it.copy(weeklyNotifyTime = t) } }
    fun setWeeklyAi(v: Boolean) = viewModelScope.launch { settings.update { it.copy(weeklyAiAuto = v) } }

    companion object {
        val INTERVALS = listOf(45, 60, 90, 120)
        /** Mục tiêu tùy chỉnh trong khoảng kẹp của 02 §8. */
        const val GOAL_MIN = 1500
        const val GOAL_MAX = 4000
        const val CUP_MIN = 10
        const val CUP_MAX = 3000
    }
}

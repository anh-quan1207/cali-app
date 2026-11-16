package com.cali.core.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cali.core.calc.WaterReminderInput
import com.cali.core.calc.WaterReminderPlanner
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ReminderRescheduler
import com.cali.core.data.repo.WaterRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Đặt **một** alarm nhắc nước tại lần kế tiếp (06 §4), bằng `setAndAllowWhileIdle`:
 * không cần exact alarm, lệch vài phút chấp nhận được. Logic giờ ở [WaterReminderPlanner].
 */
@Singleton
class WaterReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    // Lazy: WaterRepository cũng phụ thuộc vào scheduler (gọi reschedule sau mỗi lần ghi).
    private val water: Lazy<WaterRepository>,
    private val state: ReminderStateStore,
    private val clock: Clock,
) : ReminderRescheduler {

    private val alarms = context.getSystemService(AlarmManager::class.java)
    private val lock = Mutex()

    /** Đầu vào planner theo trạng thái hiện tại. */
    suspend fun input(): WaterReminderInput {
        val s = settings.current()
        val repo = water.get()
        val day = repo.observeDay(repo.today()).first()
        return WaterReminderInput(
            enabled = s.waterEnabled,
            permitted = NotificationPermission.canPostWater(context),
            start = s.waterStart,
            end = s.waterEnd,
            intervalMin = s.waterIntervalMin,
            goalMl = day.goalMl.takeIf { it > 0 } ?: Int.MAX_VALUE,
            totalTodayMl = day.totalMl,
            lastDrinkAt = repo.latestDrinkAt(),
            lastReminderAt = state.lastReminderAt,
            snoozeUntil = state.snoozeUntil,
        )
    }

    /** Lần nhắc kế tiếp (không đặt alarm), cho dòng "Nhắc tiếp lúc …" ở S15 / S43. */
    suspend fun preview(): Instant? = WaterReminderPlanner.next(clock.instant(), clock.zone, input())

    override suspend fun reschedule() {
        lock.withLock {
            val next = WaterReminderPlanner.next(clock.instant(), clock.zone, input())
            val pi = alarmIntent()
            alarms.cancel(pi)
            state.scheduledAt = next
            if (next != null) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toEpochMilli(), pi)
        }
    }

    /** "Để sau" trên thông báo: +30 phút. */
    suspend fun snooze() {
        state.snoozeUntil = clock.instant().plusSeconds(WaterReminderPlanner.SNOOZE_MIN * 60)
        reschedule()
    }

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE,
        Intent(context, WaterAlarmReceiver::class.java).setAction(WaterAlarmReceiver.ACTION_FIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val REQUEST_CODE = 2001
    }
}

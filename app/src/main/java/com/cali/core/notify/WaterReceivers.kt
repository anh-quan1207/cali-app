package com.cali.core.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cali.core.calc.WaterReminderPlanner
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.WaterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Việc của các receiver nhắc nước, tách khỏi BroadcastReceiver để test bằng Robolectric. */
@Singleton
class WaterReminderActions @Inject constructor(
    private val scheduler: WaterReminderScheduler,
    private val water: WaterRepository,
    private val settings: SettingsRepository,
    private val notifier: WaterNotifier,
    private val state: ReminderStateStore,
    private val clock: Clock,
) {
    /** Chuông reo: kiểm tra lại điều kiện → hiện thông báo → đặt lần kế tiếp. Trả true nếu đã hiện. */
    suspend fun onAlarm(): Boolean {
        val now = clock.instant()
        val show = WaterReminderPlanner.shouldNotify(now, clock.zone, scheduler.input())
        if (show) {
            val day = water.observeDay(water.today()).first()
            notifier.showReminder(day.totalMl, day.goalMl, settings.current().waterCupSmallMl)
            state.lastReminderAt = now
            state.snoozeUntil = null
        }
        scheduler.reschedule()
        return show
    }

    /** "+{nút nhỏ} ml" trên thông báo: ghi nguồn NOTIFICATION, thay thông báo bằng dòng xác nhận. */
    suspend fun onAdd(): Result<Long> {
        val cup = settings.current().waterCupSmallMl
        val today = water.today()
        state.snoozeUntil = null // uống rồi thì bỏ "Để sau"; add() tự đặt lại lịch
        return water.add(today, cup, null, WaterSource.NOTIFICATION).onSuccess {
            val day = water.observeDay(today).first()
            notifier.showLogged(cup, day.totalMl, day.goalMl)
        }
    }

    /** "Để sau": ẩn thông báo, nhắc lại sau 30 phút. */
    suspend fun onSnooze() {
        notifier.cancel()
        scheduler.snooze()
    }

    suspend fun onSystemChange() = scheduler.reschedule()
}

/** Chạy việc suspend trong receiver bằng goAsync (tối đa ~10 giây). */
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        try { block() } finally { pending.finish() }
    }
}

/** Chuông nhắc reo (06 §4). */
@AndroidEntryPoint
class WaterAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var actions: WaterReminderActions

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_FIRE) runAsync { actions.onAlarm() }
    }

    companion object {
        const val ACTION_FIRE = "com.cali.water.FIRE"
    }
}

/** Nút trên thông báo: "+{nút nhỏ} ml" và "Để sau" (05 §3). */
@AndroidEntryPoint
class WaterActionReceiver : BroadcastReceiver() {
    @Inject lateinit var actions: WaterReminderActions

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ADD -> runAsync { actions.onAdd() }
            ACTION_SNOOZE -> runAsync { actions.onSnooze() }
        }
    }

    companion object {
        const val ACTION_ADD = "com.cali.water.ADD"
        const val ACTION_SNOOZE = "com.cali.water.SNOOZE"
    }
}

/** Khởi động lại máy, cài bản mới, đổi giờ / múi giờ → đặt lại lịch (06 §4). */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var actions: WaterReminderActions

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in ACTIONS) runAsync { actions.onSystemChange() }
    }

    companion object {
        val ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}

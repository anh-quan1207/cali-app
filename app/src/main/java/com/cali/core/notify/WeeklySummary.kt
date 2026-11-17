package com.cali.core.notify

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cali.MainActivity
import com.cali.R
import com.cali.core.calc.WeekGrade
import com.cali.core.calc.WeeklyStatsCalculator
import com.cali.core.calc.WeeklySummaryPlanner
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.StatsRepository
import com.cali.core.ui.format.VnFormat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thông báo tổng kết tuần sáng Thứ Hai (08 §1): một alarm `setAndAllowWhileIdle` tới Thứ Hai kế tiếp lúc giờ đã chọn ở S43.
 * Điểm tính trên máy, không gọi AI.
 */
@Singleton
class WeeklySummaryScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val clock: Clock,
) {
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private val lock = Mutex()

    /** Lần báo kế tiếp, null khi tắt. */
    suspend fun next(): LocalDateTime? {
        val s = settings.current()
        if (!s.weeklyNotifyEnabled) return null
        return WeeklySummaryPlanner.nextFire(LocalDateTime.now(clock), s.weeklyNotifyTime)
    }

    suspend fun reschedule() {
        lock.withLock {
            val pi = alarmIntent()
            alarms.cancel(pi)
            val at = next() ?: return
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.atZone(clock.zone).toInstant().toEpochMilli(), pi)
        }
    }

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE,
        Intent(context, WeeklyAlarmReceiver::class.java).setAction(WeeklyAlarmReceiver.ACTION_FIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val REQUEST_CODE = 2101
    }
}

/** Kênh "Tổng kết tuần" và nội dung thông báo. */
@Singleton
class WeeklyNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stats: StatsRepository,
    private val clock: Clock,
) {
    private val nm = NotificationManagerCompat.from(context)

    fun ensureChannel() {
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.weekly_channel))
                .setDescription(context.getString(R.string.weekly_channel_desc))
                .build(),
        )
    }

    fun canPost(): Boolean {
        if (!NotificationPermission.runtimeGranted(context) || !nm.areNotificationsEnabled()) return false
        val ch = nm.getNotificationChannelCompat(CHANNEL_ID) ?: return true
        return ch.importance != NotificationManagerCompat.IMPORTANCE_NONE
    }

    /** "Tổng kết tuần 10/11 – 16/11 đã sẵn sàng · Điểm 71/100". Trả false nếu không có quyền. */
    @SuppressLint("MissingPermission")
    suspend fun show(): Boolean {
        if (!canPost()) return false
        ensureChannel()
        val today = LocalDate.now(clock)
        val week = WeeklySummaryPlanner.reviewedWeek(today)
        val end = week.plusDays(6)
        val data = stats.load(week, end)
        val s = WeeklyStatsCalculator.compute(week, today, data.daysIn(week, end), firstUseDate = data.firstUseDate)
        val body = if (s.loggedDays == 0) {
            context.getString(R.string.weekly_notif_body_empty)
        } else {
            context.getString(R.string.weekly_notif_body, s.score.total, context.getString(gradeRes(s.grade)))
        }
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_weekly)
            .setContentTitle(context.getString(R.string.weekly_notif_title, VnFormat.dayMonth(week), VnFormat.dayMonth(end)))
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context, REQUEST_OPEN,
                    Intent(context, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_OPEN_WEEKLY, week.toString())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
        nm.notify(NOTIFICATION_ID, n)
        return true
    }

    private fun gradeRes(g: WeekGrade) = when (g) {
        WeekGrade.EXCELLENT -> R.string.s35_grade_excellent
        WeekGrade.GOOD -> R.string.s35_grade_good
        WeekGrade.NEEDS_WORK -> R.string.s35_grade_needs
        WeekGrade.POOR -> R.string.s35_grade_poor
    }

    companion object {
        const val CHANNEL_ID = "weekly_summary"
        const val NOTIFICATION_ID = 1101
        private const val REQUEST_OPEN = 2102
    }
}

/** Chuông Thứ Hai: hiện thông báo rồi hẹn tuần sau. */
@AndroidEntryPoint
class WeeklyAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var notifier: WeeklyNotifier
    @Inject lateinit var scheduler: WeeklySummaryScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                runCatching { notifier.show() }
                runCatching { scheduler.reschedule() }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.cali.weekly.FIRE"
    }
}

package com.cali.core.notify

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.prefs.runTestWithScope
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.repo.TEST_PROFILE
import com.cali.core.data.repo.TEST_TODAY
import com.cali.core.data.repo.TEST_ZONE
import com.cali.core.data.repo.WaterRepository
import com.cali.core.data.repo.newSettings
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Đồng hồ chỉnh được giữa các bước của một test. */
private class MutableClock(var now: Instant, private val zoneId: ZoneId) : Clock() {
    override fun getZone(): ZoneId = zoneId
    override fun withZone(zone: ZoneId): Clock = MutableClock(now, zone)
    override fun instant(): Instant = now
}

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class) // không chạy CaliApp (Hilt) trong test này
class WaterReminderSchedulerTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private val app: Application = ApplicationProvider.getApplicationContext()
    private val alarms get() = shadowOf(app.getSystemService(AlarmManager::class.java))
    private val notifications get() = shadowOf(app.getSystemService(NotificationManager::class.java))

    private fun at(h: Int, m: Int) = LocalDateTime.of(TEST_TODAY, LocalTime.of(h, m)).atZone(TEST_ZONE).toInstant()
    private fun local(millis: Long) = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TEST_ZONE)

    private class Env(
        val clock: MutableClock,
        val settings: SettingsRepository,
        val water: WaterRepository,
        val scheduler: WaterReminderScheduler,
        val state: ReminderStateStore,
        val actions: WaterReminderActions,
    )

    private suspend fun env(name: String, now: Instant, settings: SettingsRepository): Env {
        val clock = MutableClock(now, TEST_ZONE)
        ProfileRepository(db, settings, clock).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val state = ReminderStateStore(app).also { it.clear() }
        lateinit var scheduler: WaterReminderScheduler
        val water = WaterRepository(db, settings, clock) { scheduler.reschedule() }
        scheduler = WaterReminderScheduler(app, settings, { water }, state, clock)
        val notifier = WaterNotifier(app).also { it.ensureChannel() }
        return Env(clock, settings, water, scheduler, state, WaterReminderActions(scheduler, water, settings, notifier, state, clock))
    }

    @Before fun grant() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test fun `ghi nuoc trong app dat alarm 14h05 theo mockup`() = runTestWithScope {
        val e = env("a", at(12, 38), newSettings(tmp, "a"))
        e.state.lastReminderAt = at(10, 10)
        e.water.add(TEST_TODAY, 250, LocalTime.of(7, 5), WaterSource.APP).getOrThrow()
        e.water.add(TEST_TODAY, 500, LocalTime.of(8, 40), WaterSource.APP).getOrThrow()
        e.water.add(TEST_TODAY, 250, LocalTime.of(10, 20), WaterSource.NOTIFICATION).getOrThrow()
        e.water.add(TEST_TODAY, 250, LocalTime.of(12, 35), WaterSource.APP).getOrThrow()

        assertEquals(1, alarms.scheduledAlarms.size) // chỉ một alarm
        val alarm = alarms.nextScheduledAlarm!!
        assertEquals(AlarmManager.RTC_WAKEUP, alarm.type)
        assertEquals(LocalDateTime.of(TEST_TODAY, LocalTime.of(14, 5)), local(alarm.triggerAtTime))
        assertTrue(alarm.isAllowWhileIdle)
    }

    @Test fun `tat nhac thi huy alarm`() = runTestWithScope {
        val e = env("b", at(12, 38), newSettings(tmp, "b"))
        e.scheduler.reschedule()
        assertEquals(1, alarms.scheduledAlarms.size)
        e.settings.update { it.copy(waterEnabled = false) }.getOrThrow()
        e.scheduler.reschedule()
        assertTrue(alarms.scheduledAlarms.isEmpty())
        assertNull(e.state.scheduledAt)
    }

    @Test fun `thieu quyen thong bao thi khong dat alarm`() = runTestWithScope {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val e = env("c", at(12, 38), newSettings(tmp, "c"))
        e.scheduler.reschedule()
        assertTrue(alarms.scheduledAlarms.isEmpty())
    }

    @Test fun `chuong reo trong khung thi hien thong bao va hen lan sau`() = runTestWithScope {
        val e = env("d", at(14, 5), newSettings(tmp, "d"))
        e.water.add(TEST_TODAY, 1250, LocalTime.of(12, 35), WaterSource.APP).getOrThrow()
        assertTrue(e.actions.onAlarm())
        assertEquals(1, notifications.allNotifications.size)
        assertEquals(at(14, 5), e.state.lastReminderAt)
        assertEquals(LocalDateTime.of(TEST_TODAY, LocalTime.of(15, 35)), local(alarms.nextScheduledAlarm!!.triggerAtTime))
    }

    @Test fun `chuong reo ngoai khung gio thi khong hien`() = runTestWithScope {
        val e = env("e", at(22, 30), newSettings(tmp, "e"))
        assertFalse(e.actions.onAlarm())
        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(LocalDateTime.of(TEST_TODAY.plusDays(1), LocalTime.of(7, 0)), local(alarms.nextScheduledAlarm!!.triggerAtTime))
    }

    @Test fun `bam +250 tren thong bao ghi nguon NOTIFICATION va doi lich`() = runTestWithScope {
        val s = newSettings(tmp, "f").also { it.update { x -> x.copy(waterGoalOverrideMl = null) }.getOrThrow() } // mục tiêu tự động 2.600 như mockup
        val e = env("f", at(14, 5), s)
        e.water.add(TEST_TODAY, 1250, LocalTime.of(12, 35), WaterSource.APP).getOrThrow()
        e.actions.onAlarm()
        e.clock.now = at(14, 6)
        e.actions.onAdd().getOrThrow()

        val day = e.water.observeDay(TEST_TODAY).first()
        assertEquals(1500, day.totalMl)
        assertEquals(WaterSource.NOTIFICATION, day.entries.last().source)
        assertEquals(LocalDateTime.of(TEST_TODAY, LocalTime.of(15, 36)), local(alarms.nextScheduledAlarm!!.triggerAtTime))
    }

    @Test fun `de sau nhac lai sau 30 phut`() = runTestWithScope {
        val e = env("g", at(14, 5), newSettings(tmp, "g"))
        e.water.add(TEST_TODAY, 1250, LocalTime.of(12, 35), WaterSource.APP).getOrThrow()
        e.actions.onAlarm()
        e.actions.onSnooze()
        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(LocalDateTime.of(TEST_TODAY, LocalTime.of(14, 35)), local(alarms.nextScheduledAlarm!!.triggerAtTime))

        // 14:35 chuông reo lại: hiện thông báo dù chưa đủ 90 phút từ lần nhắc trước.
        e.clock.now = at(14, 35)
        assertTrue(e.actions.onAlarm())
    }

    @Test fun `khoi dong lai may dat lai alarm`() = runTestWithScope {
        val e = env("h", at(12, 38), newSettings(tmp, "h"))
        e.state.lastReminderAt = at(12, 0)
        // Khởi động lại xoá mọi alarm.
        assertTrue(alarms.scheduledAlarms.isEmpty())
        e.actions.onSystemChange()
        assertEquals(LocalDateTime.of(TEST_TODAY, LocalTime.of(13, 30)), local(alarms.nextScheduledAlarm!!.triggerAtTime))
    }

    @Test fun `da du muc tieu thi hen 7h sang mai`() = runTestWithScope {
        val e = env("i", at(21, 30), newSettings(tmp, "i"))
        e.water.add(TEST_TODAY, 2850, LocalTime.of(21, 10), WaterSource.APP).getOrThrow()
        assertEquals(LocalDateTime.of(TEST_TODAY.plusDays(1), LocalTime.of(7, 0)), local(alarms.nextScheduledAlarm!!.triggerAtTime))
    }

}

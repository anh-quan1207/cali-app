package com.cali.core.data.repo

import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.runTestWithScope
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class WaterRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private class CountingRescheduler : ReminderRescheduler {
        var calls = 0
        override suspend fun reschedule() { calls++ }
    }

    private fun t(h: Int, m: Int) = LocalTime.of(h, m)

    @Test fun `bon lan uong mockup tong 1250 tren muc tieu 2600 cua ngay`() = runTestWithScope {
        val settings = newSettings(tmp, "a")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        settings.update { it.copy(waterGoalOverrideMl = null) } // chế độ tự động theo hồ sơ
        val r = CountingRescheduler()
        val repo = WaterRepository(db, settings, TEST_CLOCK, r)

        repo.add(TEST_TODAY, 250, t(7, 5), WaterSource.APP).getOrThrow()
        repo.add(TEST_TODAY, 500, t(8, 40), WaterSource.APP).getOrThrow()
        repo.add(TEST_TODAY, 250, t(10, 20), WaterSource.NOTIFICATION).getOrThrow()
        repo.add(TEST_TODAY, 250, t(12, 35), WaterSource.APP).getOrThrow()

        val day = repo.observeDay(TEST_TODAY).first()
        assertEquals(1250, day.totalMl)
        assertEquals(2600, day.goalMl)
        assertTrue(day.goalAuto)
        assertEquals(listOf(t(7, 5), t(8, 40), t(10, 20), t(12, 35)), day.entries.map { repo.timeOf(it) })
        assertEquals(WaterSource.NOTIFICATION, day.entries[2].source)
        assertEquals(4, r.calls) // mỗi lần ghi đều đặt lại lịch nhắc (06 §4)
        assertEquals(repo.toInstant(TEST_TODAY, t(12, 35)), repo.latestDrinkAt())
    }

    @Test fun `them khong gio thi lay bay gio, ngoai khoang 10-3000 bi tu choi`() = runTestWithScope {
        val repo = WaterRepository(db, newSettings(tmp, "b"), TEST_CLOCK, CountingRescheduler())
        val id = repo.add(TEST_TODAY, 330, null, WaterSource.APP).getOrThrow()
        assertEquals(t(12, 38), repo.timeOf(repo.byId(id)!!))
        assertTrue(repo.add(TEST_TODAY, 5, null, WaterSource.APP).isFailure)
        assertTrue(repo.add(TEST_TODAY, 5000, null, WaterSource.APP).isFailure)
        assertEquals(330, repo.observeDay(TEST_TODAY).first().totalMl)
    }

    @Test fun `ngay cu khong gio thi ghi luc 14h30`() = runTestWithScope {
        val repo = WaterRepository(db, newSettings(tmp, "c"), TEST_CLOCK, CountingRescheduler())
        val y = TEST_TODAY.minusDays(1)
        val id = repo.add(y, 250, null, WaterSource.APP).getOrThrow()
        val e = repo.byId(id)!!
        assertEquals(y, e.date)
        assertEquals(t(14, 30), repo.timeOf(e))
    }

    @Test fun `sua luong va gio, giu ngay va nguon`() = runTestWithScope {
        val repo = WaterRepository(db, newSettings(tmp, "d"), TEST_CLOCK, CountingRescheduler())
        val id = repo.add(TEST_TODAY, 500, t(8, 40), WaterSource.NOTIFICATION).getOrThrow()
        repo.update(id, 400, t(8, 50)).getOrThrow()
        val e = repo.byId(id)!!
        assertEquals(400, e.amountMl)
        assertEquals(t(8, 50), repo.timeOf(e))
        assertEquals(WaterSource.NOTIFICATION, e.source)
        assertTrue(repo.update(id, 0, t(8, 50)).isFailure)
    }

    @Test fun `xoa roi hoan tac tra lai dung ban ghi`() = runTestWithScope {
        val repo = WaterRepository(db, newSettings(tmp, "e"), TEST_CLOCK, CountingRescheduler())
        repo.add(TEST_TODAY, 250, t(7, 5), WaterSource.APP).getOrThrow()
        val id = repo.add(TEST_TODAY, 250, t(10, 20), WaterSource.NOTIFICATION).getOrThrow()

        val removed = repo.delete(id).getOrThrow()
        assertEquals(250, repo.observeDay(TEST_TODAY).first().totalMl)

        repo.restore(removed).getOrThrow()
        val day = repo.observeDay(TEST_TODAY).first()
        assertEquals(500, day.totalMl)
        assertEquals(removed, day.entries.single { it.id == id })
    }

    @Test fun `muc tieu tuy chinh thang muc tieu tu dong`() = runTestWithScope {
        val settings = newSettings(tmp, "f")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = WaterRepository(db, settings, TEST_CLOCK, CountingRescheduler())
        settings.update { it.copy(waterGoalOverrideMl = 3000) }.getOrThrow()
        val day = repo.observeDay(TEST_TODAY).first()
        assertEquals(3000, day.goalMl)
        assertFalse(day.goalAuto)
        assertEquals(2600, day.autoGoalMl)
    }

    @Test fun `tuan T2 den CN tong tung ngay`() = runTestWithScope {
        val settings = newSettings(tmp, "g")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = WaterRepository(db, settings, TEST_CLOCK, CountingRescheduler())
        // TEST_TODAY 16/11/2026 là Thứ Hai.
        repo.add(TEST_TODAY, 1250, t(9, 0), WaterSource.APP).getOrThrow()
        repo.add(TEST_TODAY.minusDays(1), 2700, t(9, 0), WaterSource.APP).getOrThrow()

        val week = repo.observeWeek(TEST_TODAY).first()
        assertEquals(7, week.size)
        assertEquals(TEST_TODAY, week.first().date)
        assertEquals(1250, week.first().totalMl)
        assertEquals(0, week[1].totalMl)
        assertEquals(1500, week.first().goalMl) // mặc định 1.500 (G5-8)
        settings.update { it.copy(waterGoalOverrideMl = null) }.getOrThrow()
        assertEquals(2600, repo.observeWeek(TEST_TODAY).first().first().goalMl) // tự động theo snapshot
    }

    @Test fun `mac dinh muc tieu 1500 ml, khong tinh theo ho so (09 §8 G5-8)`() = runTestWithScope {
        val settings = newSettings(tmp, "g1500")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val day = WaterRepository(db, settings, TEST_CLOCK, CountingRescheduler()).observeDay(TEST_TODAY).first()
        assertEquals(1500, day.goalMl)
        assertEquals(2600, day.autoGoalMl)
        assertFalse(day.goalAuto)
    }

    @Test fun `xoa lan vua them cua ngay, tra ban ghi de Hoan tac`() = runTestWithScope {
        val r = CountingRescheduler()
        val repo = WaterRepository(db, newSettings(tmp, "dl"), TEST_CLOCK, r)
        val y = TEST_TODAY.minusDays(1)
        repo.add(TEST_TODAY, 250, t(9, 0), WaterSource.APP).getOrThrow()
        repo.add(TEST_TODAY, 500, t(11, 0), WaterSource.APP).getOrThrow()
        repo.add(TEST_TODAY, 300, t(10, 0), WaterSource.APP).getOrThrow() // thêm sau cùng, giờ sớm hơn
        repo.add(y, 400, t(20, 0), WaterSource.APP).getOrThrow() // ngày khác

        val removed = repo.deleteLatest(TEST_TODAY).getOrThrow()
        assertEquals(300, removed.amountMl) // lần bấm gần nhất, không phải giờ muộn nhất
        assertEquals(750, repo.observeDay(TEST_TODAY).first().totalMl)
        assertEquals(400, repo.observeDay(y).first().totalMl)

        repo.restore(removed).getOrThrow()
        assertEquals(1050, repo.observeDay(TEST_TODAY).first().totalMl)
        assertEquals(6, r.calls)
    }

    @Test fun `xoa lan vua them khi ngay chua uong thi tra loi, khong nem`() = runTestWithScope {
        val repo = WaterRepository(db, newSettings(tmp, "de"), TEST_CLOCK, CountingRescheduler())
        assertTrue(repo.deleteLatest(TEST_TODAY).isFailure)
    }
}

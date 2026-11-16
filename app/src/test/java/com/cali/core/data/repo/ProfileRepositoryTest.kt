package com.cali.core.data.repo

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MacroPct
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.prefs.runTestWithScope
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ProfileRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val today = LocalDate.of(2026, 11, 16)
    private val clock = Clock.fixed(today.atTime(9, 0).atZone(zone).toInstant(), zone)

    /** 02 §10: Nam, sinh 1996 (30 tuổi năm 2026), 170 cm, 75 kg, vận động nhẹ, giảm 0,5 kg/tuần → 68 kg. */
    private val input = ProfileInput(Sex.MALE, 1996, 170.0, ActivityLevel.LIGHT, GoalType.LOSE, 68.0, 0.5)

    @Test fun `onboarding luu ho so, can hom nay va muc tieu 1740`() = runTestWithScope {
        val settings = SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("a.preferences_pb").also { it.delete() } })
        val repo = ProfileRepository(db, settings, clock)

        val preview = repo.preview(input, 75.0)
        assertEquals(1740, preview.result.target.targetKcal)
        assertEquals(14, preview.weeks)
        assertEquals(LocalDate.of(2027, 2, 22), preview.etaDate) // 16/11 + 98 ngày

        val s = repo.completeOnboarding(input, 75.0).getOrThrow()
        assertEquals(today, s.effectiveFrom)
        assertEquals(1740, s.targetKcal)
        assertEquals(122, s.proteinG)
        assertEquals(196, s.carbG)
        assertEquals(52, s.fatG)
        assertEquals(2600, s.waterMl)
        assertNull(s.clampReason)

        assertEquals(75.0, repo.latestWeight.first()!!.weightKg, 0.0)
        assertEquals(today, repo.latestWeight.first()!!.date)
        assertEquals(1996, repo.profile.first()!!.birthYear)
        assertEquals(1740, repo.observeGoal(today).first()!!.targetKcal)
        assertTrue(settings.current().onboardingDone)
    }

    @Test fun `ap dung trong cung ngay ghi de snapshot, tinh theo lan can moi nhat`() = runTestWithScope {
        val settings = SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("b.preferences_pb").also { it.delete() } })
        val repo = ProfileRepository(db, settings, clock)
        repo.completeOnboarding(input, 75.0).getOrThrow()
        db.weightEntryDao().put(WeightEntryEntity(date = today, weightKg = 74.1, createdAt = 1))

        // Mockup S41: đổi tốc độ 0,5 → 0,75 ở 74,1 kg → kẹp 1.710
        val s = repo.apply(input.copy(weeklyRateKg = 0.75), autoRecalcOnWeighIn = false, kcalOverride = null, macroOverride = null).getOrThrow()
        assertEquals(1710, s.targetKcal)
        assertEquals(ClampReason.MAX_DEFICIT, s.clampReason)
        assertEquals(74.1, s.weightKgUsed, 0.0)
        assertEquals(122, s.proteinG)
        assertEquals(190, s.carbG)
        assertEquals(51, s.fatG)
        assertEquals(1710, repo.observeGoal(today).first()!!.targetKcal)
        assertEquals(0.75, repo.profile.first()!!.weeklyRateKg, 0.0)
    }

    @Test fun `ghi de 1400 va macro 30-40-30 ra 1710 va 128-171-57`() = runTestWithScope {
        val settings = SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("c.preferences_pb").also { it.delete() } })
        val repo = ProfileRepository(db, settings, clock)
        repo.completeOnboarding(input, 75.0).getOrThrow()
        db.weightEntryDao().put(WeightEntryEntity(date = today, weightKg = 74.1, createdAt = 1))

        val s = repo.apply(input, false, kcalOverride = 1400, macroOverride = MacroPct(30, 40, 30)).getOrThrow()
        assertEquals(1710, s.targetKcal)
        assertTrue(s.isKcalOverridden)
        assertTrue(s.isMacroOverridden)
        assertEquals(listOf(128, 171, 57), listOf(s.proteinG, s.carbG, s.fatG))
        assertEquals(1400, settings.current().kcalOverride)

        // Tắt ghi đè → quay về tự tính
        val back = repo.apply(input, false, null, null).getOrThrow()
        assertEquals(1730, back.targetKcal)
        assertFalse(back.isKcalOverridden)
        assertNull(settings.current().kcalOverride)
    }

    @Test fun `giu can bo qua can muc tieu va toc do`() = runTestWithScope {
        val settings = SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("d.preferences_pb").also { it.delete() } })
        val repo = ProfileRepository(db, settings, clock)
        val maintain = input.copy(goalType = GoalType.MAINTAIN)
        val p = repo.preview(maintain, 75.0)
        assertNull(p.weeks)
        assertNull(p.etaDate)
        assertEquals(2290, p.result.target.targetKcal) // TDEE 2.292,8
        repo.completeOnboarding(maintain, 75.0).getOrThrow()
        assertNull(repo.profile.first()!!.targetWeightKg)
        assertEquals(0.0, repo.profile.first()!!.weeklyRateKg, 0.0)
    }
}

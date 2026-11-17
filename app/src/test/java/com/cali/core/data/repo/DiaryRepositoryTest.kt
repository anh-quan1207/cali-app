package com.cali.core.data.repo

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cali.core.calc.FoodValues
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.TextNormalizer
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.prefs.TestScopeHolder
import com.cali.core.data.prefs.runTestWithScope
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

internal val TEST_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
internal val TEST_TODAY: LocalDate = LocalDate.of(2026, 11, 16)
internal val TEST_CLOCK: Clock = Clock.fixed(TEST_TODAY.atTime(12, 38).atZone(TEST_ZONE).toInstant(), TEST_ZONE)

/** 02 §10: Nam, 1996, 170 cm, 75 kg, vận động nhẹ, giảm 0,5 kg/tuần → 68 kg. */
internal val TEST_PROFILE = ProfileInput(Sex.MALE, 1996, 170.0, ActivityLevel.LIGHT, GoalType.LOSE, 68.0, 0.5)

internal fun TestScopeHolder.newSettings(tmp: TemporaryFolder, name: String) =
    SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("$name.preferences_pb").also { it.delete() } })

internal fun food(name: String, kcal: Double, p: Double = 0.0, c: Double = 0.0, f: Double = 0.0, myFoodId: Long? = null, save: Boolean = false) =
    NewFood(name = name, portionDesc = null, grams = null, values = FoodValues(kcal, p, c, f),
        source = if (myFoodId != null) FoodSource.MY_FOOD else FoodSource.MANUAL, myFoodId = myFoodId, saveAsMyFood = save)

@RunWith(RobolectricTestRunner::class)
class DiaryRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private fun at(date: LocalDate, h: Int, m: Int) = LocalDateTime.of(date, LocalTime.of(h, m))

    @Test fun `ngay mau 820 + 490 con 430 theo muc tieu cua ngay`() = runTestWithScope {
        val settings = newSettings(tmp, "a")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = DiaryRepository(db, TEST_CLOCK)

        repo.add(listOf(food("Bánh mì ốp la", 280.0), food("Xôi gà", 540.0)), at(TEST_TODAY, 7, 20), MealType.BREAKFAST, false).getOrThrow()
        repo.add(listOf(food("Bún bò Huế", 490.0, 33.0, 60.0, 12.0)), at(TEST_TODAY, 12, 35), MealType.LUNCH, false).getOrThrow()

        val day = repo.observeDay(TEST_TODAY).first()
        assertEquals(1740, day.goal!!.targetKcal)
        assertEquals(1310.0, day.summary.totalKcal, 0.0)
        assertEquals(430, day.summary.remainingKcal)
        assertEquals(3, day.entries.size)
        // Cùng một lần lưu → cùng batch_id, sort_order theo thứ tự
        val breakfast = day.entries.filter { it.mealType == MealType.BREAKFAST }
        assertEquals(1, breakfast.map { it.batchId }.distinct().size)
        assertNotNull(breakfast[0].batchId)
        assertEquals(listOf(0, 1), breakfast.map { it.sortOrder })
    }

    @Test fun `ngay truoc snapshot dau tien dung muc tieu cua snapshot som nhat (G6)`() = runTestWithScope {
        val settings = newSettings(tmp, "g6")
        ProfileRepository(db, settings, TEST_CLOCK).completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        settings.update { it.copy(waterGoalOverrideMl = null) }.getOrThrow() // mục tiêu nước tự động theo snapshot (G5-8)
        val repo = DiaryRepository(db, TEST_CLOCK)
        val old = TEST_TODAY.minusDays(5)
        repo.add(listOf(food("Phở bò", 690.0)), at(old, 8, 0), MealType.BREAKFAST, false).getOrThrow()
        val day = repo.observeDay(old).first()
        assertEquals(1740, day.goal?.targetKcal)
        assertEquals(1050, day.summary.remainingKcal)
        // Nước cũng lấy mục tiêu đó (S15 / S35 ngày cũ).
        assertEquals(2600, WaterRepository(db, settings, TEST_CLOCK) {}.observeDay(old).first().goalMl)
    }

    @Test fun `mon 23-30 thuoc ngay do, bua Phu`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        val late = at(TEST_TODAY, 23, 30)
        repo.add(listOf(food("Mì tôm", 350.0)), late, repo.mealFor(late.toLocalTime()), false).getOrThrow()

        val e = repo.observeDay(TEST_TODAY).first().entries.single()
        assertEquals(TEST_TODAY, e.date)
        assertEquals(MealType.SNACK, e.mealType)
        assertEquals(late.atZone(TEST_ZONE).toInstant().toEpochMilli(), e.eatenAt)
        assertTrue(repo.observeDay(TEST_TODAY.plusDays(1)).first().entries.isEmpty())
    }

    @Test fun `sua mon sang hom qua thi hom nay giam, hom qua tang`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        repo.add(listOf(food("Phở bò", 470.0), food("Trà đá", 0.0)), at(TEST_TODAY, 12, 0), MealType.LUNCH, false).getOrThrow()
        val pho = repo.observeDay(TEST_TODAY).first().entries.first { it.name == "Phở bò" }

        val yesterday = TEST_TODAY.minusDays(1)
        repo.update(pho, newEatenAt = at(yesterday, 19, 0), meal = MealType.DINNER, mealManual = true, values = FoodValues(705.0, 0.0, 0.0, 0.0), name = "Phở bò", portionDesc = "1½ tô", grams = null).getOrThrow()

        assertEquals(0.0, repo.observeDay(TEST_TODAY).first().summary.totalKcal, 0.0)
        val y = repo.observeDay(yesterday).first().entries.single()
        assertEquals(705.0, y.kcal, 0.0)
        assertEquals(yesterday, y.date)
        assertEquals(MealType.DINNER, y.mealType)
        assertTrue(y.mealTypeManual)
    }

    @Test fun `xoa roi hoan tac tra lai dung mon`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        repo.add(listOf(food("Bún chả", 520.0)), at(TEST_TODAY, 12, 0), MealType.LUNCH, false).getOrThrow()
        val e = repo.observeDay(TEST_TODAY).first().entries.single()

        val removed = repo.delete(e.id).getOrThrow()
        assertTrue(repo.observeDay(TEST_TODAY).first().entries.isEmpty())
        repo.restore(removed).getOrThrow()
        assertEquals(listOf(e), repo.observeDay(TEST_TODAY).first().entries)
    }

    @Test fun `them tu Mon cua toi tang use_count, danh dau luu tao mon moi`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        val id = db.myFoodDao().insert(
            MyFoodEntity(name = "Cơm tấm sườn", nameNormalized = TextNormalizer.normalize("Cơm tấm sườn"), kcal = 650.0, useCount = 20, createdAt = 0, updatedAt = 0),
        )
        repo.add(listOf(food("Cơm tấm sườn", 650.0, myFoodId = id), food("Bánh flan", 130.0, save = true)), at(TEST_TODAY, 12, 0), MealType.LUNCH, false).getOrThrow()

        val m = db.myFoodDao().byId(id)!!
        assertEquals(21, m.useCount)
        assertEquals(TEST_CLOCK.millis(), m.lastUsedAt)
        val flan = db.myFoodDao().search("banh flan").single()
        assertEquals(1, flan.useCount)
        val entries = repo.observeDay(TEST_TODAY).first().entries
        assertEquals(flan.id, entries.first { it.name == "Bánh flan" }.myFoodId)
        assertEquals(FoodSource.MY_FOOD, entries.first { it.name == "Cơm tấm sườn" }.source)
    }

    @Test fun `gan day nhom theo ngay va bo trung`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        repo.add(listOf(food("Phở bò", 470.0)), at(TEST_TODAY.minusDays(1), 7, 0), MealType.BREAKFAST, false).getOrThrow()
        repo.add(listOf(food("Trà đá", 0.0)), at(TEST_TODAY.minusDays(1), 12, 0), MealType.LUNCH, false).getOrThrow()
        repo.add(listOf(food("Trà đá", 0.0), food("Bún bò Huế", 490.0)), at(TEST_TODAY, 12, 0), MealType.LUNCH, false).getOrThrow()

        val groups = repo.recent()
        assertEquals(listOf(TEST_TODAY, TEST_TODAY.minusDays(1)), groups.map { it.first })
        assertEquals(setOf("Trà đá", "Bún bò Huế"), groups[0].second.map { it.name }.toSet())
        assertEquals(listOf("Phở bò"), groups[1].second.map { it.name })
    }

    @Test fun `mon tu AI luu do tin cay, ai_call_id va thumbnail`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        val callId = db.aiCallLogDao().insert(
            com.cali.core.data.db.entity.AiCallLogEntity(
                mode = com.cali.core.data.db.entity.AiMode.PHOTO_LOG, provider = "neuralwatt", model = "kimi-k3.1-pro",
                durationMs = 5000, success = true, createdAt = 1,
            ),
        )
        val f = food("Phở bò", 450.0).copy(source = FoodSource.AI_PHOTO, aiConfidence = 0.85, aiCallId = callId, thumbnailPath = "/t/a.jpg")
        repo.add(listOf(f), at(TEST_TODAY, 12, 35), MealType.LUNCH, false).getOrThrow()
        val e = repo.observeDay(TEST_TODAY).first().entries.single()
        assertEquals(FoodSource.AI_PHOTO, e.source)
        assertEquals(0.85, e.aiConfidence!!, 0.0)
        assertEquals(callId, e.aiCallId)
        assertEquals("/t/a.jpg", e.thumbnailPath)
    }

    @Test fun `ngay truoc khi co muc tieu thi goal null`() = runTestWithScope {
        val repo = DiaryRepository(db, TEST_CLOCK)
        assertNull(repo.observeDay(TEST_TODAY).first().goal)
    }
}

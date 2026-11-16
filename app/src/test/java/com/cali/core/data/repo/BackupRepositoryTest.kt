package com.cali.core.data.repo

import com.cali.core.calc.FoodValues
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.ChatRole
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.prefs.SettingsRepository
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
class BackupRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private suspend fun seed(settings: SettingsRepository) {
        val profile = ProfileRepository(db, settings, TEST_CLOCK)
        profile.completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        profile.setAutoRecalc(true).getOrThrow()
        val my = MyFoodRepository(db, TEST_CLOCK)
        val pho = my.create("Phở bò", "1 tô", 550.0, FoodValues(470.0, 30.0, 60.0, 12.0), favorite = true).getOrThrow()
        DiaryRepository(db, TEST_CLOCK).add(
            listOf(food("Phở bò", 470.0, 30.0, 60.0, 12.0, myFoodId = pho), food("Trà đá", 0.0)),
            LocalDateTime.of(TEST_TODAY, LocalTime.of(7, 20)), MealType.BREAKFAST, mealManual = true,
        ).getOrThrow()
        WeightRepository(db, profile, settings, TEST_CLOCK).save(TEST_TODAY.minusDays(2), 75.4).getOrThrow()
        db.waterEntryDao().insert(WaterEntryEntity(date = TEST_TODAY, amountMl = 250, source = WaterSource.APP, createdAt = 5))
        db.chatMessageDao().insert(ChatMessageEntity(date = TEST_TODAY, role = ChatRole.USER, text = "tối qua ăn mì tôm", createdAt = 6))
        settings.update { it.copy(personalNotes = "Không ăn hành", waterCupLargeMl = 400, kcalOverride = 1500) }.getOrThrow()
    }

    @Test fun `xuat roi nhap lai giong het`() = runTestWithScope {
        val settings = newSettings(tmp, "a")
        seed(settings)
        val repo = BackupRepository(db, settings, TEST_CLOCK)
        val before = repo.dump()
        val out = ByteArrayOutputStream()
        val counts = repo.export(out).getOrThrow()
        val settingsBefore = settings.current() // gồm cả lần xuất gần nhất (ghi trước khi ghi file)
        assertEquals(2, counts.foodEntries)
        val text = out.toString(Charsets.UTF_8)
        assertTrue(text.contains("\"app\": \"Cali\"") || text.contains("\"app\":\"Cali\""))
        assertFalse("không có API key", text.contains("api_key"))

        repo.wipeAll().getOrThrow()
        assertTrue(repo.dump().foodEntries.isEmpty())
        assertFalse(settings.current().onboardingDone)

        repo.import(ByteArrayInputStream(out.toByteArray())).getOrThrow()
        assertEquals(before, repo.dump())
        assertEquals(settingsBefore, settings.current())
        assertTrue(settings.current().onboardingDone)
    }

    @Test fun `nhap la thay toan bo, khong cong don`() = runTestWithScope {
        val settings = newSettings(tmp, "b")
        seed(settings)
        val repo = BackupRepository(db, settings, TEST_CLOCK)
        val out = ByteArrayOutputStream()
        repo.export(out).getOrThrow()
        val before = repo.dump()

        DiaryRepository(db, TEST_CLOCK).add(listOf(food("Món thừa", 999.0)), LocalDateTime.of(TEST_TODAY, LocalTime.NOON), MealType.LUNCH, false).getOrThrow()
        repo.import(ByteArrayInputStream(out.toByteArray())).getOrThrow()
        assertEquals(before, repo.dump())
    }

    @Test fun `schema_version cao hon bi tu choi, du lieu cu nguyen ven`() = runTestWithScope {
        val settings = newSettings(tmp, "c")
        seed(settings)
        val repo = BackupRepository(db, settings, TEST_CLOCK)
        val out = ByteArrayOutputStream()
        repo.export(out).getOrThrow()
        val before = repo.dump()
        val newer = out.toString(Charsets.UTF_8).replaceFirst(Regex("\"schema_version\":\\s*1"), "\"schema_version\": 2")

        val r = repo.import(ByteArrayInputStream(newer.toByteArray()))
        assertTrue(r.exceptionOrNull() is BackupError.NewerVersion)
        assertEquals(before, repo.dump())
    }

    @Test fun `file hong hoac khong phai cua Cali bi tu choi`() = runTestWithScope {
        val settings = newSettings(tmp, "d")
        seed(settings)
        val repo = BackupRepository(db, settings, TEST_CLOCK)
        val before = repo.dump()

        assertTrue(repo.import(ByteArrayInputStream("{khong phai json".toByteArray())).exceptionOrNull() is BackupError.Unreadable)
        assertTrue(repo.import(ByteArrayInputStream("""{"app":"Khac","schema_version":1}""".toByteArray())).exceptionOrNull() is BackupError.NotCali)
        assertEquals(before, repo.dump())
    }

    @Test fun `thong ke tren may`() = runTestWithScope {
        val settings = newSettings(tmp, "e")
        seed(settings)
        val s = BackupRepository(db, settings, TEST_CLOCK).stats().first()
        assertEquals(1, s.daysLogged)
        assertEquals(2, s.weights)
        assertEquals(1, s.myFoods)
    }

    @Test fun `ghi lan xuat gan nhat`() = runTestWithScope {
        val settings = newSettings(tmp, "f")
        seed(settings)
        val repo = BackupRepository(db, settings, TEST_CLOCK)
        repo.export(ByteArrayOutputStream(), fileName = "cali-2026-11-16.json").getOrThrow()
        assertEquals(TEST_CLOCK.millis(), settings.current().lastBackupAt)
        assertEquals("cali-2026-11-16.json", settings.current().lastBackupFile)
        assertEquals(TEST_CLOCK.millis(), settings.settings.first().lastBackupAt)
    }
}

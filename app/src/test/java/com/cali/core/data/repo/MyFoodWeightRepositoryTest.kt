package com.cali.core.data.repo

import com.cali.core.calc.FoodValues
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.DbTestBase
import com.cali.core.data.prefs.runTestWithScope
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
class MyFoodRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    private fun v(kcal: Double) = FoodValues(kcal, 0.0, 0.0, 0.0)

    @Test fun `tim khong dau xep theo so lan dung`() = runTestWithScope {
        val repo = MyFoodRepository(db, TEST_CLOCK)
        val a = repo.create("Trà sữa trân châu", "1 ly", null, v(350.0), favorite = false).getOrThrow()
        repo.create("Cơm trắng", "1 chén", null, v(200.0), false).getOrThrow()
        repo.create("Phở bò", "1 tô", null, v(470.0), false).getOrThrow()
        db.myFoodDao().markUsed(a, 1)

        assertEquals(listOf("Trà sữa trân châu", "Cơm trắng"), repo.search("tra").map { it.name })
        assertEquals(3, repo.search("  ").size)
    }

    @Test fun `yeu thich ghim dau, roi theo hay dung`() = runTestWithScope {
        val repo = MyFoodRepository(db, TEST_CLOCK)
        val a = repo.create("A", null, null, v(1.0), false).getOrThrow()
        val b = repo.create("B", null, null, v(1.0), false).getOrThrow()
        repo.create("C", null, null, v(1.0), false).getOrThrow()
        db.myFoodDao().markUsed(a, 1)
        repo.toggleFavorite(b).getOrThrow()

        assertEquals(listOf("B", "A", "C"), repo.observeAll().first().map { it.name })
        assertEquals(listOf("B"), repo.observeFavorites().first().map { it.name })
    }

    @Test fun `xoa giu nhat ky, hoan tac noi lai lien ket`() = runTestWithScope {
        val repo = MyFoodRepository(db, TEST_CLOCK)
        val diary = DiaryRepository(db, TEST_CLOCK)
        val id = repo.create("Trà sữa", "1 ly", null, v(350.0), false).getOrThrow()
        diary.add(listOf(food("Trà sữa", 350.0, myFoodId = id)), LocalDateTime.of(TEST_TODAY, java.time.LocalTime.of(15, 0)), MealType.SNACK, false).getOrThrow()
        assertEquals(1, repo.entryCount(id))

        val removed = repo.delete(id).getOrThrow()
        val e = diary.observeDay(TEST_TODAY).first().entries.single()
        assertNull(e.myFoodId)
        assertEquals(350.0, e.kcal, 0.0) // số liệu giữ nguyên

        repo.restore(removed).getOrThrow()
        assertEquals(id, diary.observeDay(TEST_TODAY).first().entries.single().myFoodId)
        assertEquals(1, repo.observeAll().first().single().useCount)
    }

    @Test fun `sua chi doi mon da luu, khong doi nhat ky`() = runTestWithScope {
        val repo = MyFoodRepository(db, TEST_CLOCK)
        val diary = DiaryRepository(db, TEST_CLOCK)
        val id = repo.create("Cơm tấm sườn", null, null, v(650.0), false).getOrThrow()
        diary.add(listOf(food("Cơm tấm sườn", 650.0, myFoodId = id)), LocalDateTime.of(TEST_TODAY, java.time.LocalTime.NOON), MealType.LUNCH, false).getOrThrow()

        val f = repo.byId(id)!!
        repo.update(f.copy(name = "Cơm tấm sườn bì", kcal = 700.0)).getOrThrow()
        val after = repo.byId(id)!!
        assertEquals("com tam suon bi", after.nameNormalized)
        assertEquals(700.0, after.kcal, 0.0)
        assertEquals(650.0, diary.observeDay(TEST_TODAY).first().entries.single().kcal, 0.0)
    }
}

@RunWith(RobolectricTestRunner::class)
class WeightRepositoryTest : DbTestBase() {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun `mot ban ghi moi ngay, ghi lai la thay`() = runTestWithScope {
        val settings = newSettings(tmp, "a")
        val profile = ProfileRepository(db, settings, TEST_CLOCK)
        profile.completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = WeightRepository(db, profile, settings, TEST_CLOCK)

        assertTrue(repo.hasEntry(TEST_TODAY))
        val r = repo.save(TEST_TODAY, 74.1).getOrThrow()
        assertTrue(r.replaced)
        assertEquals(listOf(74.1), repo.observeAll().first().map { it.weightKg })
    }

    @Test fun `tu tinh lai bat thi ap dung ngay, 74,1 kg ra 1730`() = runTestWithScope {
        val settings = newSettings(tmp, "b")
        val profile = ProfileRepository(db, settings, TEST_CLOCK)
        profile.completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        profile.setAutoRecalc(true).getOrThrow()
        val repo = WeightRepository(db, profile, settings, TEST_CLOCK)

        val r = repo.save(TEST_TODAY, 74.1).getOrThrow()
        assertEquals(1730, r.applied!!.targetKcal)
        assertNull(r.proposal)
        assertEquals(1730, profile.observeGoal(TEST_TODAY).first()!!.targetKcal)
    }

    @Test fun `tu tinh lai tat thi chi de xuat Dang dung - Moi`() = runTestWithScope {
        val settings = newSettings(tmp, "c")
        val profile = ProfileRepository(db, settings, TEST_CLOCK)
        profile.completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = WeightRepository(db, profile, settings, TEST_CLOCK)

        val r = repo.save(TEST_TODAY, 74.1).getOrThrow()
        assertNull(r.applied)
        val p = assertNotNull(r.proposal).let { r.proposal!! }
        assertEquals(1740, p.current.targetKcal)
        assertEquals(1730, p.preview.result.target.targetKcal)
        assertEquals(1740, profile.observeGoal(TEST_TODAY).first()!!.targetKcal)
    }

    @Test fun `ghi cho ngay cu khong de xuat tinh lai`() = runTestWithScope {
        val settings = newSettings(tmp, "d")
        val profile = ProfileRepository(db, settings, TEST_CLOCK)
        profile.completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = WeightRepository(db, profile, settings, TEST_CLOCK)

        val r = repo.save(TEST_TODAY.minusDays(3), 75.6).getOrThrow()
        assertFalse(r.replaced)
        assertNull(r.proposal)
        assertNull(r.applied)
        assertEquals(75.0, repo.previousBefore(TEST_TODAY.plusDays(1))!!.weightKg, 0.0)
        assertEquals(75.6, repo.previousBefore(TEST_TODAY)!!.weightKg, 0.0)
    }

    @Test fun `xoa roi hoan tac`() = runTestWithScope {
        val settings = newSettings(tmp, "e")
        val profile = ProfileRepository(db, settings, TEST_CLOCK)
        profile.completeOnboarding(TEST_PROFILE, 75.0).getOrThrow()
        val repo = WeightRepository(db, profile, settings, TEST_CLOCK)
        val e = repo.observeAll().first().single()
        repo.delete(e).getOrThrow()
        assertTrue(repo.observeAll().first().isEmpty())
        repo.restore(e).getOrThrow()
        assertEquals(listOf(e), repo.observeAll().first())
    }
}

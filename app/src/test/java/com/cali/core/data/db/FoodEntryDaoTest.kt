package com.cali.core.data.db

import app.cash.turbine.test
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.db.entity.MyFoodEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class FoodEntryDaoTest : DbTestBase() {

    private val today = LocalDate.of(2026, 11, 16)

    private fun entry(name: String, eatenAt: Long, date: LocalDate = today, myFoodId: Long? = null) = FoodEntryEntity(
        date = date, eatenAt = eatenAt, mealType = MealType.LUNCH, name = name, kcal = 490.0,
        proteinG = 25.0, carbG = 60.0, fatG = 14.0, source = FoodSource.MANUAL, myFoodId = myFoodId,
        createdAt = 0, updatedAt = 0,
    )

    @Test fun `luu va doc theo ngay, xep theo gio an`() = runTest {
        val dao = db.foodEntryDao()
        dao.insert(entry("Cơm tấm", eatenAt = 3_000))
        dao.insert(entry("Phở bò", eatenAt = 1_000))
        dao.insert(entry("Hôm qua", eatenAt = 500, date = today.minusDays(1)))

        val list = dao.observeByDate(today).first()
        assertEquals(listOf("Phở bò", "Cơm tấm"), list.map { it.name })
        assertEquals(today, list.first().date)
        assertEquals(490.0, list.first().kcal, 0.0)
    }

    @Test fun `Flow phat lai khi them mon`() = runTest {
        val dao = db.foodEntryDao()
        dao.observeByDate(today).test {
            assertEquals(0, awaitItem().size)
            dao.insert(entry("Phở bò", 1_000))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `xoa Mon cua toi thi my_food_id thanh null, gia tri mon giu nguyen`() = runTest {
        val myFoodId = db.myFoodDao().insert(
            MyFoodEntity(name = "Phở bò", nameNormalized = "pho bo", kcal = 450.0, createdAt = 0, updatedAt = 0),
        )
        val id = db.foodEntryDao().insert(entry("Phở bò", 1_000, myFoodId = myFoodId))

        db.myFoodDao().delete(db.myFoodDao().search("pho").single())

        val saved = db.foodEntryDao().byId(id)!!
        assertNull(saved.myFoodId)
        assertEquals(490.0, saved.kcal, 0.0) // giá trị tuyệt đối, không tham chiếu Món của tôi
    }
}

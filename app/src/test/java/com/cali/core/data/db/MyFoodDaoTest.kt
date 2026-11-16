package com.cali.core.data.db

import com.cali.core.data.db.entity.MyFoodEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MyFoodDaoTest : DbTestBase() {

    private fun food(name: String, useCount: Int = 0, favorite: Boolean = false) = MyFoodEntity(
        name = name, nameNormalized = TextNormalizer.normalize(name), kcal = 400.0,
        isFavorite = favorite, useCount = useCount, createdAt = 0, updatedAt = 0,
    )

    @Test fun `tim khong dau tren name_normalized`() = runTest {
        val dao = db.myFoodDao()
        dao.insert(food("Phở bò tái"))
        dao.insert(food("Bánh mì đặc biệt"))
        dao.insert(food("Bún bò Huế"))

        assertEquals(listOf("Bún bò Huế", "Phở bò tái"), dao.search(TextNormalizer.normalize("BÒ")).map { it.name }.sorted())
        assertEquals(listOf("Bánh mì đặc biệt"), dao.search(TextNormalizer.normalize("dac biet")).map { it.name })
    }

    @Test fun `top theo use_count giam dan`() = runTest {
        val dao = db.myFoodDao()
        dao.insert(food("A", useCount = 1))
        dao.insert(food("B", useCount = 7))
        dao.insert(food("C", useCount = 3))

        assertEquals(listOf("B", "C"), dao.top(2).map { it.name })
    }

    @Test fun `danh dau da dung tang use_count`() = runTest {
        val dao = db.myFoodDao()
        val id = dao.insert(food("Trà sữa", useCount = 2))
        dao.markUsed(id, at = 1234)

        val f = dao.top(1).single()
        assertEquals(3, f.useCount)
        assertEquals(1234L, f.lastUsedAt)
    }
}

package com.cali.core.data.repo

import androidx.room.withTransaction
import com.cali.core.calc.FoodValues
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.TextNormalizer
import com.cali.core.data.db.entity.MyFoodEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Bản đã xoá kèm các món nhật ký từng trỏ tới nó, để Hoàn tác nối lại. */
data class DeletedMyFood(val food: MyFoodEntity, val linkedEntryIds: List<Long>)

/** Món của tôi (04 §2 my_food, F07). */
@Singleton
class MyFoodRepository @Inject constructor(
    private val db: AppDatabase,
    private val clock: Clock,
) {
    private val dao = db.myFoodDao()
    private val foodDao = db.foodEntryDao()

    fun observeAll(): Flow<List<MyFoodEntity>> = dao.observeAll()
    fun observeFavorites(): Flow<List<MyFoodEntity>> = dao.observeFavorites()

    /** Tìm không dấu; ô tìm trống → tất cả (theo Hay dùng). */
    suspend fun search(query: String): List<MyFoodEntity> {
        val q = TextNormalizer.normalize(query)
        return if (q.isEmpty()) observeAll().first() else dao.search(q)
    }

    suspend fun byId(id: Long): MyFoodEntity? = dao.byId(id)

    /** Số lần món này có trong nhật ký (S34 "21 lần đã ghi giữ số cũ"). */
    suspend fun entryCount(id: Long): Int = foodDao.countForMyFood(id)

    suspend fun create(name: String, portionDesc: String?, grams: Double?, values: FoodValues, favorite: Boolean): Result<Long> = runCatching {
        require(name.isNotBlank())
        val now = clock.millis()
        dao.insert(
            MyFoodEntity(
                name = name.trim(), nameNormalized = TextNormalizer.normalize(name), portionDesc = portionDesc?.takeIf { it.isNotBlank() },
                grams = grams, kcal = values.kcal, proteinG = values.proteinG, carbG = values.carbG, fatG = values.fatG,
                isFavorite = favorite, createdAt = now, updatedAt = now,
            ),
        )
    }

    /** Chỉ đổi món đã lưu; nhật ký giữ giá trị tuyệt đối cũ. */
    suspend fun update(food: MyFoodEntity): Result<Unit> = runCatching {
        require(food.name.isNotBlank())
        dao.update(food.copy(name = food.name.trim(), nameNormalized = TextNormalizer.normalize(food.name), updatedAt = clock.millis()))
    }

    suspend fun toggleFavorite(id: Long): Result<Boolean> = runCatching {
        val f = requireNotNull(dao.byId(id))
        dao.update(f.copy(isFavorite = !f.isFavorite, updatedAt = clock.millis()))
        !f.isFavorite
    }

    suspend fun delete(id: Long): Result<DeletedMyFood> = runCatching {
        db.withTransaction {
            val f = requireNotNull(dao.byId(id))
            val linked = foodDao.idsForMyFood(id)
            dao.delete(f) // FK SET NULL: nhật ký giữ nguyên số liệu
            DeletedMyFood(f, linked)
        }
    }

    suspend fun restore(deleted: DeletedMyFood): Result<Unit> = runCatching {
        db.withTransaction {
            dao.insert(deleted.food)
            if (deleted.linkedEntryIds.isNotEmpty()) foodDao.relinkMyFood(deleted.food.id, deleted.linkedEntryIds)
        }
    }
}

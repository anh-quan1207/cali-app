package com.cali.feature.debug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.cali.core.calc.FoodValues
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MealType
import com.cali.core.calc.model.Sex
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.DiaryRepository
import com.cali.core.data.repo.MyFoodRepository
import com.cali.core.data.repo.NewFood
import com.cali.core.data.repo.ProfileInput
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.repo.WaterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Chỉ bản debug: nạp bộ số mockup S10 cho hôm nay để chụp so ảnh (09 §1 mục 5).
 * `adb shell am start -n com.cali/.feature.debug.SeedActivity --es day ok|light|over|empty [--ez myfoods true]`
 * Hồ sơ: Nam, 1996, 170 cm, 75 kg, vận động nhẹ, giảm 0,5 kg/tuần → 1.740 kcal.
 */
@AndroidEntryPoint
class SeedActivity : ComponentActivity() {
    @Inject lateinit var db: AppDatabase
    @Inject lateinit var profile: ProfileRepository
    @Inject lateinit var diary: DiaryRepository
    @Inject lateinit var myFoods: MyFoodRepository
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var water: WaterRepository

    private data class F(val t: String, val meal: MealType, val n: String, val p: String, val k: Double, val m: List<Double>, val src: FoodSource)

    private val banhmi = F("07:20", MealType.BREAKFAST, "Bánh mì ốp la", "1 ổ · 2 trứng", 280.0, listOf(14.0, 30.0, 11.0), FoodSource.AI_PHOTO)
    private val bunbo = F("12:35", MealType.LUNCH, "Bún bò Huế", "1 tô vừa", 490.0, listOf(33.0, 60.0, 12.0), FoodSource.AI_PHOTO)
    private val suachua = F("15:10", MealType.SNACK, "Sữa chua không đường", "1 hộp 100 g", 50.0, listOf(4.0, 5.0, 2.0), FoodSource.MY_FOOD)
    private val comtam = F("19:05", MealType.DINNER, "Cơm tấm sườn bì chả", "1 dĩa", 720.0, listOf(38.0, 82.0, 26.0), FoodSource.AI_PHOTO)
    private val canhchua = F("18:50", MealType.DINNER, "Cơm + canh chua cá", "1 chén cơm · 1 tô canh", 870.0, listOf(45.0, 110.0, 18.0), FoodSource.AI_PHOTO)
    private val trasua = F("22:10", MealType.SNACK, "Trà sữa trân châu", "1 ly size L · 100% đường", 450.0, listOf(3.0, 80.0, 14.0), FoodSource.AI_PHOTO)
    private val chuoi = F("20:30", MealType.SNACK, "Chuối", "1 quả", 100.0, listOf(1.0, 26.0, 0.0), FoodSource.MANUAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val day = intent.getStringExtra("day") ?: "ok"
        val withMyFoods = intent.getBooleanExtra("myfoods", false)
        val waterMode = intent.getStringExtra("water")
        lifecycleScope.launch {
            val today = LocalDate.now()
            if (!settings.current().onboardingDone) {
                profile.completeOnboarding(ProfileInput(Sex.MALE, 1996, 170.0, ActivityLevel.LIGHT, GoalType.LOSE, 68.0, 0.5), 75.0).getOrThrow()
                db.weightEntryDao().put(WeightEntryEntity(date = today.minusDays(5), weightKg = 74.6, createdAt = 1))
                db.weightEntryDao().put(WeightEntryEntity(date = today, weightKg = 74.1, createdAt = 2))
            }
            db.foodEntryDao().between(today, today).forEach { db.foodEntryDao().delete(it) }
            val items = when (day) {
                "light" -> listOf(banhmi, bunbo, suachua, canhchua, chuoi)
                "over" -> listOf(banhmi, bunbo, suachua, comtam, trasua)
                "empty" -> emptyList()
                else -> listOf(banhmi, bunbo, suachua)
            }
            items.forEach { f ->
                val v = FoodValues(f.k, f.m[0], f.m[1], f.m[2])
                diary.add(listOf(NewFood(f.n, f.p, null, v, f.src)), LocalDateTime.of(today, LocalTime.parse(f.t)), f.meal, false).getOrThrow()
            }
            if (withMyFoods && myFoods.observeAllOnce().isEmpty()) {
                listOf(
                    Triple("Cà phê sữa đá", "1 ly · ít đường", FoodValues(140.0, 2.0, 22.0, 4.0)) to true,
                    Triple("Cơm tấm sườn", "1 dĩa", FoodValues(650.0, 32.0, 80.0, 20.0)) to true,
                    Triple("Bún bò Huế", "1 tô vừa", FoodValues(490.0, 33.0, 60.0, 12.0)) to true,
                    Triple("Trà sữa trân châu", "1 ly size M", FoodValues(400.0, 2.0, 70.0, 12.0)) to false,
                    Triple("Phở bò", "1 tô vừa", FoodValues(470.0, 28.0, 55.0, 12.0)) to false,
                    Triple("Xôi gà", "1 gói", FoodValues(380.0, 18.0, 52.0, 11.0)) to false,
                    Triple("Bánh mì thịt", "1 ổ", FoodValues(330.0, 16.0, 38.0, 12.0)) to false,
                    Triple("Cơm trắng", "1 chén", FoodValues(200.0, 4.0, 44.0, 0.0)) to false,
                    Triple("Trà đá", "1 ly · 300 ml", FoodValues(0.0, 0.0, 0.0, 0.0)) to false,
                    Triple("Bánh tráng trộn", "1 bịch", FoodValues(320.0, 8.0, 45.0, 12.0)) to false,
                ).forEachIndexed { i, (t, fav) ->
                    val id = myFoods.create(t.first, t.second, null, t.third, fav).getOrThrow()
                    repeat(20 - i) { db.myFoodDao().markUsed(id, System.currentTimeMillis() - i * 3_600_000L) }
                }
            }
            if (waterMode != null) seedWater(today, waterMode)
            Toast.makeText(this@SeedActivity, "Đã nạp dữ liệu mẫu: $day", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * GĐ5 — nước theo mockup s15: `--es water today` (1.250 ml), `evening` (2.850 ml), `none`.
     * Luôn nạp kèm 3 ngày trước: hôm qua 2.700 ml (s15 f9), rồi 2.400 và 2.600.
     */
    private suspend fun seedWater(today: LocalDate, mode: String) {
        val dao = db.waterEntryDao()
        val days = (0L..3L).map { today.minusDays(it) }
        days.forEach { d -> dao.observeByDate(d).first().forEach { dao.delete(it) } }
        val todayFirst = listOf("07:05" to 250 to WaterSource.APP, "08:40" to 500 to WaterSource.APP, "10:20" to 250 to WaterSource.NOTIFICATION, "12:35" to 250 to WaterSource.APP)
        val evening = todayFirst + listOf("14:05" to 250 to WaterSource.NOTIFICATION, "15:50" to 250 to WaterSource.APP, "17:30" to 500 to WaterSource.APP, "19:40" to 250 to WaterSource.NOTIFICATION, "21:10" to 350 to WaterSource.APP)
        val yesterday = listOf("06:50" to 250, "08:15" to 500, "10:00" to 250, "11:40" to 250, "13:30" to 500, "15:10" to 250, "17:00" to 250, "19:20" to 450)
            .mapIndexed { i, p -> p to if (i == 2 || i == 5) WaterSource.NOTIFICATION else WaterSource.APP }
        val sets = mapOf(
            today to when (mode) { "evening" -> evening; "none" -> emptyList(); else -> todayFirst },
            today.minusDays(1) to yesterday,
            today.minusDays(2) to listOf("09:00" to 1200 to WaterSource.APP, "15:00" to 1200 to WaterSource.APP),
            today.minusDays(3) to listOf("09:00" to 1300 to WaterSource.APP, "15:00" to 1300 to WaterSource.APP),
        )
        sets.forEach { (d, list) ->
            list.forEach { (tm, src) ->
                water.add(d, tm.second, LocalTime.parse(tm.first), src).getOrThrow()
            }
        }
    }
}

private suspend fun MyFoodRepository.observeAllOnce() = search("")

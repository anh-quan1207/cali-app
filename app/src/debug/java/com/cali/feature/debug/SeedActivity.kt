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
 * GĐ6: `--ez history true` nạp 4 tuần trước tuần này (tuần trước = ví dụ 08 §5.4, điểm 71) + 16 lần cân (mockup s31).
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
        if (intent.hasExtra("fakeAi")) {
            val flag = java.io.File(filesDir, com.cali.core.ai.SwitchingAiProvider.DEBUG_FLAG_FILE)
            if (intent.getBooleanExtra("fakeAi", false)) flag.createNewFile() else flag.delete()
            if (!intent.hasExtra("day") && !intent.hasExtra("myfoods")) {
                finish()
                return
            }
        }
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
            if (intent.getBooleanExtra("history", false)) seedHistory(today)
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

    /** Một ngày: món có tên (kcal) + phần còn lại "Cơm nhà" mang toàn bộ macro của ngày. */
    private data class Day(val kcal: Double, val p: Double, val c: Double, val f: Double, val water: Int, val named: List<Pair<String, Double>>)

    private suspend fun seedHistory(today: LocalDate) {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val from = monday.minusWeeks(4)
        db.foodEntryDao().between(from, today.minusDays(1)).forEach { db.foodEntryDao().delete(it) }
        val wdao = db.waterEntryDao()
        (0 until java.time.temporal.ChronoUnit.DAYS.between(from, today).toInt()).forEach { i ->
            wdao.observeByDate(from.plusDays(i.toLong())).first().forEach { wdao.delete(it) }
        }
        val ts = "Trà sữa trân châu" to 400.0
        val ct = "Cơm tấm sườn" to 650.0
        val pho = "Phở bò" to 470.0
        val bm = "Bánh mì thịt" to 330.0
        val bb = "Bún bò Huế" to 490.0
        // Tuần trước = 08 §5.4: 1.710 · 1.760 · 690 · 1.800 · 1.650 · 2.360 · 2.630; nước đạt 5/7 → điểm 71.
        val last = listOf(
            Day(1710.0, 110.0, 220.0, 50.0, 2600, listOf(ts)),
            Day(1760.0, 105.0, 230.0, 55.0, 2600, listOf(ct)),
            Day(690.0, 40.0, 90.0, 25.0, 1100, listOf(pho)),
            Day(1800.0, 115.0, 240.0, 60.0, 2600, listOf(bm)),
            Day(1650.0, 118.0, 210.0, 55.0, 2600, listOf(bb)),
            Day(2360.0, 120.0, 300.0, 65.0, 1100, listOf(ts, bm, pho)),
            Day(2630.0, 120.0, 327.0, 75.0, 2600, listOf(ts, ct)),
        )
        val before = listOf(1700.0, 1900.0, 1650.0, 2000.0, 1720.0, 1800.0, 1600.0)
        val weeks = mapOf(
            monday.minusWeeks(4) to listOf(1750.0, 1690.0, 1820.0, 1600.0, 1980.0, 2100.0, 1700.0),
            monday.minusWeeks(3) to listOf(1690.0, 1780.0, 0.0, 1720.0, 2100.0, 1600.0, 1950.0),
            monday.minusWeeks(2) to before,
        )
        weeks.forEach { (start, list) ->
            list.forEachIndexed { i, k ->
                if (k > 0) addDay(start.plusDays(i.toLong()), Day(k, 100.0, k * 0.13, k * 0.03, 2400, emptyList()))
            }
        }
        last.forEachIndexed { i, d -> addDay(monday.minusWeeks(1).plusDays(i.toLong()), d) }
        if (today != monday) addDay(monday, Day(1710.0, 112.0, 215.0, 52.0, 2600, listOf(ct)))
        // 16 lần cân (mockup s31, lịch thật lùi 1 ngày), tuần trước TB 74,1; tuần này chưa cân.
        val wd = db.weightEntryDao()
        wd.observeAll().first().forEach { wd.delete(it) }
        val lastMonday = monday.minusWeeks(1)
        listOf(
            -35 to 76.0, -33 to 75.9, -31 to 75.8, -28 to 75.9, -25 to 75.5,
            -21 to 75.6, -19 to 75.3, -16 to 75.3, -13 to 75.0, -9 to 75.0,
            -7 to 74.8, -4 to 74.5, -2 to 74.5, 1 to 74.3, 3 to 73.9, 5 to 74.1,
        ).forEachIndexed { i, (off, kg) ->
            wd.put(WeightEntryEntity(date = lastMonday.plusDays(off.toLong()), weightKg = kg, createdAt = i.toLong()))
        }
    }

    private suspend fun addDay(date: LocalDate, d: Day) {
        val namedKcal = d.named.sumOf { it.second }
        d.named.forEachIndexed { i, (n, k) ->
            diary.add(listOf(NewFood(n, null, null, FoodValues(k, 0.0, 0.0, 0.0), FoodSource.MANUAL)), LocalDateTime.of(date, LocalTime.of(8 + i * 4, 0)), if (i == 0) MealType.BREAKFAST else MealType.SNACK, false).getOrThrow()
        }
        diary.add(
            listOf(NewFood("Cơm nhà", null, null, FoodValues(d.kcal - namedKcal, d.p, d.c, d.f), FoodSource.MANUAL)),
            LocalDateTime.of(date, LocalTime.of(19, 0)), MealType.DINNER, false,
        ).getOrThrow()
        water.add(date, d.water, LocalTime.of(12, 0), WaterSource.APP).getOrThrow()
    }
}

private suspend fun MyFoodRepository.observeAllOnce() = search("")

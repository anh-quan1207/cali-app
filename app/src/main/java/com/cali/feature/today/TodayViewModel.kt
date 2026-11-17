package com.cali.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.DayData
import com.cali.core.data.repo.DiaryRepository
import com.cali.core.data.repo.WeightRepository
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.repo.WaterRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.component.hm
import com.cali.core.ui.UndoCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeightTile(val latest: WeightEntryEntity, val previous: WeightEntryEntity?)

/** Ô Nước (S10 mục 6, GĐ5): tổng ngày đang xem / mục tiêu, hai nút nhanh. */
data class WaterTile(val totalMl: Int, val goalMl: Int, val cupSmall: Int, val cupLarge: Int)

/** Thẻ tuần: S33 (tuần của ngày đang xem) và S10 Thứ Hai–Thứ Ba (tuần trước, 08 §1). */
data class WeekTile(
    val weekStart: LocalDate,
    val score: Int,
    val grade: com.cali.core.calc.WeekGrade,
    val partial: Boolean,
    val remainingDays: Int,
    val reviewStale: Boolean,
    val lastWeek: Boolean,
    val deltaKcal: Int = 0,
    val onTargetDays: Int = 0,
    val countedDays: Int = 0,
)

data class TodayUiState(
    val today: LocalDate,
    val day: DayData,
    val weight: WeightTile?,
    val water: WaterTile,
    val week: WeekTile? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val diary: DiaryRepository,
    weights: WeightRepository,
    settings: SettingsRepository,
    private val water: WaterRepository,
    private val undo: UndoCenter,
    private val stats: com.cali.core.data.repo.StatsRepository,
    private val reviews: com.cali.core.ai.WeeklyReviewService,
) : ViewModel() {

    private val selected = MutableStateFlow(diary.today())

    val state: StateFlow<TodayUiState?> = combine(
        selected.flatMapLatest { diary.observeDay(it) },
        weights.observeAll().map { all -> all.firstOrNull()?.let { WeightTile(it, all.getOrNull(1)) } },
        settings.settings,
        selected.flatMapLatest { water.observeDay(it) },
        selected.flatMapLatest { weekTile(it) },
    ) { day, w, s, wd, wk ->
        diary.resolver = com.cali.core.calc.MealTypeResolver(s.mealTimes)
        TodayUiState(diary.today(), day, w, WaterTile(wd.totalMl, wd.goalMl, s.waterCupSmallMl, s.waterCupLargeMl), wk)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun weekTile(date: LocalDate): kotlinx.coroutines.flow.Flow<WeekTile?> {
        val today = diary.today()
        val planner = com.cali.core.calc.WeeklySummaryPlanner
        val (week, last) = when {
            date.isBefore(today) -> planner.weekStart(date) to false
            planner.showLastWeekCard(today) -> planner.weekStart(today).minusWeeks(1) to true
            else -> return kotlinx.coroutines.flow.flowOf(null)
        }
        return combine(stats.observe(week, week.plusDays(6)), reviews.state(week)) { data, review ->
            val s = com.cali.core.calc.WeeklyStatsCalculator.compute(week, data.today, data.daysIn(week, week.plusDays(6)), firstUseDate = data.firstUseDate)
            if (s.elapsedDays == 0 || (last && s.loggedDays == 0)) {
                null
            } else {
                WeekTile(
                    week, s.score.total, s.grade, s.isPartial, s.remainingDays,
                    reviewStale = review is com.cali.core.ai.WeeklyReviewState.Ready && review.stale,
                    lastWeek = last,
                    deltaKcal = Math.round(s.sumDeltaKcal).toInt(),
                    onTargetDays = s.onTargetDays,
                    countedDays = s.elapsedDays,
                )
            }
        }
    }

    /** Nút +250 trên ô Nước: ghi cho ngày đang xem (hôm nay = bây giờ, ngày cũ = 14:30). */
    fun addWater(ml: Int) = viewModelScope.launch { water.add(selected.value, ml, null, WaterSource.APP) }

    /** Nút xoá trên ô Nước: bỏ lần vừa thêm của ngày đang xem, có Hoàn tác (G5-8). */
    fun deleteLastWater() = viewModelScope.launch {
        water.deleteLatest(selected.value).onSuccess { removed ->
            undo.post(UiMessage(R.string.s15_deleted, listOf(com.cali.core.ui.format.VnFormat.int(removed.amountMl), water.timeOf(removed).hm())) { water.restore(removed) })
        }
    }

    fun goTo(date: LocalDate) {
        if (!date.isAfter(diary.today())) selected.value = date
    }

    fun shift(days: Long) = goTo(selected.value.plusDays(days))

    /** Mở lại app sang ngày mới → về hôm nay nếu đang xem "hôm qua" của hôm qua. */
    fun refreshToday() {
        val t = diary.today()
        if (selected.value.isAfter(t)) selected.value = t
    }

    fun delete(e: FoodEntryEntity) = viewModelScope.launch {
        diary.delete(e.id).onSuccess { removed ->
            undo.post(UiMessage(R.string.s10_deleted, listOf(removed.name)) { diary.restore(removed) })
        }
    }

    fun move(e: FoodEntryEntity, meal: MealType, mealName: String) = viewModelScope.launch {
        diary.moveMeal(e.id, meal).onSuccess {
            undo.post(UiMessage(R.string.s10_moved, listOf(mealName)) { diary.put(e) })
        }
    }

    fun saveToMyFoods(e: FoodEntryEntity) = viewModelScope.launch {
        diary.saveToMyFoods(e.id).onSuccess { undo.post(UiMessage(R.string.s10_saved_myfood)) }
    }

    val messages = undo.messages

    fun timeLabel(e: FoodEntryEntity): String =
        diary.toLocal(e.eatenAt).toLocalTime().let { "%02d:%02d".format(it.hour, it.minute) }
}

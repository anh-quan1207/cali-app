package com.cali.feature.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.FoodValues
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.Projection
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.TextNormalizer
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.DayData
import com.cali.core.data.repo.DiaryRepository
import com.cali.core.data.repo.MyFoodRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.MealTime
import com.cali.core.ui.component.TimeSource
import com.cali.core.ui.format.VnFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

enum class LogStage { PICK, MANUAL, CONFIRM }
enum class PickTab { RECENT, FREQUENT, FAVORITE }

/** Một hàng chọn được ở S13 (từ nhật ký hoặc Món của tôi). */
data class PickRow(
    val key: String,
    val name: String,
    val portion: String?,
    val grams: Double?,
    val values: FoodValues,
    val myFoodId: Long?,
    val uses: Int?,
    val match: IntRange?,
)

data class LogUi(
    val stage: LogStage,
    val tab: PickTab,
    val query: String,
    val mealTime: MealTime,
    val today: LocalDate,
    val day: DayData?,
    val items: List<DraftItem>,
    val recent: List<Pair<LocalDate, List<PickRow>>>,
    val frequent: List<PickRow>,
    val favorites: List<PickRow>,
    val searchResults: List<PickRow>?,
    val savedCount: Int,
    val manual: FoodFormState,
    val manualShowErrors: Boolean,
) {
    val totalKcal get() = items.sumOf { it.values.kcal }
    val projection: Projection? get() = day?.let { Projection.of(it.summary.totalKcal, 0.0, totalKcal, it.goal?.targetKcal) }
}

/**
 * Luồng ghi món không AI: S13 Thêm nhanh → (Nhập tay) → S12 Xác nhận → lưu.
 * Tham số: `date` (ISO), `meal` (tên MealType hoặc rỗng) từ nút + trên S10.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LogFoodViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val diary: DiaryRepository,
    private val myFoods: MyFoodRepository,
    settings: SettingsRepository,
    private val undo: UndoCenter,
) : ViewModel() {
    private val argDate: LocalDate = handle.get<String>("date")?.let(LocalDate::parse) ?: diary.today()
    private val argMeal: MealType? = handle.get<String>("meal")?.takeIf { it.isNotBlank() }?.let(MealType::valueOf)

    var resolver = MealTypeResolver()
        private set

    private val stage = MutableStateFlow(LogStage.PICK)
    private val tab = MutableStateFlow(PickTab.RECENT)
    private val query = MutableStateFlow("")
    private val mealTime = MutableStateFlow(initialTime())
    private val items = MutableStateFlow<List<DraftItem>>(emptyList())
    private val recent = MutableStateFlow<List<Pair<LocalDate, List<PickRow>>>>(emptyList())
    private val search = MutableStateFlow<List<PickRow>?>(null)
    private val manual = MutableStateFlow(FoodFormState() to false)
    private var nextKey = 1L

    val done = MutableStateFlow(false)

    private fun initialTime(): MealTime {
        val now = diary.now()
        return when {
            argMeal != null -> MealTime(argDate, resolver.defaultTimeFor(argMeal), argMeal, true, TimeSource.MEAL_DEFAULT)
            argDate == now.toLocalDate() -> MealTime(argDate, now.toLocalTime().withSecond(0).withNano(0), resolver.fromTime(now.toLocalTime()), false, TimeSource.NOW)
            else -> {
                val t = now.toLocalTime().withSecond(0).withNano(0)
                MealTime(argDate, t, resolver.fromTime(t), false, TimeSource.NOW)
            }
        }
    }

    init {
        viewModelScope.launch {
            settings.settings.map { it.mealTimes }.collect { m ->
                resolver = MealTypeResolver(m)
                diary.resolver = resolver
                // Giờ chưa bị người dùng đổi → tính lại theo khung giờ của người dùng.
                if (mealTime.value.source != TimeSource.USER) mealTime.value = initialTime()
            }
        }
        viewModelScope.launch { refreshRecent() }
        viewModelScope.launch {
            query.collect { q ->
                search.value = if (TextNormalizer.normalize(q).isEmpty()) null else myFoods.search(q).map { it.toRow(q) }
            }
        }
    }

    private suspend fun refreshRecent() {
        recent.value = diary.recent().map { (d, list) -> d to list.map { it.toRow() } }
    }

    private val lists = combine(myFoods.observeAll(), myFoods.observeFavorites()) { all, fav ->
        Triple(all.sortedWith(compareByDescending<MyFoodEntity> { it.useCount }.thenByDescending { it.lastUsedAt ?: 0 }.thenBy { it.name }).map { it.toRow(null) }, fav.map { it.toRow(null) }, all.size)
    }

    private val dayFlow = mealTime.map { it.date }.flatMapLatest { diary.observeDay(it) }

    private val pickState = combine(stage, tab, query, search, recent) { st, tb, q, sr, rc -> PickPart(st, tb, q, sr, rc) }

    val ui: StateFlow<LogUi?> = combine(pickState, mealTime, items, lists, combine(dayFlow, manual) { d, m -> d to m }) { p, mt, its, (freq, fav, count), (day, man) ->
        LogUi(p.stage, p.tab, p.query, mt, diary.today(), day, its, p.recent, freq, fav, p.search, count, man.first, man.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private data class PickPart(val stage: LogStage, val tab: PickTab, val query: String, val search: List<PickRow>?, val recent: List<Pair<LocalDate, List<PickRow>>>)

    // ---- S13 ----
    fun setTab(t: PickTab) { tab.value = t }
    fun setQuery(q: String) { query.value = q.take(60) }
    fun setMealTime(m: MealTime) { mealTime.value = m }

    fun pick(row: PickRow) {
        items.update {
            it + DraftItem(
                key = nextKey++, name = row.name, portion = row.portion, baseGrams = row.grams, base = row.values,
                source = if (row.myFoodId != null) FoodSource.MY_FOOD else FoodSource.MANUAL, myFoodId = row.myFoodId,
            )
        }
        query.value = ""
        stage.value = LogStage.CONFIRM
    }

    fun openManual() {
        manual.value = FoodFormState(name = query.value.trim()) to false
        stage.value = LogStage.MANUAL
    }

    fun updateManual(s: FoodFormState) { manual.update { s to it.second } }

    fun submitManual() {
        val f = manual.value.first
        if (!f.valid) {
            manual.value = f.copy(checked = true) to true
            return
        }
        items.update {
            it + DraftItem(
                key = nextKey++, name = f.name.trim(), portion = f.portion.trim().ifBlank { null }, baseGrams = f.gramsValue(),
                base = f.values(), source = FoodSource.MANUAL,
            )
        }
        query.value = ""
        stage.value = LogStage.CONFIRM
    }

    /** Nút quay lại / đóng. Trả false khi luồng kết thúc (đóng màn). */
    fun back(): Boolean = when (stage.value) {
        LogStage.MANUAL -> { stage.value = LogStage.PICK; true }
        LogStage.PICK -> if (items.value.isNotEmpty()) { stage.value = LogStage.CONFIRM; true } else false
        LogStage.CONFIRM -> false
    }

    // ---- S12 ----
    fun addMore(fromMyFoods: Boolean) {
        if (fromMyFoods) tab.value = PickTab.FREQUENT
        stage.value = LogStage.PICK
    }

    fun setFactor(key: Long, f: Double) = items.update { l -> l.map { if (it.key == key) it.copy(factor = f) else it } }
    fun toggleBookmark(key: Long) = items.update { l -> l.map { if (it.key == key) it.copy(bookmark = !it.bookmark) else it } }

    fun remove(key: Long): DraftItem? {
        val idx = items.value.indexOfFirst { it.key == key }
        if (idx < 0) return null
        val removed = items.value[idx]
        items.update { it.filterNot { i -> i.key == key } }
        lastRemoved = idx to removed
        return removed
    }

    private var lastRemoved: Pair<Int, DraftItem>? = null
    fun undoRemove() {
        val (idx, item) = lastRemoved ?: return
        items.update { it.toMutableList().apply { add(idx.coerceAtMost(size), item) } }
        lastRemoved = null
    }

    fun save() = viewModelScope.launch {
        val list = items.value
        if (list.isEmpty()) return@launch
        val mt = mealTime.value
        diary.add(list.map { it.toNewFood() }, LocalDateTime.of(mt.date, mt.time), mt.meal, mt.mealManual).onSuccess {
            undo.post(UiMessage(R.string.s10_logged, listOf(list.size, VnFormat.int(list.sumOf { it.values.kcal }.roundToInt()))))
            done.value = true
        }
    }

    // ---- chuyển đổi ----
    private fun FoodEntryEntity.toRow() = PickRow(
        key = "e$id", name = name, portion = portionDesc, grams = grams, values = FoodValues(kcal, proteinG, carbG, fatG),
        myFoodId = myFoodId, uses = null, match = null,
    )

    private fun MyFoodEntity.toRow(q: String?) = PickRow(
        key = "m$id", name = name, portion = portionDesc, grams = grams, values = FoodValues(kcal, proteinG, carbG, fatG),
        myFoodId = id, uses = useCount, match = q?.let { TextNormalizer.matchRange(name, it) },
    )
}

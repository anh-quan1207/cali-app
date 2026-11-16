package com.cali.feature.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.ai.AiDraft
import com.cali.core.ai.AiDraftHolder
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiFoodLogJson
import com.cali.core.ai.AiLogService
import com.cali.core.ai.AiOutcome
import com.cali.core.ai.AiReview
import com.cali.core.ai.ThumbnailStore
import com.cali.core.ai.ValidatedFoodItem
import com.cali.core.ai.ValidatedFoodLog
import com.cali.core.data.repo.ChatRepository
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
    /** null = luồng không AI (S13 → S12). */
    val ai: AiPanel? = null,
) {
    val totalKcal get() = items.sumOf { it.values.kcal }
    val projection: Projection? get() = day?.let { Projection.of(it.summary.totalKcal, 0.0, totalKcal, it.goal?.targetKcal) }
}

/** Phần riêng của S12 khi kết quả đến từ AI (ảnh hoặc chat). */
data class AiPanel(
    val source: FoodSource,
    val thumbnail: ByteArray?,
    val comment: String?,
    val question: String?,
    val noFood: Boolean,
    val answering: Boolean = false,
    val answerError: AiErrorType? = null,
    val answerNote: String? = null,
)

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
    private val settings: SettingsRepository,
    private val undo: UndoCenter,
    private val drafts: AiDraftHolder,
    private val chat: ChatRepository,
    private val aiService: AiLogService,
    private val thumbs: ThumbnailStore,
) : ViewModel() {
    private val argAi: Boolean = handle.get<Boolean>("ai") == true
    private val argChat: Long = handle.get<Long>("chatId") ?: -1L
    private val argStart: String = handle.get<String>("start").orEmpty()
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
    private val aiPanel = MutableStateFlow<AiPanel?>(null)
    private var nextKey = 1L
    private var aiDraft: AiDraft? = null
    private var aiLog: ValidatedFoodLog? = null

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
                aiService.resolver = resolver
                if (mealTime.value.source == TimeSource.NOW || mealTime.value.source == TimeSource.MEAL_DEFAULT) mealTime.value = initialTime()
            }
        }
        when (argStart) {
            "manual" -> openManual()
            "myfoods" -> tab.value = PickTab.FREQUENT
        }
        when {
            argAi -> drafts.take()?.let(::applyDraft) ?: run { done.value = true }
            argChat >= 0 -> viewModelScope.launch {
                val m = chat.byId(argChat)
                val d = AiFoodLogJson.decode(m?.foodLogJson)
                if (m == null || d == null) {
                    done.value = true
                } else {
                    applyDraft(
                        AiDraft(
                            d.log, d.timing, d.mealManual, FoodSource.AI_CHAT, m.aiCallId, thumbnailJpeg = null,
                            hadImage = false, defaultAt = d.timing.eatenAt, chatMessageId = m.id,
                        ),
                    )
                }
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

    // ---- AI ----
    private fun applyDraft(d: AiDraft) {
        aiDraft = d
        aiLog = d.log
        items.value = d.log.items.let(AiReview::reviewOrder).map { it.toDraft(d.source, d.callId) }
        val t = d.timing.eatenAt
        val saidByUser = d.log.eatenTime != null || d.log.eatenDate != null || d.log.mealType != null
        val src = when {
            saidByUser -> TimeSource.AI
            d.source == FoodSource.AI_CHAT -> TimeSource.SENT
            else -> TimeSource.CAPTURE
        }
        mealTime.value = MealTime(t.toLocalDate(), t.toLocalTime().withSecond(0).withNano(0), d.timing.mealType, d.mealManual, src)
        aiPanel.value = AiPanel(d.source, d.thumbnailJpeg, d.log.comment, d.log.clarifyingQuestion, noFood = d.log.isEmpty)
        stage.value = LogStage.CONFIRM
    }

    private fun ValidatedFoodItem.toDraft(source: FoodSource, callId: Long?) = DraftItem(
        key = nextKey++, name = name, portion = portionDesc.ifBlank { null }, baseGrams = grams.takeIf { it > 0 },
        base = FoodValues(kcal, proteinG, carbG, fatG), source = source,
        confidence = confidence, assumptions = assumptions, flags = flags, aiCallId = callId,
    )

    /** Trả lời câu hỏi làm rõ (03 §3.4): món AI cũ được thay bằng kết quả mới, món người dùng tự thêm giữ nguyên. */
    fun answer(text: String) = viewModelScope.launch {
        val d = aiDraft ?: return@launch
        val panel = aiPanel.value ?: return@launch
        val q = panel.question ?: return@launch
        if (text.isBlank()) return@launch
        aiPanel.value = panel.copy(answering = true, answerError = null)
        when (val r = aiService.clarify(aiLog ?: d.log, q, text.trim(), d.hadImage, d.defaultAt)) {
            is AiOutcome.FoodLog -> {
                aiLog = r.log
                items.update { old -> old.filter { it.confidence == null } + AiReview.reviewOrder(r.log.items).map { it.toDraft(d.source, r.callId) } }
                aiPanel.value = panel.copy(
                    comment = r.log.comment ?: panel.comment, question = r.log.clarifyingQuestion,
                    noFood = items.value.isEmpty(), answering = false,
                )
            }
            is AiOutcome.Text -> aiPanel.value = panel.copy(question = null, answerNote = r.text, answering = false)
            is AiOutcome.Failed -> aiPanel.value = panel.copy(answering = false, answerError = r.type)
        }
    }

    fun skipQuestion() = aiPanel.update { it?.copy(question = null, answerError = null) }

    private val pickState = combine(stage, tab, query, search, recent) { st, tb, q, sr, rc -> PickPart(st, tb, q, sr, rc) }

    val ui: StateFlow<LogUi?> = combine(pickState, mealTime, items, lists, combine(dayFlow, manual, aiPanel) { d, m, a -> Triple(d, m, a) }) { p, mt, its, (freq, fav, count), (day, man, ai) ->
        LogUi(p.stage, p.tab, p.query, mt, diary.today(), day, its, p.recent, freq, fav, p.search, count, man.first, man.second, ai)
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
        val at = LocalDateTime.of(mt.date, mt.time)
        // Ảnh gốc không bao giờ lưu; thumbnail 256 px chỉ khi bật ở S45 (03 §7).
        val thumbPath = aiPanel.value?.thumbnail?.takeIf { settings.current().saveThumbnails }?.let { thumbs.save(it).getOrNull() }
        val foods = list.map { i -> i.toNewFood().let { if (i.confidence != null) it.copy(thumbnailPath = thumbPath) else it } }
        diary.add(foods, at, mt.meal, mt.mealManual).onSuccess {
            aiDraft?.chatMessageId?.let { chat.markSaved(it, at, mt.meal, mt.mealManual) }
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

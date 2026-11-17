package com.cali.feature.stats

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiException
import com.cali.core.ai.WeeklyReview
import com.cali.core.ai.WeeklyReviewService
import com.cali.core.ai.WeeklyReviewState
import com.cali.core.calc.WeeklySummaryPlanner
import com.cali.core.calc.WeightTrendCalculator
import com.cali.core.data.prefs.SecureKeyStore
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.StatsRepository
import com.cali.core.ui.component.ScoreLanes
import com.cali.core.ui.component.StatCard
import com.cali.core.ui.component.WeightChartData
import com.cali.core.ui.component.WeightLineChart
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import com.cali.feature.weight.WeightSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/** Trạng thái tạo nhận xét đang chạy (ngoài dữ liệu đã lưu). */
sealed interface AiRun {
    data object Idle : AiRun
    data object Writing : AiRun
    data class Failed(val type: AiErrorType) : AiRun
}

data class WeeklyUiState(
    val period: PeriodView,
    val review: WeeklyReviewState,
    val run: AiRun,
    val hasKey: Boolean,
    val canPrev: Boolean,
    val canNext: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeeklyViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val stats: StatsRepository,
    private val reviews: WeeklyReviewService,
    private val settings: SettingsRepository,
    keys: SecureKeyStore,
    @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
) : ViewModel() {
    /** Bản debug có cờ AI giả thì coi như có key (chụp ảnh so mockup không cần key thật). */
    private val fakeAi = com.cali.BuildConfig.DEBUG &&
        java.io.File(context.filesDir, com.cali.core.ai.SwitchingAiProvider.DEBUG_FLAG_FILE).exists()
    private val week = MutableStateFlow(
        handle.get<String>("week")?.takeIf { it.isNotEmpty() }?.let { WeeklySummaryPlanner.weekStart(LocalDate.parse(it)) }
            ?: WeeklySummaryPlanner.defaultWeek(stats.today()),
    )
    private val run = MutableStateFlow<AiRun>(AiRun.Idle)
    private val autoTried = mutableSetOf<LocalDate>()

    val state: StateFlow<WeeklyUiState?> = week.flatMapLatest { w ->
        val (from, to) = Periods.weekRange(w)
        combine(stats.observe(from, to), reviews.state(w), keys.hasKey, run) { data, review, realKey, r ->
            val key = realKey || fakeAi
            val v = Periods.week(data, w)
            val first = data.firstUseDate?.let(WeeklySummaryPlanner::weekStart)
            WeeklyUiState(v, review, r, key, canPrev = first == null || w.isAfter(first), canNext = w.isBefore(WeeklySummaryPlanner.weekStart(data.today)))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // 08 §5.1: tự tạo khi mở tổng kết của tuần đã kết thúc (nếu bật), mỗi tuần thử một lần mỗi lần mở.
        viewModelScope.launch {
            state.collect { s ->
                if (s == null || !s.hasKey || s.run != AiRun.Idle) return@collect
                val w = s.period.start
                val finished = !s.period.stats.isPartial && s.period.stats.loggedDays > 0
                val needs = s.review is WeeklyReviewState.None ||
                    (s.review is WeeklyReviewState.Ready && s.review.partial)
                if (finished && needs && w !in autoTried && settings.current().weeklyAiAuto) {
                    autoTried += w
                    generate()
                }
            }
        }
    }

    fun shift(weeks: Long) {
        week.value = week.value.plusWeeks(weeks)
        run.value = AiRun.Idle
    }

    fun generate() {
        val w = week.value
        run.value = AiRun.Writing
        viewModelScope.launch {
            reviews.generate(w)
                .onSuccess { if (week.value == w) run.value = AiRun.Idle }
                .onFailure { e -> if (week.value == w) run.value = AiRun.Failed((e as? AiException)?.type ?: AiErrorType.UNKNOWN) }
        }
    }
}

@Composable
fun WeeklyRoute(onBack: () -> Unit, onOpenDay: (LocalDate) -> Unit, onOpenAiSettings: () -> Unit, vm: WeeklyViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val st = s ?: return
    WeeklyScreen(st, vm, onBack, onOpenDay, onOpenAiSettings)
}

@Composable
private fun WeeklyScreen(s: WeeklyUiState, vm: WeeklyViewModel, onBack: () -> Unit, onOpenDay: (LocalDate) -> Unit, onOpenAiSettings: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0 } }
    val bar by animateColorAsState(if (scrolled) cs.surfaceContainer else cs.surface, label = "bar")
    var weigh by remember { mutableStateOf(false) }
    val v = s.period
    Column(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.background(bar)) {
            BackTitleBar(stringResource(R.string.s35_title), onBack, bar)
            PeriodNav(weekTitle(v.start, v.today), periodSub(v), v.stats.isPartial, s.canNext, onPrev = { vm.shift(-1) }, onNext = { vm.shift(1) })
            Spacer(Modifier.height(Spacing.sm))
        }
        LazyColumn(
            state = list, modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item(key = "score") { ScoreCard(v) }
            item(key = "cal") { CalorieWeekCard(v, onOpenDay) }
            item(key = "water") { WaterWeekCard(v) }
            item(key = "macro") {
                val prev = v.comparison?.takeIf { it.enough }?.previous?.avgProteinG
                MacroCard(v, stringResource(R.string.s35_macro)) {
                    val now = v.stats.avgProteinG
                    if (prev != null && now != null) {
                        val d = (now - prev).roundToInt()
                        TrendTag(stringResource(R.string.macro_protein) + " " + VnFormat.signedInt(d) + " g", up = if (d == 0) null else d > 0)
                    }
                }
            }
            item(key = "weight") { WeightWeekCard(v, onLog = { weigh = true }) }
            if (v.stats.topFoods.isNotEmpty()) item(key = "foods") { TopFoodsCard(v) }
            if (s.hasKey) item(key = "ai") { AiCard(s, vm, onOpenAiSettings) }
            item(key = "foot") {
                Text(
                    stringResource(R.string.gd6_footer), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }
        }
    }
    if (weigh) WeightSheet(onDismiss = { weigh = false })
}

@Composable
private fun ScoreCard(v: PeriodView) {
    val cs = MaterialTheme.colorScheme
    val sc = v.stats.score
    val grade = gradeText(v.stats.grade)
    val parts = listOf(sc.calorie to 40, sc.protein to 20, sc.water to 20, sc.logging to 20)
    val cd = stringResource(
        R.string.s35_score_cd, sc.total, grade,
        sc.calorie.roundToInt(), sc.protein.roundToInt(), sc.water.roundToInt(), sc.logging.roundToInt(),
    )
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.primaryContainer)
            .padding(Spacing.lg).semantics(mergeDescendants = true) { contentDescription = cd },
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(sc.total.toString(), style = MaterialTheme.typography.displayLarge, color = cs.onPrimaryContainer)
            Text(
                stringResource(R.string.s35_points), style = MaterialTheme.typography.titleMedium, color = cs.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp).weight(1f),
            )
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(grade, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = cs.onPrimaryContainer)
                val prev = v.comparison?.takeIf { it.enough }?.previous
                if (prev != null) {
                    val d = sc.total - prev.score.total
                    val txt = (if (d > 0) "↑ " else if (d < 0) "↓ " else "") + abs(d)
                    Text(
                        stringResource(if (v.stats.isPartial) R.string.s35_vs_same else R.string.s35_vs_last, txt),
                        style = MaterialTheme.typography.labelMedium, color = cs.onPrimaryContainer,
                    )
                }
            }
        }
        ScoreLanes(parts, Modifier.padding(top = Spacing.md))
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val names = listOf(R.string.s35_lane_calorie, R.string.s35_lane_protein, R.string.s35_lane_water, R.string.s35_lane_logging)
            parts.forEachIndexed { i, (got, max) ->
                Column(Modifier.weight(max.toFloat())) {
                    Text(stringResource(names[i]), style = MaterialTheme.typography.labelMedium, color = cs.onPrimaryContainer.copy(alpha = 0.8f))
                    Text("${got.roundToInt()}/$max", style = MaterialTheme.typography.titleSmall, color = cs.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun WeightWeekCard(v: PeriodView, onLog: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s35_weight), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            if (v.weights.isNotEmpty()) Text(stringResource(R.string.s35_weighs, v.weights.size), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        val avg = v.weightAvg
        if (avg == null) {
            Row(Modifier.padding(top = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MonitorWeight, null, tint = cs.onSurfaceVariant)
                }
                Spacer(Modifier.width(Spacing.md))
                Text(stringResource(R.string.s35_weight_none), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            OutlinedButton(onClick = onLog, modifier = Modifier.padding(top = Spacing.md).fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s35_weight_log))
            }
            return@StatCard
        }
        Row(Modifier.padding(top = Spacing.sm), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(VnFormat.decimal1(avg), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
                    Text(" " + stringResource(R.string.s35_weight_avg), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
                v.prevWeightAvg?.let { p ->
                    val ch = Math.round((avg - p) * 10) / 10.0
                    Text(stringResource(R.string.s35_weight_change, VnFormat.signedKg(ch)), style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
                    if (v.stats.loggedDays > 0) {
                        Text(
                            stringResource(R.string.s35_weight_expected, fmt2(v.stats.expectedWeightChangeKg), VnFormat.signedKg(ch)),
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        )
                    }
                }
            }
            MiniWeightChart(v, Modifier.width(140.dp))
        }
        val profile = v.data.profile
        val progress = WeightTrendCalculator.progress(v.data.weights.filter { !it.date.isAfter(v.end) }, profile?.targetWeightKg, profile?.weeklyRateKg ?: 0.0, v.today)
        if (progress?.eta != null && profile?.targetWeightKg != null) {
            NoteRow(
                Icons.Rounded.Flag,
                stringResource(R.string.s35_weight_left, VnFormat.kg(progress.remainingKg), VnFormat.kg(profile.targetWeightKg), VnFormat.date(progress.eta)),
            )
        }
    }
}

@Composable
private fun MiniWeightChart(v: PeriodView, modifier: Modifier) {
    val weeks = WeightTrendCalculator.weeklyAverages(v.data.weights.filter { !it.date.isAfter(v.end) }, minOf(v.end, v.today)).takeLast(4)
    if (weeks.size < 2) return
    val data = WeightChartData(
        points = emptyList(),
        weekly = weeks.map { it.weekStart.toEpochDay() + 3 to it.avgKg },
        fromDay = weeks.first().weekStart.toEpochDay() + 3,
        toDay = weeks.last().weekStart.toEpochDay() + 3,
        goalKg = null,
        ticks = listOf(weeks.first(), weeks.last()).map { it.weekStart.toEpochDay() + 3 to VnFormat.dayMonth(it.weekStart) },
    )
    WeightLineChart(data, modifier, height = 84.dp)
}

@Composable
private fun TopFoodsCard(v: PeriodView) {
    val cs = MaterialTheme.colorScheme
    val foods = v.stats.topFoods
    val max = foods.maxOf { it.kcal }.coerceAtLeast(1.0)
    StatCard {
        Text(stringResource(R.string.s35_foods), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.semantics { heading() })
        foods.forEachIndexed { i, f ->
            if (i > 0) HorizontalDivider(color = cs.outlineVariant)
            Column(Modifier.padding(vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant, modifier = Modifier.width(24.dp))
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(f.name, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface, modifier = Modifier.weight(1f, fill = false), maxLines = 1)
                        Text(" ×${f.count}", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                    Text(VnFormat.int(f.kcal.roundToInt()), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Text(" kcal", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
                Box(Modifier.padding(start = 24.dp, top = 6.dp).fillMaxWidth((f.kcal / max).toFloat()).height(4.dp).background(cs.primary))
            }
        }
        v.stats.topMeal?.let { meal ->
            val days = v.stats.elapsedDays
            val mealKcal = v.data.foods.filter { it.mealType == meal && it.date in v.start..v.end }.groupBy { it.date }.values.map { l -> l.sumOf { it.kcal } }
            val avg = if (mealKcal.isNotEmpty()) mealKcal.average() else 0.0
            if (days > 0) NoteRow(Icons.Rounded.Restaurant, stringResource(R.string.s35_top_meal, stringResource(meal.label()), VnFormat.int(avg.roundToInt())))
        }
    }
}

private val MADE = DateTimeFormatter.ofPattern("dd/MM · HH:mm")

@Composable
private fun AiCard(s: WeeklyUiState, vm: WeeklyViewModel, onOpenAiSettings: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val v = s.period
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = cs.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.s35_ai), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            val r = s.review
            if (r is WeeklyReviewState.Ready && s.run == AiRun.Idle) {
                val made = Instant.ofEpochMilli(r.createdAt).atZone(ZoneId.systemDefault()).format(MADE)
                Text(stringResource(R.string.s35_ai_made, made), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
        when (val run = s.run) {
            AiRun.Writing -> Row(Modifier.padding(top = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(Spacing.md))
                Column {
                    Text(stringResource(R.string.s35_ai_writing), style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
                    Text(stringResource(R.string.s35_ai_writing_sub), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
            is AiRun.Failed -> {
                Text(stringResource(R.string.s35_ai_error), style = MaterialTheme.typography.bodyMedium, color = cs.onSurface, modifier = Modifier.padding(top = Spacing.sm))
                Text(stringResource(com.cali.feature.food.aiErrorText(run.type)), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                FilledTonalButton(
                    onClick = { if (run.type == AiErrorType.INVALID_KEY || run.type == AiErrorType.NO_KEY) onOpenAiSettings() else vm.generate() },
                    modifier = Modifier.padding(top = Spacing.md).heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.s35_ai_retry)) }
            }
            AiRun.Idle -> when (val r = s.review) {
                is WeeklyReviewState.Ready -> {
                    if (r.stale || (r.partial && !v.stats.isPartial)) {
                        Row(
                            Modifier.padding(top = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.medium).background(cs.primaryContainer.copy(alpha = 0.5f))
                                .padding(start = Spacing.md, end = Spacing.xs, top = Spacing.xs, bottom = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Sync, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            Text(stringResource(R.string.s35_ai_stale), style = MaterialTheme.typography.bodySmall, color = cs.onSurface, modifier = Modifier.weight(1f))
                            androidx.compose.material3.TextButton(onClick = vm::generate, modifier = Modifier.heightIn(min = 48.dp)) {
                                Text(stringResource(R.string.s33_regen_action))
                            }
                        }
                    }
                    ReviewBody(r.review)
                }
                WeeklyReviewState.None -> {
                    if (v.stats.isPartial) {
                        Text(
                            stringResource(R.string.s35_ai_partial_note, v.stats.elapsedDays),
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                    if (v.stats.loggedDays > 0) {
                        FilledTonalButton(onClick = vm::generate, modifier = Modifier.padding(top = Spacing.md).heightIn(min = 48.dp)) {
                            Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            Text(stringResource(if (v.stats.isPartial) R.string.s35_ai_make_partial else R.string.s35_ai_make))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewBody(r: WeeklyReview) {
    val cs = MaterialTheme.colorScheme
    Text(r.headline, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.padding(top = Spacing.md))
    Section(Icons.Rounded.CheckCircle, stringResource(R.string.s35_ai_good), r.highlights)
    Section(Icons.Rounded.Flag, stringResource(R.string.s35_ai_issues), r.issues)
    Section(Icons.AutoMirrored.Rounded.TrendingDown, stringResource(R.string.s35_ai_patterns), r.patterns)
    if (r.suggestions.isNotEmpty()) {
        Row(Modifier.padding(top = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lightbulb, null, tint = cs.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.s35_ai_suggest), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
        }
        r.suggestions.forEachIndexed { i, sug ->
            Row(
                Modifier.padding(top = Spacing.sm).fillMaxWidth().clip(MaterialTheme.shapes.medium).background(cs.surfaceContainer).padding(Spacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
                    Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = cs.onPrimaryContainer)
                }
                Spacer(Modifier.width(Spacing.md))
                Text(sug, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
            }
        }
    }
    Text(stringResource(R.string.s35_ai_disclaimer), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.md))
}

@Composable
private fun Section(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, items: List<String>) {
    if (items.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    Row(Modifier.padding(top = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
    }
    items.forEach { t ->
        Row(Modifier.padding(start = 26.dp, top = Spacing.xs)) {
            Text("•", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            Spacer(Modifier.width(Spacing.sm))
            Text(t, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
        }
    }
}

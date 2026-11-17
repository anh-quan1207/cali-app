package com.cali.feature.weight

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.NutritionCalculator
import com.cali.core.calc.WeightHeadline
import com.cali.core.calc.WeightPoint
import com.cali.core.calc.WeightProgress
import com.cali.core.calc.WeightRange
import com.cali.core.calc.WeightTrendCalculator
import com.cali.core.calc.model.GoalType
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.repo.WeightRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.BmiScale
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.component.LegendLine
import com.cali.core.ui.component.StatCard
import com.cali.core.ui.component.SwipeToDelete
import com.cali.core.ui.component.WeightChartData
import com.cali.core.ui.component.WeightLineChart
import com.cali.core.ui.component.label
import com.cali.core.ui.component.weightGoalInFrame
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import com.cali.feature.stats.BackTitleBar
import com.cali.feature.stats.NoteRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

enum class WeightRangeTab(val days: Int?) { D7(7), D30(30), D90(90), ALL(null) }

data class WeekGroup(val weekStart: LocalDate, val entries: List<WeightEntryEntity>, val avg: Double?, val change: Double?)

data class WeightUiState(
    val today: LocalDate,
    val entries: List<WeightEntryEntity>,
    val headline: WeightHeadline?,
    val progress: WeightProgress?,
    val range: WeightRange?,
    val tab: WeightRangeTab,
    val profile: UserProfileEntity?,
    val groups: List<WeekGroup>,
)

@HiltViewModel
class WeightScreenViewModel @Inject constructor(
    private val weights: WeightRepository,
    profiles: ProfileRepository,
    private val undo: UndoCenter,
    private val clock: Clock,
) : ViewModel() {
    private val tab = MutableStateFlow(WeightRangeTab.D30)

    val state: StateFlow<WeightUiState?> = combine(weights.observeAll(), profiles.profile, tab) { all, p, t ->
        val today = LocalDate.now(clock)
        val pts = all.map { WeightPoint(it.date, it.weightKg) }
        val avgs = WeightTrendCalculator.weeklyAverages(pts, today).associateBy { it.weekStart }
        val thisWeek = WeightTrendCalculator.weekStart(today)
        val byWeek = all.groupBy { WeightTrendCalculator.weekStart(it.date) }
        val starts = (byWeek.keys + thisWeek).sortedDescending()
        val groups = starts.map { w ->
            val avg = avgs[w]?.avgKg
            val prev = avgs.keys.filter { it < w }.maxOrNull()?.let { avgs[it]?.avgKg }
            WeekGroup(w, byWeek[w].orEmpty().sortedByDescending { it.date }, avg, if (avg != null && prev != null) Math.round((avg - prev) * 10) / 10.0 else null)
        }
        WeightUiState(
            today = today,
            entries = all,
            headline = WeightTrendCalculator.headline(pts, today),
            progress = WeightTrendCalculator.progress(pts, p?.targetWeightKg, p?.weeklyRateKg ?: 0.0, today),
            range = WeightTrendCalculator.range(pts, today, t.days),
            tab = t,
            profile = p,
            groups = groups,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages = undo.messages

    fun setTab(t: WeightRangeTab) {
        tab.value = t
    }

    fun delete(e: WeightEntryEntity) = viewModelScope.launch {
        weights.delete(e).onSuccess {
            undo.post(UiMessage(R.string.s31_deleted, listOf(VnFormat.dayMonth(e.date))) { weights.restore(e) })
        }
    }
}

@Composable
fun WeightRoute(onBack: () -> Unit, vm: WeightScreenViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    val st = s ?: return
    WeightScreen(st, vm, snack, onBack)
}

@Composable
private fun WeightScreen(s: WeightUiState, vm: WeightScreenViewModel, snack: SnackbarHostState, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0 } }
    val bar by animateColorAsState(if (scrolled) cs.surfaceContainer else cs.surface, label = "bar")
    var weigh by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(cs.surface).navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            BackTitleBar(stringResource(R.string.s31_title), onBack, bar)
            LazyColumn(
                state = list, modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (s.entries.isEmpty()) {
                    item(key = "empty") {
                        StatCard { Text(stringResource(R.string.s31_empty), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant) }
                    }
                } else {
                    item(key = "head") { HeadCard(s) }
                    item(key = "trend") { TrendCard(s, vm) }
                    item(key = "bmi") { BmiCard(s) }
                    item(key = "records-head") {
                        Row(Modifier.fillMaxWidth().padding(start = Spacing.sm, end = Spacing.sm, top = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.s31_records), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
                            Text(stringResource(R.string.s31_records_count, s.entries.size), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                        }
                    }
                    s.groups.forEach { g ->
                        item(key = "w-${g.weekStart}") { WeekHeader(g, s.today) }
                        items(g.entries, key = { "e-${it.id}" }) { e ->
                            SwipeToDelete(onDelete = { vm.delete(e) }) {
                                Row(
                                    Modifier.fillMaxWidth().background(cs.surfaceContainerLow).heightIn(min = 52.dp).padding(horizontal = Spacing.lg),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(VnFormat.weekdayDayMonth(e.date), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface, modifier = Modifier.weight(1f))
                                    Text(VnFormat.kg(e.weightKg), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                                    Text(" kg", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { weigh = true },
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text(stringResource(R.string.s31_log)) },
            containerColor = cs.primaryContainer, contentColor = cs.onPrimaryContainer,
            modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg),
        )
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
    }
    if (weigh) WeightSheet(onDismiss = { weigh = false })
}

@Composable
private fun HeadCard(s: WeightUiState) {
    val cs = MaterialTheme.colorScheme
    val h = s.headline
    StatCard {
        if (h != null) {
            Text(
                stringResource(R.string.s31_avg_week, VnFormat.dayMonth(h.week.weekStart), VnFormat.dayMonth(h.week.weekStart.plusDays(6)), h.week.count),
                style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(VnFormat.decimal1(h.week.avgKg), style = MaterialTheme.typography.displayLarge, color = cs.onSurface)
                Text(" kg", style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp).weight(1f))
                h.changeKg?.let { c ->
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(VnFormat.signedKg(c) + " kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
                        Text(stringResource(R.string.s31_vs_last), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                }
            }
        }
        val p = s.progress
        val target = s.profile?.targetWeightKg
        if (p != null && target != null && p.startKg != target) {
            val frac = ((p.startKg - p.latestKg) / (p.startKg - target)).toFloat().coerceIn(0f, 1f)
            Box(Modifier.padding(top = Spacing.sm).fillMaxWidth().height(8.dp).background(cs.surfaceContainerHighest)) {
                Box(Modifier.fillMaxWidth(frac).height(8.dp).background(cs.primary))
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(stringResource(R.string.s31_first_week, VnFormat.decimal1(p.startKg)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.s31_goal_kg, VnFormat.decimal1(target)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(top = Spacing.md), color = cs.outlineVariant)
            val gain = s.profile.goalType == GoalType.GAIN
            val text = when {
                p.remainingKg < 0.05 -> stringResource(R.string.s31_progress_reached, VnFormat.kg(target))
                p.eta != null -> stringResource(
                    if (gain) R.string.s31_progress_gain else R.string.s31_progress_lose,
                    VnFormat.kg(abs(p.changedKg)), VnFormat.kg(p.remainingKg), VnFormat.date(p.eta), p.etaWeeks ?: 0,
                )
                else -> null
            }
            if (text != null) NoteRow(Icons.Rounded.Flag, text, tint = cs.primary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrendCard(s: WeightUiState, vm: WeightScreenViewModel) {
    val cs = MaterialTheme.colorScheme
    StatCard {
        Text(stringResource(R.string.s31_trend), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.semantics { heading() })
        val tabs = listOf(R.string.s31_range_7, R.string.s31_range_30, R.string.s31_range_90, R.string.s31_range_all)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = Spacing.md)) {
            WeightRangeTab.entries.forEachIndexed { i, t ->
                SegmentedButton(selected = s.tab == t, onClick = { vm.setTab(t) }, shape = SegmentedButtonDefaults.itemShape(i, 4), icon = {}) {
                    Text(stringResource(tabs[i]), maxLines = 1)
                }
            }
        }
        val r = s.range ?: return@StatCard
        val from = s.tab.days?.let { s.today.minusDays((it - 1).toLong()) } ?: r.points.first().date
        val span = java.time.temporal.ChronoUnit.DAYS.between(from, s.today)
        val step = when {
            span <= 10 -> 2L
            span <= 40 -> 7L
            span <= 100 -> 21L
            else -> (span / 4).coerceAtLeast(7)
        }
        val ticks = generateSequence(s.today) { it.minusDays(step) }.takeWhile { !it.isBefore(from) }.toList().reversed()
            .map { it.toEpochDay() to VnFormat.dayMonth(it) }
        val data = WeightChartData(
            points = r.points.map { it.date.toEpochDay() to it.kg },
            weekly = r.weeks.map { w ->
                val mid = w.weekStart.plusDays(3).let { if (it.isAfter(s.today)) s.today else it }
                (if (mid.isBefore(from)) from else mid).toEpochDay() to w.avgKg
            },
            fromDay = from.toEpochDay(), toDay = s.today.toEpochDay(),
            goalKg = s.profile?.targetWeightKg,
            ticks = ticks,
        )
        val cd = stringResource(R.string.s31_chart_cd, stringResource(tabs[s.tab.ordinal]), VnFormat.decimal1(r.firstAvgKg), VnFormat.decimal1(r.lastAvgKg))
        WeightLineChart(data, Modifier.padding(top = Spacing.lg).semantics { contentDescription = cd })
        FlowRow(Modifier.padding(top = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendLine(stringResource(R.string.s31_legend_avg), dashed = false, color = cs.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(cs.onSurface))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.s31_legend_points, r.points.size), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            s.profile?.targetWeightKg?.let { g ->
                LegendLine(
                    stringResource(if (weightGoalInFrame(data)) R.string.s31_legend_goal else R.string.s31_legend_goal_below, VnFormat.kg(g)),
                    dashed = true,
                )
            }
        }
        HorizontalDivider(Modifier.padding(top = Spacing.md), color = cs.outlineVariant)
        if (r.weeks.size > 1) {
            NoteRow(
                Icons.AutoMirrored.Rounded.TrendingDown,
                stringResource(R.string.s31_range_change, stringResource(tabs[s.tab.ordinal]), VnFormat.decimal1(r.firstAvgKg), VnFormat.decimal1(r.lastAvgKg), VnFormat.signedKg(r.changeKg)),
            )
        }
        r.kgPerWeek?.let { k ->
            val plan = s.profile?.weeklyRateKg ?: 0.0
            val planSigned = when (s.profile?.goalType) {
                GoalType.LOSE -> -plan
                GoalType.GAIN -> plan
                else -> 0.0
            }
            NoteRow(Icons.Rounded.Speed, stringResource(R.string.s31_speed, signed2(k), signed2(planSigned)))
        }
        NoteRow(Icons.Rounded.Info, stringResource(R.string.s31_explain))
    }
}

private fun signed2(x: Double): String {
    val r = Math.round(x * 100) / 100.0
    val body = VnFormat.decimal2(abs(r)).trimEnd('0').trimEnd(',')
    return when {
        r < 0 -> "−$body"
        r > 0 -> "+$body"
        else -> "0"
    }
}

@Composable
private fun BmiCard(s: WeightUiState) {
    val cs = MaterialTheme.colorScheme
    val p = s.profile ?: return
    val latest = s.entries.maxByOrNull { it.date } ?: return
    val bmi = NutritionCalculator.bmi(latest.weightKg, p.heightCm)
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.bmi_value, VnFormat.decimal1(bmi)), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.bmi_asia, stringResource(NutritionCalculator.bmiClass(bmi).label())), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        BmiScale(bmi, Modifier.padding(top = Spacing.sm))
        Text(
            stringResource(R.string.s31_bmi_from, VnFormat.dayMonth(latest.date), VnFormat.kg(latest.weightKg), VnFormat.int(p.heightCm.toInt())),
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

@Composable
private fun WeekHeader(g: WeekGroup, today: LocalDate) {
    val cs = MaterialTheme.colorScheme
    val thisWeek = g.weekStart == WeightTrendCalculator.weekStart(today)
    Column(Modifier.fillMaxWidth().padding(start = Spacing.sm, end = Spacing.sm, top = Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val range = stringResource(R.string.s31_week_header, VnFormat.dayMonth(g.weekStart), VnFormat.dayMonth(g.weekStart.plusDays(6)))
            Text(
                if (thisWeek) stringResource(R.string.gd6_this_week_range, VnFormat.dayMonth(g.weekStart), VnFormat.dayMonth(g.weekStart.plusDays(6))) else range,
                style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f),
            )
            g.avg?.let { a ->
                Text(stringResource(R.string.s31_week_avg_row, VnFormat.decimal1(a)), style = MaterialTheme.typography.labelLarge, color = cs.onSurface)
                g.change?.let { c -> Text(" · " + VnFormat.signedKg(c) + " kg", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant) }
            }
        }
        if (thisWeek && g.entries.isEmpty()) {
            Text(stringResource(R.string.s31_no_weigh_this_week), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.xs))
        }
    }
}

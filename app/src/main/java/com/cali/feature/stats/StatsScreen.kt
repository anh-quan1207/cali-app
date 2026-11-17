package com.cali.feature.stats

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
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
import com.cali.core.calc.DayStatus
import com.cali.core.calc.WeeklySummaryPlanner
import com.cali.core.data.repo.StatsRepository
import com.cali.core.ui.component.DayBarChart
import com.cali.core.ui.component.StatCard
import com.cali.core.ui.component.WeekdayAxis
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

data class StatsUiState(
    val monthMode: Boolean,
    val period: PeriodView,
    /** Chỉ số ngày đang chọn trên biểu đồ tháng. */
    val selected: Int?,
    val canNext: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(handle: SavedStateHandle, private val stats: StatsRepository) : ViewModel() {
    private val today = stats.today()
    private val monthMode = MutableStateFlow(handle.get<Boolean>("month") ?: false)
    private val week = MutableStateFlow(WeeklySummaryPlanner.weekStart(today))
    private val month = MutableStateFlow(YearMonth.from(today))
    private val selected = MutableStateFlow<Int?>(null)

    val state: StateFlow<StatsUiState?> = combine(monthMode, week, month) { m, w, mo -> Triple(m, w, mo) }
        .flatMapLatest { (m, w, mo) ->
            val (from, to) = if (m) Periods.monthRange(mo) else Periods.weekRange(w)
            combine(stats.observe(from, to), selected) { data, sel ->
                val v = if (m) Periods.month(data, mo) else Periods.week(data, w)
                val canNext = if (m) mo.isBefore(YearMonth.from(data.today)) else w.isBefore(WeeklySummaryPlanner.weekStart(data.today))
                val defaultSel = if (m) {
                    v.stats.days.indexOfLast { it.status != DayStatus.FUTURE && it.status != DayStatus.BEFORE_START }.takeIf { it >= 0 }
                } else {
                    null
                }
                StatsUiState(m, v, sel ?: defaultSel, canNext)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setMonthMode(m: Boolean) {
        monthMode.value = m
        selected.value = null
    }

    fun shift(n: Long) {
        selected.value = null
        if (monthMode.value) month.value = month.value.plusMonths(n) else week.value = week.value.plusWeeks(n)
    }

    fun select(i: Int) {
        selected.value = i
    }
}

@Composable
fun StatsRoute(
    onBack: () -> Unit,
    onOpenDay: (LocalDate) -> Unit,
    onOpenWeekly: (LocalDate) -> Unit,
    onOpenWeight: () -> Unit,
    vm: StatsViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val st = s ?: return
    StatsScreen(st, vm, onBack, onOpenDay, onOpenWeekly, onOpenWeight)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsScreen(
    s: StatsUiState,
    vm: StatsViewModel,
    onBack: () -> Unit,
    onOpenDay: (LocalDate) -> Unit,
    onOpenWeekly: (LocalDate) -> Unit,
    onOpenWeight: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0 } }
    val bar by animateColorAsState(if (scrolled) cs.surfaceContainer else cs.surface, label = "bar")
    val v = s.period
    Column(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.background(bar)) {
            BackTitleBar(stringResource(R.string.s32_title), onBack, bar)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = Spacing.md)) {
                SegmentedButton(selected = !s.monthMode, onClick = { vm.setMonthMode(false) }, shape = SegmentedButtonDefaults.itemShape(0, 2), icon = {}) {
                    Text(stringResource(R.string.s32_tab_week))
                }
                SegmentedButton(selected = s.monthMode, onClick = { vm.setMonthMode(true) }, shape = SegmentedButtonDefaults.itemShape(1, 2), icon = {}) {
                    Text(stringResource(R.string.s32_tab_month))
                }
            }
            val title = if (s.monthMode) stringResource(R.string.gd6_month, v.start.monthValue, v.start.year) else weekTitle(v.start, v.today)
            Spacer(Modifier.height(Spacing.sm))
            PeriodNav(title, periodSub(v), v.stats.isPartial, s.canNext, onPrev = { vm.shift(-1) }, onNext = { vm.shift(1) })
            Spacer(Modifier.height(Spacing.sm))
        }
        LazyColumn(
            state = list, modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item(key = "cal") { CalorieAvgCard(s, vm, onOpenDay) }
            if (!s.monthMode) item(key = "weekly") { WeeklyLink(v, onOpenWeekly) }
            if (v.stats.averagesReady) {
                item(key = "macro") {
                    MacroCard(v, stringResource(R.string.s35_macro), showProteinDays = false) {
                        Text(stringResource(R.string.s32_protein_hit, v.stats.proteinMetDays, v.stats.elapsedDays), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                }
                item(key = "water") { WaterAvgCard(v) }
            }
            item(key = "weight") { WeightRowCard(v, s.monthMode, onOpenWeight) }
            if (v.stats.averagesReady) item(key = "cmp") { CompareCard(v, s.monthMode) }
            item(key = "foot") {
                Text(
                    stringResource(R.string.s32_footer), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun CalorieAvgCard(s: StatsUiState, vm: StatsViewModel, onOpenDay: (LocalDate) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val v = s.period
    val st = v.stats
    val target = v.target?.targetKcal ?: 0
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s32_cal_avg), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            Text(stringResource(R.string.s32_logged, st.loggedDays, st.elapsedDays), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        val avg = st.avgKcal
        if (!st.averagesReady || avg == null) {
            if (v.firstUse != null && v.firstUse.isAfter(v.start)) {
                NoteRow(Icons.Rounded.Flag, stringResource(R.string.s32_newbie, VnFormat.dayMonth(v.firstUse)), tint = cs.primary)
            }
        } else {
            Row(Modifier.padding(top = Spacing.sm), verticalAlignment = Alignment.Bottom) {
                Text(VnFormat.int(avg.roundToInt()), style = MaterialTheme.typography.displayLarge, color = cs.onSurface)
                Text(" kcal", style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp).weight(1f))
                if (target > 0) {
                    val d = (avg - target).roundToInt()
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 8.dp)) {
                        val over = d > target * 0.1
                        Text(
                            VnFormat.signedInt(d), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (d > 0 && over) cs.error else if (d > 0) cc.warn else cs.onSurface,
                        )
                        Text(stringResource(R.string.s32_vs_target, VnFormat.int(target)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                }
            }
            Text(
                stringResource(
                    if (v.todayLogged && st.isPartial) R.string.s32_total_today else R.string.s32_total,
                    VnFormat.int(st.sumEatenKcal.roundToInt()), st.loggedDays,
                ),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
            )
        }
        val bars = kcalBars(v, labels = !s.monthMode, suspect = true)
        if (s.monthMode) {
            DayBarChart(
                bars, target.toDouble().takeIf { it > 0 }, Modifier.padding(top = Spacing.lg),
                plotHeight = 110.dp, avg = avg?.takeIf { st.averagesReady }, barWidth = 8.dp,
                selected = s.selected, onSelect = { i -> if (st.days[i].status != DayStatus.FUTURE && st.days[i].status != DayStatus.BEFORE_START) vm.select(i) },
            )
            MonthAxis(v, s.selected)
            s.selected?.let { i -> SelectedDayRow(v, i, vm, onOpenDay) }
        } else {
            DayBarChart(
                bars, target.toDouble().takeIf { it > 0 }, Modifier.padding(top = Spacing.lg),
                avg = avg?.takeIf { st.averagesReady },
                onSelect = { i -> if (st.days[i].status != DayStatus.FUTURE && st.days[i].status != DayStatus.BEFORE_START) onOpenDay(st.days[i].date) },
            )
            val short = stringArrayResource(R.array.weekday_short)
            WeekdayAxis(short.toList(), highlight = if (v.includesToday && st.isPartial) v.today.dayOfWeek.value - 1 else null, dim = { st.days[it].status == DayStatus.FUTURE })
        }
        StatusLegend(v, showSuspect = true, avgLabel = avg?.takeIf { st.averagesReady }?.let { stringResource(R.string.gd6_legend_avg, VnFormat.int(it.roundToInt())) })
        SuspectNotes(v)
        if (st.loggedDays > 0) {
            HorizontalDivider(Modifier.padding(top = Spacing.md), color = cs.outlineVariant)
            MaxMinRow(v)
        }
    }
}

@Composable
private fun MonthAxis(v: PeriodView, selected: Int?) {
    val cs = MaterialTheme.colorScheme
    val n = v.stats.days.size
    HorizontalDivider(color = cs.outlineVariant)
    BoxWithConstraints(Modifier.fillMaxWidth().height(22.dp)) {
        val col = maxWidth / n
        v.stats.days.forEachIndexed { i, d ->
            if (d.date.dayOfWeek.value == 1) {
                Text(
                    VnFormat.dayMonth(d.date), style = MaterialTheme.typography.labelSmall,
                    color = if (d.date.isAfter(v.today)) cs.onSurface.copy(alpha = 0.38f) else cs.onSurfaceVariant,
                    modifier = Modifier.offset(x = minOf(col * i, maxWidth - 36.dp)).padding(top = 4.dp),
                )
            }
        }
        if (selected != null) Box(Modifier.offset(x = col * selected + col / 2 - 2.dp, y = 16.dp).size(4.dp).clip(androidx.compose.foundation.shape.CircleShape).background(cs.primary))
    }
}

@Composable
private fun SelectedDayRow(v: PeriodView, i: Int, vm: StatsViewModel, onOpenDay: (LocalDate) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val d = v.stats.days[i]
    val first = v.stats.days.indexOfFirst { it.status != DayStatus.BEFORE_START }
    val last = v.stats.days.indexOfLast { it.status != DayStatus.FUTURE }
    Row(
        Modifier.padding(top = Spacing.sm).fillMaxWidth().clip(MaterialTheme.shapes.medium).background(cs.surfaceContainer).padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { vm.select(i - 1) }, enabled = i > first) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.prev_period)) }
        Column(Modifier.weight(1f)) {
            Text(VnFormat.weekdayDayMonth(d.date), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
            val kcal = v.kcalOf(d.date).roundToInt()
            val sub = when {
                d.status == DayStatus.NOT_LOGGED -> stringResource(R.string.s32_day_info_none)
                d.date == v.today -> stringResource(R.string.s32_day_info, VnFormat.int(kcal)) + " · " + stringResource(R.string.gd6_today_logging)
                else -> stringResource(R.string.s32_day_info, VnFormat.int(kcal))
            }
            Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        IconButton(onClick = { vm.select(i + 1) }, enabled = i < last) { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.next_period)) }
        TextButton(onClick = { onOpenDay(d.date) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.s32_open_day)) }
    }
}

@Composable
private fun WeeklyLink(v: PeriodView, onOpenWeekly: (LocalDate) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val st = v.stats
    if (st.elapsedDays == 0) return
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).clickable { onOpenWeekly(v.start) }
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.medium).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Insights, null, tint = cs.onPrimaryContainer)
        }
        Spacer(Modifier.width(Spacing.lg))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.s32_open_weekly), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(
                if (st.isPartial) stringResource(R.string.s32_open_weekly_sub_partial, st.score.total)
                else stringResource(R.string.s32_open_weekly_sub, st.score.total, gradeText(st.grade)),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
            )
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
    }
}

@Composable
private fun WaterAvgCard(v: PeriodView) {
    val cs = MaterialTheme.colorScheme
    val st = v.stats
    val goal = v.target?.targetWaterMl ?: 0
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s32_water_avg), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            Text(stringResource(R.string.s32_water_hit, st.waterMetDays, st.elapsedDays), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        Row(Modifier.padding(top = Spacing.sm), verticalAlignment = Alignment.Bottom) {
            Text(VnFormat.decimal2(st.avgWaterMl / 1000.0).trimEnd('0').trimEnd(','), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
            Text(" L", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f).padding(bottom = 4.dp))
            if (goal > 0) Text(stringResource(R.string.s32_water_of, VnFormat.decimal1(goal / 1000.0)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        BoxWithConstraints(Modifier.padding(top = Spacing.sm).fillMaxWidth().height(14.dp)) {
            val w = maxWidth
            val ratio = if (goal > 0) (st.avgWaterMl / goal).toFloat() else 0f
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(10.dp).background(cs.surfaceContainerHighest))
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(ratio.coerceIn(0f, 1f)).height(10.dp).background(cs.primary))
            Box(Modifier.align(Alignment.CenterStart).offset(x = w - 2.dp).width(2.dp).fillMaxHeight().background(cs.onSurface))
        }
        Text(
            stringResource(R.string.s32_water_total, VnFormat.decimal1(st.waterTotalMl / 1000.0), VnFormat.decimal1(st.waterTargetTotalMl / 1000.0), VnFormat.decimal1(st.waterShortfallMl / 1000.0)),
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

@Composable
private fun WeightRowCard(v: PeriodView, month: Boolean, onOpenWeight: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).clickable(onClick = onOpenWeight)
            .padding(start = Spacing.lg, end = Spacing.lg, top = 14.dp, bottom = Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s35_weight), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
        }
        val avg = v.weightAvg
        if (avg == null) {
            Text(stringResource(R.string.s32_weight_none), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm))
            return@Column
        }
        Row(Modifier.padding(top = Spacing.sm), verticalAlignment = Alignment.Bottom) {
            Text(VnFormat.decimal1(avg), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
            Text(" " + stringResource(R.string.s32_weight_line, v.weights.size), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f).padding(bottom = 4.dp))
            v.prevWeightAvg?.let { p ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(VnFormat.signedKg(avg - p) + " kg", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Text(stringResource(R.string.s32_weight_vs, VnFormat.decimal1(p)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
            }
        }
        if (month) {
            val pts = v.data.weights.filter { it.date in v.start..v.end }
            if (pts.size >= 2) {
                Text(
                    stringResource(R.string.s32_weight_period, VnFormat.kg(pts.first().kg), VnFormat.kg(pts.last().kg)),
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun CompareCard(v: PeriodView, month: Boolean) {
    val cs = MaterialTheme.colorScheme
    val c = v.comparison
    StatCard {
        Text(stringResource(R.string.s32_compare), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.semantics { heading() })
        if (c == null || !c.enough) {
            Text(stringResource(R.string.s32_compare_missing), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.xs))
            Text(stringResource(R.string.s32_compare_note), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm))
            return@StatCard
        }
        val p = c.previous
        val span = if (v.stats.isPartial) stringResource(R.string.s32_compare_same, p.elapsedDays) else stringResource(R.string.s32_compare_full, p.days.size)
        val prevStart = v.prevStart
        Text(
            if (month) stringResource(R.string.s32_compare_month, span)
            else stringResource(R.string.s32_compare_week, VnFormat.dayMonth(prevStart), VnFormat.dayMonth(prevStart.plusDays(6)), span),
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
        )
        Row(Modifier.fillMaxWidth().padding(top = Spacing.md, bottom = Spacing.xs)) {
            Spacer(Modifier.weight(1.4f))
            listOf(R.string.s32_col_now, R.string.s32_col_prev, R.string.s32_col_diff).forEach {
                Text(stringResource(it), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
        val s = v.stats
        fun diff(a: Double?, b: Double?, unit: String, fmt: (Double) -> String): String {
            if (a == null || b == null) return "—"
            val d = a - b
            val ar = if (abs(d) < 1e-9) "" else if (d > 0) "↑ " else "↓ "
            return ar + fmt(abs(d)) + unit
        }
        CompareRow(stringResource(R.string.s32_row_kcal), s.avgKcal?.let { VnFormat.int(it.roundToInt()) } ?: "—", p.avgKcal?.let { VnFormat.int(it.roundToInt()) } ?: "—",
            diff(s.avgKcal, p.avgKcal, "") { VnFormat.int(it.roundToInt()) })
        CompareRow(stringResource(R.string.s32_row_on), "${s.onTargetDays}/${s.elapsedDays}", "${p.onTargetDays}/${p.elapsedDays}",
            diff(s.onTargetDays.toDouble(), p.onTargetDays.toDouble(), " " + stringResource(R.string.s32_unit_days)) { it.roundToInt().toString() })
        CompareRow(stringResource(R.string.s32_row_protein), s.avgProteinG?.let { "${it.roundToInt()} g" } ?: "—", p.avgProteinG?.let { "${it.roundToInt()} g" } ?: "—",
            diff(s.avgProteinG, p.avgProteinG, " g") { it.roundToInt().toString() })
        CompareRow(stringResource(R.string.s32_row_water), VnFormat.decimal2(s.avgWaterMl / 1000.0) + " L", VnFormat.decimal2(p.avgWaterMl / 1000.0) + " L",
            diff(s.avgWaterMl, p.avgWaterMl, " ml") { VnFormat.int(it.roundToInt()) })
        val wNow = v.weightAvg
        val wPrev = v.prevWeightAvg
        if (wNow != null && wPrev != null) {
            CompareRow(stringResource(R.string.s32_row_weight), VnFormat.decimal1(wNow) + " kg", VnFormat.decimal1(wPrev) + " kg",
                diff(wNow, wPrev, " kg") { VnFormat.decimal1(it) })
        }
        Text(stringResource(R.string.s32_compare_note), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.md))
    }
}

@Composable
private fun CompareRow(label: String, now: String, prev: String, diff: String) {
    val cs = MaterialTheme.colorScheme
    HorizontalDivider(color = cs.outlineVariant)
    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = cs.onSurface, modifier = Modifier.weight(1.4f))
        Text(now, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(prev, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(diff, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

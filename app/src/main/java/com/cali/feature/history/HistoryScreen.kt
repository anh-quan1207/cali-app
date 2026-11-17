package com.cali.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
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
import com.cali.core.calc.DayStatus
import com.cali.core.calc.WeeklyStats
import com.cali.core.calc.WeeklyStatsCalculator
import com.cali.core.calc.WeeklySummaryPlanner
import com.cali.core.calc.WeightHeadline
import com.cali.core.calc.WeightTrendCalculator
import com.cali.core.data.repo.MyFoodRepository
import com.cali.core.data.repo.RangeData
import com.cali.core.data.repo.StatsRepository
import com.cali.core.ui.component.DayBarChart
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import com.cali.feature.stats.PeriodView
import com.cali.feature.stats.Periods
import com.cali.feature.stats.StatusLegend
import com.cali.feature.stats.gradeText
import com.cali.feature.stats.kcalBars
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.roundToInt

data class HistoryUiState(
    val month: YearMonth,
    val today: LocalDate,
    val firstUse: LocalDate?,
    val weeks: List<PeriodView>,
    val monthView: PeriodView,
    val thisWeek: PeriodView?,
    val lastWeek: WeeklyStats?,
    val weight: WeightHeadline?,
    val latestKg: Double?,
    val myFoods: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(private val stats: StatsRepository, myFoods: MyFoodRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.from(stats.today()))

    val state: StateFlow<HistoryUiState?> = month.flatMapLatest { m ->
        val today = stats.today()
        val firstWeek = WeekRows.firstMonday(m)
        val lastWeek = WeekRows.lastMonday(m)
        val thisWeek = WeeklySummaryPlanner.weekStart(today)
        val from = minOf(firstWeek.minusWeeks(1), m.minusMonths(1).atDay(1), thisWeek.minusWeeks(2))
        val to = maxOf(lastWeek.plusDays(6), thisWeek.plusDays(6))
        combine(stats.observe(from, to), myFoods.observeAll().map { it.size }) { data, foods -> build(m, data, foods) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun build(m: YearMonth, data: RangeData, foods: Int): HistoryUiState {
        val today = data.today
        val mondays = generateSequence(WeekRows.firstMonday(m)) { it.plusWeeks(1) }.takeWhile { !it.isAfter(WeekRows.lastMonday(m)) }.toList()
        val weeks = mondays.map { Periods.week(data, it) }
        val thisWeekStart = WeeklySummaryPlanner.weekStart(today)
        val lastWeekStart = thisWeekStart.minusWeeks(1)
        val lastWeekStats = WeeklyStatsCalculator.compute(lastWeekStart, today, data.daysIn(lastWeekStart, lastWeekStart.plusDays(6)), firstUseDate = data.firstUseDate)
        val headline = WeightTrendCalculator.headline(data.weights, today)
        return HistoryUiState(
            month = m,
            today = today,
            firstUse = data.firstUseDate,
            weeks = weeks,
            monthView = Periods.month(data, m),
            thisWeek = Periods.week(data, thisWeekStart),
            lastWeek = lastWeekStats.takeIf { it.elapsedDays > 0 && it.loggedDays > 0 },
            weight = headline,
            latestKg = data.weights.lastOrNull()?.kg,
            myFoods = foods,
        )
    }

    fun shift(n: Long) {
        val next = month.value.plusMonths(n)
        if (!next.isAfter(YearMonth.from(stats.today()))) month.value = next
    }
}

object WeekRows {
    fun firstMonday(m: YearMonth): LocalDate = WeeklySummaryPlanner.weekStart(m.atDay(1))
    fun lastMonday(m: YearMonth): LocalDate = WeeklySummaryPlanner.weekStart(m.atEndOfMonth())
}

@Composable
fun HistoryRoute(
    onOpenDay: (LocalDate) -> Unit,
    onOpenWeekly: (LocalDate?) -> Unit,
    onOpenWeight: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenMyFoods: () -> Unit,
    vm: HistoryViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val st = s ?: return
    HistoryScreen(st, vm, onOpenDay, onOpenWeekly, onOpenWeight, onOpenStats, onOpenMyFoods)
}

@Composable
private fun HistoryScreen(
    s: HistoryUiState,
    vm: HistoryViewModel,
    onOpenDay: (LocalDate) -> Unit,
    onOpenWeekly: (LocalDate?) -> Unit,
    onOpenWeight: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenMyFoods: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val short = stringArrayResource(R.array.weekday_short)
    val canNext = s.month.isBefore(YearMonth.from(s.today))
    Column(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.fillMaxWidth().background(cs.surface).padding(top = Spacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.s30_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface, modifier = Modifier.semantics { heading() })
            Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.shift(-1) }) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.prev_period), tint = cs.onSurfaceVariant) }
                Text(
                    stringResource(R.string.gd6_month, s.month.monthValue, s.month.year), style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                IconButton(onClick = { vm.shift(1) }, enabled = canNext) {
                    Icon(Icons.Rounded.ChevronRight, stringResource(R.string.next_period), tint = if (canNext) cs.onSurfaceVariant else cs.onSurface.copy(alpha = 0.38f))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.xs)) {
                short.forEach { Text(it, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = Spacing.sm, end = Spacing.sm, bottom = Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            s.weeks.forEach { w ->
                item(key = "w-${w.start}") {
                    val active = w.stats.elapsedDays > 0
                    if (active) WeekCard(w, s, onOpenDay, onOpenWeekly) else FadedRow(w, s.month)
                }
            }
            item(key = "legend") {
                Column(Modifier.padding(horizontal = Spacing.sm)) {
                    StatusLegend(s.monthView, showSuspect = true, goalBand = false)
                }
            }
            val fu = s.firstUse
            if (fu != null && YearMonth.from(fu) == s.month && fu.dayOfMonth > 1) {
                item(key = "newbie") {
                    val nextMonday = WeeklySummaryPlanner.weekStart(fu).plusWeeks(1)
                    Row(
                        Modifier.padding(top = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.lg),
                    ) {
                        Icon(Icons.Rounded.Flag, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.md))
                        Text(stringResource(R.string.s30_newbie, VnFormat.dayMonth(fu), VnFormat.dayMonth(nextMonday)), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
            }
            s.thisWeek?.takeIf { it.stats.elapsedDays > 0 && s.month == YearMonth.from(s.today) }?.let { tw ->
                item(key = "summary") { WeekSummary(tw) }
            }
            item(key = "more") { MoreLinks(s, onOpenWeekly, onOpenWeight, onOpenStats, onOpenMyFoods) }
        }
    }
}

@Composable
private fun WeekCard(w: PeriodView, s: HistoryUiState, onOpenDay: (LocalDate) -> Unit, onOpenWeekly: (LocalDate?) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val isThis = w.includesToday
    val st = w.stats
    val end = w.start.plusDays(6)
    val cd = stringResource(R.string.s30_week_cd, VnFormat.dayMonth(w.start), VnFormat.dayMonth(end))
    val border = if (isThis) Modifier.border(1.dp, cs.primary, MaterialTheme.shapes.large) else Modifier
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).then(border).padding(bottom = Spacing.sm)) {
        Row(
            Modifier.fillMaxWidth().clickable { onOpenWeekly(w.start) }.semantics(mergeDescendants = true) { contentDescription = cd }
                .heightIn(min = 48.dp).padding(start = Spacing.md, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (isThis) stringResource(R.string.gd6_this_week_range, VnFormat.dayMonth(w.start), VnFormat.dayMonth(end))
                else VnFormat.dayMonth(w.start) + " – " + VnFormat.dayMonth(end),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (isThis) FontWeight.SemiBold else FontWeight.Medium),
                color = cs.onSurface, modifier = Modifier.weight(1f),
            )
            if (st.isPartial) {
                val avgReady = st.averagesReady
                Text(
                    if (avgReady) stringResource(R.string.s30_week_partial, st.score.total) else stringResource(R.string.s30_week_partial_only),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSecondaryContainer,
                    modifier = Modifier.clip(MaterialTheme.shapes.small).background(cs.secondaryContainer).padding(horizontal = 8.dp, vertical = 3.dp),
                )
            } else {
                Text(st.score.total.toString(), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Text(" · " + gradeText(st.grade), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm)) {
            st.days.forEach { d ->
                val inMonth = YearMonth.from(d.date) == s.month
                val future = d.status == DayStatus.FUTURE
                val clickable = !future && d.status != DayStatus.BEFORE_START
                val isToday = d.date == s.today
                val dayCd = stringResource(R.string.s30_day_cd, VnFormat.weekdayDayMonth(d.date), VnFormat.int(w.kcalOf(d.date).roundToInt()) + " kcal")
                Box(
                    Modifier.weight(1f).height(36.dp).let { if (clickable) it.clickable { onOpenDay(d.date) }.semantics { contentDescription = dayCd } else it },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (d.date == s.firstUse) Icon(Icons.Rounded.Flag, null, tint = cs.primary, modifier = Modifier.size(12.dp))
                        Box(
                            Modifier.size(30.dp).let { if (isToday) it.border(1.5.dp, cs.primary, CircleShape) else it },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                d.date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (clickable && inMonth) FontWeight.SemiBold else FontWeight.Normal),
                                color = when {
                                    isToday -> cs.primary
                                    future || !inMonth || d.status == DayStatus.BEFORE_START -> cs.onSurface.copy(alpha = 0.38f)
                                    else -> cs.onSurface
                                },
                            )
                        }
                    }
                    if (d.suspectedUnderLog) {
                        Icon(Icons.Rounded.Warning, null, tint = CaliTheme.colors.warn, modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp).size(12.dp))
                    }
                }
            }
        }
        DayBarChart(
            kcalBars(w, labels = false), w.target?.targetKcal?.toDouble(),
            Modifier.padding(horizontal = Spacing.sm), plotHeight = 30.dp, band = false, barWidth = 16.dp,
        )
    }
}

@Composable
private fun FadedRow(w: PeriodView, month: YearMonth) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm).height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        w.stats.days.forEach { d ->
            val inMonth = YearMonth.from(d.date) == month
            Text(
                d.date.dayOfMonth.toString(), style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface.copy(alpha = if (inMonth) 0.38f else 0.2f),
                modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeekSummary(w: PeriodView) {
    val cs = MaterialTheme.colorScheme
    val st = w.stats
    Column(Modifier.padding(top = Spacing.lg)) {
        Text(
            stringResource(R.string.s30_summary_this), style = MaterialTheme.typography.titleMedium, color = cs.onSurface,
            modifier = Modifier.padding(start = Spacing.sm, bottom = Spacing.sm).semantics { heading() },
        )
        Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(vertical = Spacing.md)) {
            Cell(stringResource(R.string.s30_avg_day), st.avgKcal?.takeIf { st.averagesReady }?.let { VnFormat.int(it.roundToInt()) } ?: "—", " kcal", Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(48.dp).background(cs.outlineVariant))
            Cell(stringResource(R.string.s30_days_on), st.onTargetDays.toString(), " " + stringResource(R.string.s30_days_of, st.elapsedDays), Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(48.dp).background(cs.outlineVariant))
            Cell(stringResource(R.string.s30_water_avg), if (st.averagesReady) VnFormat.decimal1(st.avgWaterMl / 1000.0) else "—", " L", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Cell(label: String, value: String, unit: String, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = Spacing.md)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
            Text(unit, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun MoreLinks(s: HistoryUiState, onOpenWeekly: (LocalDate?) -> Unit, onOpenWeight: () -> Unit, onOpenStats: () -> Unit, onOpenMyFoods: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(top = Spacing.lg)) {
        Text(stringResource(R.string.s30_more), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.padding(start = Spacing.sm, bottom = Spacing.sm).semantics { heading() })
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow)) {
            val lw = s.lastWeek
            LinkRow(
                Icons.Rounded.Insights, stringResource(R.string.s30_go_weekly),
                if (lw != null) stringResource(R.string.s30_go_weekly_sub, lw.score.total, gradeText(lw.grade)) else stringResource(R.string.s30_go_weekly_sub_none),
            ) { onOpenWeekly(null) }
            HorizontalDivider(color = cs.outlineVariant)
            val h = s.weight
            LinkRow(
                Icons.Rounded.MonitorWeight, stringResource(R.string.s30_go_weight),
                when {
                    h?.changeKg != null -> stringResource(R.string.s30_go_weight_sub, VnFormat.decimal1(h.week.avgKg), VnFormat.signedKg(h.changeKg))
                    s.latestKg != null -> stringResource(R.string.s30_go_weight_sub_one, VnFormat.kg(s.latestKg))
                    else -> stringResource(R.string.s30_go_weight_none)
                },
                onOpenWeight,
            )
            HorizontalDivider(color = cs.outlineVariant)
            LinkRow(Icons.Rounded.BarChart, stringResource(R.string.s30_go_stats), stringResource(R.string.s30_go_stats_sub), onOpenStats)
            HorizontalDivider(color = cs.outlineVariant)
            LinkRow(Icons.AutoMirrored.Rounded.MenuBook, stringResource(R.string.s30_go_myfoods), stringResource(R.string.s30_go_myfoods_sub, s.myFoods), onOpenMyFoods)
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 64.dp).padding(horizontal = Spacing.lg, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = cs.onPrimaryContainer)
        }
        Spacer(Modifier.width(Spacing.lg))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
    }
}

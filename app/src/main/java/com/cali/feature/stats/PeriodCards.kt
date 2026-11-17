package com.cali.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.DayResult
import com.cali.core.calc.DayStatus
import com.cali.core.calc.Trend
import com.cali.core.calc.WeekGrade
import com.cali.core.ui.component.BarKind
import com.cali.core.ui.component.DayBar
import com.cali.core.ui.component.DayBarChart
import com.cali.core.ui.component.EnergyBar
import com.cali.core.ui.component.LegendLine
import com.cali.core.ui.component.LegendSwatch
import com.cali.core.ui.component.MacroTargetBar
import com.cali.core.ui.component.StatCard
import com.cali.core.ui.component.WeekdayAxis
import com.cali.core.ui.component.barColors
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

// ---------- Thanh trên + chọn kỳ ----------

@Composable
fun BackTitleBar(title: String, onBack: () -> Unit, background: Color = MaterialTheme.colorScheme.surface) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().background(background).statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = cs.onSurface) }
        Text(title, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, modifier = Modifier.semantics { heading() })
    }
}

/** ‹ Tuần 03/11 – 09/11 › + dòng phụ (Tạm tính / Đã kết thúc). */
@Composable
fun PeriodNav(title: String, sub: String?, partial: Boolean, canNext: Boolean, onPrev: () -> Unit, onNext: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.prev_period), tint = cs.onSurfaceVariant) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            if (sub != null) {
                val bg = if (partial) cs.primaryContainer.copy(alpha = 0.6f) else Color.Transparent
                Row(
                    Modifier.padding(top = 2.dp).clip(MaterialTheme.shapes.small).background(bg).padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(if (partial) Icons.Rounded.HourglassTop else Icons.Rounded.CalendarMonth, null, tint = if (partial) cs.onPrimaryContainer else cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(sub, style = MaterialTheme.typography.labelMedium, color = if (partial) cs.onPrimaryContainer else cs.onSurfaceVariant)
                }
            }
        }
        IconButton(onClick = onNext, enabled = canNext) {
            Icon(Icons.Rounded.ChevronRight, stringResource(R.string.next_period), tint = if (canNext) cs.onSurfaceVariant else cs.onSurface.copy(alpha = 0.38f))
        }
    }
}

@Composable
fun periodSub(v: PeriodView): String = if (v.stats.isPartial) {
    stringResource(R.string.gd6_partial_left, v.stats.remainingDays)
} else {
    stringResource(R.string.gd6_finished_logged, v.stats.loggedDays, v.stats.elapsedDays)
}

@Composable
fun weekTitle(start: LocalDate, today: LocalDate): String {
    val end = start.plusDays(6)
    return if (!today.isBefore(start) && !today.isAfter(end)) {
        stringResource(R.string.gd6_this_week_range, VnFormat.dayMonth(start), VnFormat.dayMonth(end))
    } else {
        stringResource(R.string.gd6_week_range, VnFormat.dayMonth(start), VnFormat.dayMonth(end))
    }
}

@Composable
fun gradeText(g: WeekGrade): String = stringResource(
    when (g) {
        WeekGrade.EXCELLENT -> R.string.s35_grade_excellent
        WeekGrade.GOOD -> R.string.s35_grade_good
        WeekGrade.NEEDS_WORK -> R.string.s35_grade_needs
        WeekGrade.POOR -> R.string.s35_grade_poor
    },
)

/** Mũi tên trung tính "↑ TB +5%". */
@Composable
fun TrendTag(text: String, up: Boolean?) {
    val cs = MaterialTheme.colorScheme
    Text(
        when (up) {
            true -> "↑ $text"
            false -> "↓ $text"
            null -> text
        },
        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
    )
}

// ---------- Cột ngày ----------

@Composable
fun kcalBars(v: PeriodView, labels: Boolean = true, suspect: Boolean = labels): List<DayBar> {
    val short = stringArrayResource(R.array.weekday_short)
    val sOn = stringResource(R.string.gd6_status_on)
    val sOver = stringResource(R.string.gd6_status_over)
    val sUnder = stringResource(R.string.gd6_status_under)
    val sToday = stringResource(R.string.gd6_today_logging)
    return v.stats.days.map { d ->
        val kcal = v.kcalOf(d.date)
        val name = if (v.isWeek) short[d.date.dayOfWeek.value - 1] else VnFormat.dayMonth(d.date)
        val isToday = d.date == v.today
        when (d.status) {
            DayStatus.FUTURE, DayStatus.BEFORE_START -> DayBar(0.0, BarKind.EMPTY, contentDescription = name)
            DayStatus.NOT_LOGGED -> DayBar(
                0.0, if (isToday) BarKind.EMPTY else BarKind.NONE, label = if (labels && !isToday) "—" else null,
                contentDescription = stringResource(R.string.gd6_bar_cd_none, name),
            )
            else -> {
                val kind = when {
                    isToday && v.stats.isPartial -> BarKind.TODAY
                    d.status == DayStatus.OVER -> BarKind.OVER
                    d.status == DayStatus.UNDER -> BarKind.UNDER
                    else -> BarKind.ON
                }
                val state = when {
                    isToday && v.stats.isPartial -> sToday
                    d.status == DayStatus.OVER -> sOver
                    d.status == DayStatus.UNDER -> sUnder
                    else -> sOn
                }
                DayBar(
                    kcal, kind, label = if (labels) VnFormat.int(kcal.roundToInt()) else null, suspect = suspect && d.suspectedUnderLog,
                    contentDescription = stringResource(R.string.gd6_bar_cd, name, VnFormat.int(kcal.roundToInt()), state),
                )
            }
        }
    }
}

// ---------- Chú giải Đạt / Vượt / Thiếu / Chưa ghi ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusLegend(v: PeriodView, showSuspect: Boolean = false, avgLabel: String? = null, goalBand: Boolean = true) {
    val c = barColors()
    val cs = MaterialTheme.colorScheme
    val s = v.stats
    FlowRow(Modifier.padding(top = Spacing.md), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LegendSwatch(c.on, stringResource(R.string.gd6_legend_on), s.onTargetDays.toString())
        LegendSwatch(c.over, stringResource(R.string.gd6_legend_over), s.overDays.toString())
        val underToday = v.includesToday && s.days.any { it.date == v.today && it.status == DayStatus.UNDER }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendSwatch(c.under, stringResource(R.string.gd6_legend_under), s.underDays.toString())
            if (underToday) Text(" " + stringResource(R.string.gd6_legend_under_today), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        LegendSwatch(null, stringResource(R.string.gd6_legend_none), s.notLoggedDays.toString(), stroke = cs.outline, dashed = true)
        if (showSuspect && s.suspectedUnderLogDays > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, null, tint = CaliTheme.colors.warn, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.gd6_legend_suspect), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Text(" ${s.suspectedUnderLogDays}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = cs.onSurface)
            }
        }
        v.target?.let {
            LegendLine(
                if (goalBand) stringResource(R.string.gd6_legend_goal_band, VnFormat.int(it.targetKcal)) else stringResource(R.string.gd6_legend_goal, VnFormat.int(it.targetKcal)),
                dashed = true,
            )
        }
        if (avgLabel != null) LegendLine(avgLabel, dashed = false, color = cs.primary)
    }
}

@Composable
fun NoteRow(icon: ImageVector, text: String, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(modifier.fillMaxWidth().padding(top = Spacing.sm), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = tint, modifier = Modifier.padding(top = 1.dp).size(16.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** "Nhiều nhất CN 2.630 · ít nhất T4 690 kcal" */
@Composable
fun MaxMinRow(v: PeriodView) {
    val s = v.stats
    val max = s.maxDay ?: return
    val min = s.minDay ?: return
    val short = stringArrayResource(R.array.weekday_short)
    fun name(d: DayResult) = if (v.isWeek && !v.stats.isPartial) short[d.date.dayOfWeek.value - 1] else VnFormat.dayMonth(d.date)
    NoteRow(
        Icons.Rounded.SwapVert,
        stringResource(R.string.gd6_maxmin, name(max), VnFormat.int(v.kcalOf(max.date).roundToInt()), name(min), VnFormat.int(v.kcalOf(min.date).roundToInt())),
    )
}

@Composable
fun SuspectNotes(v: PeriodView) {
    val short = stringArrayResource(R.array.weekday_short)
    v.stats.days.filter { it.suspectedUnderLog }.forEach { d ->
        val name = if (v.isWeek) short[d.date.dayOfWeek.value - 1] else VnFormat.dayMonth(d.date)
        NoteRow(Icons.Rounded.Warning, stringResource(R.string.gd6_suspect_note, name, VnFormat.int(v.kcalOf(d.date).roundToInt())), tint = CaliTheme.colors.warn)
    }
}

@Composable
fun TdeeRow(v: PeriodView) {
    val s = v.stats
    if (s.loggedDays == 0) return
    val tdee = v.target?.tdee ?: return
    val bal = s.tdeeBalanceKcal
    val text = if (bal <= 0) {
        stringResource(R.string.s35_tdee_deficit, VnFormat.int(tdee.roundToInt()), VnFormat.int(abs(bal).roundToInt()), fmt2(s.expectedWeightChangeKg))
    } else {
        stringResource(R.string.s35_tdee_surplus, VnFormat.int(tdee.roundToInt()), VnFormat.int(bal.roundToInt()), fmt2(s.expectedWeightChangeKg))
    }
    NoteRow(Icons.AutoMirrored.Rounded.TrendingDown, text)
}

/** −0,45 (2 chữ số, bỏ số 0 thừa). */
fun fmt2(x: Double): String {
    val r = Math.round(x * 100) / 100.0
    val body = VnFormat.decimal2(abs(r)).trimEnd('0').trimEnd(',')
    return if (r < 0) "−$body" else body
}

// ---------- Thẻ Calo kiểu S35 ----------

@Composable
fun CalorieWeekCard(v: PeriodView, onOpenDay: (LocalDate) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val s = v.stats
    val short = stringArrayResource(R.array.weekday_short)
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s35_cal), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            val prevAvg = v.comparison?.takeIf { it.enough }?.previous?.avgKcal
            if (prevAvg != null && s.avgKcal != null && prevAvg > 0) {
                val pct = ((s.avgKcal - prevAvg) / prevAvg * 100).roundToInt()
                TrendTag(stringResource(R.string.gd6_legend_avg, (if (pct >= 0) "+" else "−") + abs(pct) + "%"), up = if (pct == 0) null else pct > 0)
            }
        }
        if (s.loggedDays == 0) {
            Text(stringResource(R.string.s35_empty), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm))
        } else {
            val delta = s.sumDeltaKcal.roundToInt()
            val pct = if (s.sumTargetKcal > 0) (s.sumDeltaKcal / s.sumTargetKcal * 100) else 0.0
            val inBand = abs(pct) <= 1.0
            Text(
                buildAnnotatedString {
                    val parts = when {
                        inBand -> listOf(stringResource(R.string.s35_cal_even))
                        delta > 0 -> stringResource(R.string.s35_cal_over, "\u0000").split("\u0000")
                        else -> stringResource(R.string.s35_cal_under, "\u0000").split("\u0000")
                    }
                    append(parts[0])
                    if (parts.size > 1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(VnFormat.int(abs(delta))) }
                        append(parts[1])
                    }
                },
                style = MaterialTheme.typography.headlineSmall, color = cs.onSurface, modifier = Modifier.padding(top = Spacing.sm),
            )
            if (delta > 0 && !inBand) {
                val p = pct.roundToInt().coerceAtLeast(1)
                val light = pct <= 10.0
                Row(
                    Modifier.padding(top = Spacing.sm).clip(MaterialTheme.shapes.small).background(if (light) cc.warnContainer else cs.errorContainer)
                        .heightIn(min = 32.dp).padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val on = if (light) cc.onWarnContainer else cs.onErrorContainer
                    Icon(if (light) Icons.Rounded.Info else Icons.Rounded.Warning, null, tint = on, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (light) R.string.s35_cal_over_pct else R.string.s35_cal_over_much_pct, p), style = MaterialTheme.typography.labelLarge, color = on)
                }
            }
            Text(
                stringResource(
                    if (v.todayLogged && s.isPartial) R.string.s35_cal_sum_partial else R.string.s35_cal_sum,
                    VnFormat.int(s.sumEatenKcal.roundToInt()), VnFormat.int(s.sumTargetKcal.roundToInt()), VnFormat.int((s.avgKcal ?: 0.0).roundToInt()),
                ),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        DayBarChart(
            kcalBars(v), v.target?.targetKcal?.toDouble(),
            Modifier.padding(top = Spacing.md),
        )
        WeekdayAxis(short.toList(), highlight = if (v.includesToday && s.isPartial) v.today.dayOfWeek.value - 1 else null, dim = { s.days[it].status == DayStatus.FUTURE })
        StatusLegend(v)
        v.comparison?.takeIf { it.enough }?.let { c ->
            Text(
                stringResource(if (s.isPartial) R.string.s35_prev_on_same else R.string.s35_prev_on, c.previous.onTargetDays),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.xs),
            )
        }
        SuspectNotes(v)
        if (s.loggedDays > 0) {
            HorizontalDivider(Modifier.padding(top = Spacing.md), color = cs.outlineVariant)
            MaxMinRow(v)
            TdeeRow(v)
        }
    }
}

// ---------- Thẻ Nước kiểu S35 ----------

@Composable
fun WaterWeekCard(v: PeriodView) {
    val cs = MaterialTheme.colorScheme
    val s = v.stats
    val short = stringArrayResource(R.array.weekday_short)
    val goal = v.target?.targetWaterMl ?: 0
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s35_water), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            val prev = v.comparison?.takeIf { it.enough }?.previous
            if (prev != null) {
                val d = (s.avgWaterMl - prev.avgWaterMl).roundToInt()
                TrendTag(stringResource(R.string.gd6_legend_avg, VnFormat.signedInt(d) + " ml"), up = if (d == 0) null else d > 0)
            }
        }
        Text(
            VnFormat.decimal1(s.waterTotalMl / 1000.0) + " / " + VnFormat.decimal1(s.waterTargetTotalMl / 1000.0) + " L",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface, modifier = Modifier.padding(top = Spacing.sm),
        )
        Text(
            stringResource(
                R.string.s35_water_sum, VnFormat.decimal1(goal / 1000.0), s.waterMetDays, s.elapsedDays,
                VnFormat.decimal1(s.waterShortfallMl / 1000.0), VnFormat.decimal2(s.avgWaterMl / 1000.0),
            ),
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.xs),
        )
        val bars = s.days.map { d ->
            val ml = v.inputs[d.date]?.waterMl ?: 0
            val isToday = d.date == v.today && s.isPartial
            when {
                d.status == DayStatus.FUTURE || d.status == DayStatus.BEFORE_START -> DayBar(0.0, BarKind.EMPTY)
                ml == 0 && !isToday -> DayBar(0.0, BarKind.NONE)
                else -> DayBar(
                    ml.toDouble(),
                    when {
                        isToday -> BarKind.TODAY
                        d.waterMet -> BarKind.ON
                        else -> BarKind.UNDER
                    },
                    label = VnFormat.decimal1(ml / 1000.0),
                )
            }
        }
        DayBarChart(bars, goal.toDouble().takeIf { it > 0 }, Modifier.padding(top = Spacing.md), plotHeight = 96.dp, band = false, splitOver = false)
        WeekdayAxis(short.toList(), highlight = if (v.includesToday && s.isPartial) v.today.dayOfWeek.value - 1 else null, dim = { s.days[it].status == DayStatus.FUTURE })
    }
}

// ---------- Thẻ Macro (S32, S35) ----------

@Composable
fun MacroCard(v: PeriodView, title: String, showProteinDays: Boolean = true, trailing: (@Composable () -> Unit)? = null) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val s = v.stats
    val t = v.target
    StatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
            trailing?.invoke()
        }
        if (s.avgProteinG == null || t == null) {
            Text(stringResource(R.string.s35_empty), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm))
            return@StatCard
        }
        MacroLine(stringResource(R.string.macro_protein), cc.protein, s.avgProteinG, t.targetProteinG)
        if (showProteinDays) Text(
            stringResource(R.string.s35_protein_days, s.proteinMetDays, s.elapsedDays),
            style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp),
        )
        MacroLine(stringResource(R.string.macro_carb), cc.carb, s.avgCarbG ?: 0.0, t.targetCarbG)
        MacroLine(stringResource(R.string.macro_fat), cc.fat, s.avgFatG ?: 0.0, t.targetFatG)
        EnergySplit(
            stringResource(R.string.s35_actual), listOf(s.avgProteinG * 4, (s.avgCarbG ?: 0.0) * 4, (s.avgFatG ?: 0.0) * 9),
            Modifier.padding(top = Spacing.lg),
        )
        EnergySplit(
            stringResource(R.string.s35_target), listOf(t.targetProteinG * 4.0, t.targetCarbG * 4.0, t.targetFatG * 9.0),
            Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun MacroLine(name: String, color: Color, avg: Double, target: Int) {
    val cs = MaterialTheme.colorScheme
    val g = avg.roundToInt()
    val over = target in 1 until g
    val pct = if (target > 0) (avg / target * 100).roundToInt() else 0
    Column(Modifier.padding(top = Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    withStyle(MaterialTheme.typography.titleMedium.toSpanStyle().copy(color = if (over) cs.error else cs.onSurface)) { append(VnFormat.int(g)) }
                    withStyle(SpanStyle(color = cs.onSurfaceVariant)) { append(" / $target g · $pct%") }
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
        MacroTargetBar(if (target > 0) avg / target else 0.0, color, Modifier.padding(top = 4.dp))
        if (over) {
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, null, tint = cs.error, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.s35_macro_over, g - target), style = MaterialTheme.typography.labelMedium, color = cs.error)
            }
        }
    }
}

@Composable
private fun EnergySplit(label: String, kcal: List<Double>, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val total = kcal.sum().takeIf { it > 0 } ?: 1.0
    val pcts = kcal.map { (it / total * 100).roundToInt() }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.width(64.dp))
        EnergyBar(listOf(kcal[0] to cc.protein, kcal[1] to cc.carb, kcal[2] to cc.fat), Modifier.weight(1f))
        Text(pcts.joinToString(" · ") + " %", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = Spacing.md))
    }
}

/** Ký hiệu mũi tên trung tính cho bảng so sánh. */
fun arrow(t: Trend?): String = when (t) {
    Trend.UP -> "↑"
    Trend.DOWN -> "↓"
    else -> ""
}

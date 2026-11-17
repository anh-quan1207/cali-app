package com.cali.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import kotlin.math.max
import kotlin.math.roundToInt

// ---------- Cột theo ngày (S30, S32, S35, cùng ngữ pháp) ----------

enum class BarKind {
    /** Trong mục tiêu / đạt nước. */
    ON,

    /** Vượt: phần tới vạch mục tiêu giữ màu, phần dư tách khe 3dp màu error. */
    OVER,

    /** Thiếu: chàm nhạt, không bao giờ hổ phách. */
    UNDER,

    /** Hôm nay đang ghi: nhạt + viền. */
    TODAY,

    /** Đã qua nhưng chưa ghi: ô viền đứt ở chân. */
    NONE,

    /** Tương lai / trước khi dùng app: để trống. */
    EMPTY,
}

@Immutable
data class DayBar(
    val value: Double,
    val kind: BarKind,
    /** Chữ trên đầu cột (null = không ghi). */
    val label: String? = null,
    val suspect: Boolean = false,
    val contentDescription: String = "",
)

/** Bảng màu cột dùng chung. */
@Composable
fun barColors(): BarColors {
    val cs = MaterialTheme.colorScheme
    return BarColors(
        on = cs.primary,
        under = lerp(cs.surfaceContainerLow, cs.primary, 0.55f),
        today = lerp(CaliTheme.colors.ringTrack, cs.primary, 0.35f),
        over = cs.error,
    )
}

@Immutable
data class BarColors(val on: Color, val under: Color, val today: Color, val over: Color)

/**
 * Cột ngày với vạch mục tiêu (đứt) và dải ±10% tuỳ chọn, đường TB tuỳ chọn.
 * [target] = giá trị vạch; [splitOver] = tách phần vượt ở vạch (calo), false cho nước.
 * [onSelect] khác null thì cột chọn được (biểu đồ tháng, slider semantics).
 */
@Composable
fun DayBarChart(
    bars: List<DayBar>,
    target: Double?,
    modifier: Modifier = Modifier,
    plotHeight: Dp = 120.dp,
    band: Boolean = true,
    avg: Double? = null,
    splitOver: Boolean = true,
    barWidth: Dp = 28.dp,
    selected: Int? = null,
    onSelect: ((Int) -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val c = barColors()
    val maxV = bars.maxOfOrNull { it.value } ?: 0.0
    val t = target ?: 0.0
    val top = max(max(maxV * 1.08, t * 1.3), 1.0)
    val labelSpace = if (bars.any { it.label != null || it.suspect }) 30.dp else 6.dp
    BoxWithConstraints(modifier.fillMaxWidth().height(plotHeight + labelSpace)) {
        val colW = maxWidth / bars.size.coerceAtLeast(1)
        val bw = minOf(barWidth, colW - 4.dp).coerceAtLeast(3.dp)
        val ink = cs.onSurfaceVariant
        val bandColor = cs.onSurface.copy(alpha = 0.06f)
        val avgColor = cs.primary
        Canvas(Modifier.fillMaxSize()) {
            val plotTop = size.height - plotHeight.toPx()
            fun y(v: Double) = (size.height - v / top * plotHeight.toPx()).toFloat()
            if (t > 0) {
                if (band) drawRect(bandColor, Offset(0f, y(t * 1.1)), Size(size.width, y(t * 0.9) - y(t * 1.1)))
                drawLine(ink, Offset(0f, y(t)), Offset(size.width, y(t)), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
            }
            if (avg != null && avg > 0) drawLine(avgColor, Offset(0f, y(avg)), Offset(size.width, y(avg)), 1.dp.toPx())
            if (selected != null) {
                val x = colW.toPx() * selected
                drawRect(cs.onSurface.copy(alpha = 0.08f), Offset(x, plotTop - labelSpace.toPx()), Size(colW.toPx(), size.height - plotTop + labelSpace.toPx()))
            }
        }
        bars.forEachIndexed { i, b ->
            val h = plotHeight * (b.value / top).toFloat()
            val split = splitOver && b.kind == BarKind.OVER && t > 0
            val hTarget = plotHeight * (t / top).toFloat()
            var col = Modifier.offset(x = colW * i).width(colW).fillMaxHeight()
            if (onSelect != null) {
                col = col.clickable { onSelect(i) }.semantics(mergeDescendants = true) {
                    contentDescription = b.contentDescription
                    this.selected = selected == i
                }
            } else if (b.contentDescription.isNotEmpty()) {
                col = col.semantics(mergeDescendants = true) { contentDescription = b.contentDescription }
            }
            Box(col) {
                when (b.kind) {
                    BarKind.EMPTY -> Unit
                    BarKind.NONE -> Box(
                        Modifier.align(Alignment.BottomCenter).width(minOf(bw, 16.dp)).height(minOf(bw, 12.dp))
                            .dashedBorder(cs.outline),
                    )
                    else -> {
                        if (split) {
                            Box(Modifier.align(Alignment.BottomCenter).width(bw).height(hTarget).background(c.on))
                            Box(
                                Modifier.align(Alignment.BottomCenter).offset(y = -(hTarget + 3.dp)).width(bw)
                                    .height((h - hTarget - 3.dp).coerceAtLeast(2.dp)).background(c.over),
                            )
                        } else {
                            val fill = when (b.kind) {
                                BarKind.ON, BarKind.OVER -> c.on
                                BarKind.UNDER -> c.under
                                else -> c.today
                            }
                            var bar = Modifier.align(Alignment.BottomCenter).width(bw).height(h.coerceAtLeast(2.dp)).background(fill)
                            if (b.kind == BarKind.TODAY) bar = bar.border(1.5.dp, cs.primary)
                            Box(bar)
                        }
                    }
                }
                if (b.label != null || b.suspect) {
                    val labelTop = if (b.kind == BarKind.NONE) 12.dp else h
                    Column(
                        Modifier.align(Alignment.BottomCenter).offset(y = -(labelTop + 2.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (b.suspect) Icon(Icons.Rounded.Warning, null, tint = CaliTheme.colors.warn, modifier = Modifier.size(14.dp))
                        if (b.label != null) {
                            Text(
                                b.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (b.kind == BarKind.OVER) FontWeight.Bold else FontWeight.Medium),
                                color = when {
                                    b.kind == BarKind.OVER && splitOver -> cs.error
                                    b.suspect -> CaliTheme.colors.warn
                                    else -> cs.onSurfaceVariant
                                },
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Hàng nhãn dưới biểu đồ 7 ngày: T2 … CN, hôm nay đậm + chấm. */
@Composable
fun WeekdayAxis(labels: List<String>, highlight: Int?, dim: (Int) -> Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth()) {
        HorizontalDivider(color = cs.outlineVariant)
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, l ->
                val cur = i == highlight
                Column(Modifier.weight(1f).padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        l,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (cur) FontWeight.Bold else FontWeight.Medium),
                        color = when {
                            cur -> cs.onSurface
                            dim(i) -> cs.onSurface.copy(alpha = 0.38f)
                            else -> cs.onSurfaceVariant
                        },
                    )
                    if (cur) Box(Modifier.padding(top = 2.dp).size(4.dp).clip(CircleShape).background(cs.primary))
                }
            }
        }
    }
}

/** Chú giải một ô màu + chữ + số đậm. */
@Composable
fun LegendSwatch(fill: Color?, label: String, count: String? = null, stroke: Color? = null, dashed: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) {
            if (fill != null) drawRect(fill)
            if (stroke != null) {
                drawRect(
                    stroke, style = Stroke(1.2.dp.toPx(), pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())) else null),
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        if (count != null) {
            Text(" $count", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = cs.onSurface)
        }
    }
}

/** Chú giải vạch: đứt (mục tiêu) hoặc liền (TB). */
@Composable
fun LegendLine(label: String, dashed: Boolean, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(width = 14.dp, height = 10.dp)) {
            drawLine(
                color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.2.dp.toPx(),
                pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())) else null,
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Modifier.dashedBorder(color: Color) = drawBehind {
    drawRect(color, style = Stroke(1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()))))
}

// ---------- Điểm tuần: 4 làn 40/20/20/20 ----------

/** Làn điểm S35: bề rộng theo trọng số 40/20/20/20, phần đạt tô primary. */
@Composable
fun ScoreLanes(parts: List<Pair<Double, Int>>, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.forEach { (got, max) ->
            Box(Modifier.weight(max.toFloat()).fillMaxHeight().background(cs.primary.copy(alpha = 0.22f))) {
                Box(Modifier.fillMaxWidth((got / max).toFloat().coerceIn(0f, 1f)).fillMaxHeight().background(cs.primary))
            }
        }
    }
}

// ---------- Macro TB so với mục tiêu (S32, S35) ----------

/**
 * Thanh macro dài: vạch mục tiêu ở 80% bề rộng (100% mục tiêu), phần vượt tách khe 3dp màu error.
 */
@Composable
fun MacroTargetBar(ratio: Double, color: Color, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    BoxWithConstraints(modifier.fillMaxWidth().height(14.dp)) {
        val w = maxWidth
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(6.dp).background(cs.surfaceContainerHighest))
        if (ratio <= 1.0) {
            Box(Modifier.align(Alignment.CenterStart).width(w * (ratio * 0.8).toFloat().coerceIn(0f, 0.8f)).height(6.dp).background(color))
        } else {
            Box(Modifier.align(Alignment.CenterStart).width(w * 0.8f - 1.5.dp).height(6.dp).background(color))
            val ex = (minOf(ratio - 1, 0.25) * 0.8).toFloat()
            Box(Modifier.align(Alignment.CenterStart).offset(x = w * 0.8f + 1.5.dp).width((w * ex - 1.5.dp).coerceAtLeast(2.dp)).height(6.dp).background(cs.error))
        }
        Box(Modifier.align(Alignment.CenterStart).offset(x = w * 0.8f - 1.dp).width(2.dp).fillMaxHeight().background(cs.onSurface))
    }
}

// ---------- Đường cân (S31) ----------

@Immutable
data class WeightChartData(
    /** Lần cân (epoch day, kg). */
    val points: List<Pair<Long, Double>>,
    /** TB tuần (epoch day của ngày giữa tuần, kg). */
    val weekly: List<Pair<Long, Double>>,
    val fromDay: Long,
    val toDay: Long,
    val goalKg: Double?,
    /** Nhãn trục x: (epoch day, chữ). */
    val ticks: List<Pair<Long, String>>,
)

/** Biểu đồ đường cân: ô nhỏ = từng lần cân, đường + ô rỗng = TB tuần, ô cuối đặc; vạch mục tiêu chỉ khi nằm trong khung. */
@Composable
fun WeightLineChart(d: WeightChartData, modifier: Modifier = Modifier, height: Dp = 180.dp) {
    val cs = MaterialTheme.colorScheme
    val all = d.points.map { it.second } + d.weekly.map { it.second }
    if (all.isEmpty()) return
    var lo = kotlin.math.floor((all.min() - 0.3) * 2) / 2
    var hi = kotlin.math.ceil((all.max() + 0.3) * 2) / 2
    if (hi - lo < 1.0) { hi += 0.5; lo -= 0.5 }
    val goalIn = d.goalKg != null && d.goalKg in lo..hi
    val steps = ((hi - lo) / 0.5).roundToInt().coerceAtLeast(1)
    val stepKg = if (steps > 6) ((steps + 5) / 6) * 0.5 else 0.5
    val grid = cs.outlineVariant
    val ink = cs.onSurfaceVariant
    val line = cs.primary
    val dot = cs.onSurface
    val surface = cs.surfaceContainerLow
    val labelStyle = MaterialTheme.typography.labelSmall
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    Canvas(modifier.fillMaxWidth().height(height)) {
        val right = 40.dp.toPx()
        val bottom = 20.dp.toPx()
        val w = size.width - right
        val h = size.height - bottom
        val span = (d.toDay - d.fromDay).coerceAtLeast(1)
        fun x(day: Long) = ((day - d.fromDay).toFloat() / span) * (w - 12.dp.toPx()) + 6.dp.toPx()
        fun y(kg: Double) = (h - (kg - lo) / (hi - lo) * h).toFloat()
        var g = lo
        while (g <= hi + 1e-9) {
            val yy = y(g)
            drawLine(grid, Offset(0f, yy), Offset(w, yy), 1f)
            val tl = measurer.measure(com.cali.core.ui.format.VnFormat.decimal1(g), labelStyle.copy(color = ink))
            drawText(tl, topLeft = Offset(w + 6.dp.toPx(), yy - tl.size.height / 2f))
            g += stepKg
        }
        if (goalIn) {
            val yy = y(d.goalKg!!)
            drawLine(ink, Offset(0f, yy), Offset(w, yy), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.dp.toPx())))
        }
        d.ticks.forEach { (day, text) ->
            val tl = measurer.measure(text, labelStyle.copy(color = ink))
            drawText(tl, topLeft = Offset((x(day) - tl.size.width / 2f).coerceIn(0f, w - tl.size.width), h + 4.dp.toPx()))
        }
        val s = 5.dp.toPx()
        d.points.forEach { (day, kg) ->
            drawRect(dot, Offset(x(day) - s / 2, y(kg) - s / 2), Size(s, s))
        }
        if (d.weekly.size > 1) {
            val p = Path()
            d.weekly.forEachIndexed { i, (day, kg) -> if (i == 0) p.moveTo(x(day), y(kg)) else p.lineTo(x(day), y(kg)) }
            drawPath(p, line, style = Stroke(2.dp.toPx()))
        }
        val bs = 9.dp.toPx()
        d.weekly.forEachIndexed { i, (day, kg) ->
            val o = Offset(x(day) - bs / 2, y(kg) - bs / 2)
            if (i == d.weekly.lastIndex) {
                drawRect(line, o, Size(bs, bs))
            } else {
                drawRect(surface, o, Size(bs, bs))
                drawRect(line, o, Size(bs, bs), style = Stroke(1.5.dp.toPx()))
            }
        }
    }
}

/** Phần mục tiêu có nằm trong khung vẽ không (cho chú giải "dưới khung"). */
fun weightGoalInFrame(d: WeightChartData): Boolean {
    val all = d.points.map { it.second } + d.weekly.map { it.second }
    if (all.isEmpty() || d.goalKg == null) return false
    var lo = kotlin.math.floor((all.min() - 0.3) * 2) / 2
    var hi = kotlin.math.ceil((all.max() + 0.3) * 2) / 2
    if (hi - lo < 1.0) { hi += 0.5; lo -= 0.5 }
    return d.goalKg in lo..hi
}

/** Khoảng đệm thẻ dùng chung GĐ6. */
@Composable
fun StatCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = Spacing.lg, end = Spacing.lg, top = 14.dp, bottom = Spacing.lg),
    ) { content() }
}

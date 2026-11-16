package com.cali.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cali.core.ui.theme.CaliTheme
import kotlin.math.max

/** Một đoạn vòng: các món liên tiếp cùng bữa gộp lại (DESIGN.md Vòng calo). */
data class RingSegment(val kcal: Double, val color: Color)

/**
 * Vòng calo bản tĩnh (GĐ1). 220dp, bán kính 98dp, nét 14dp, đầu vuông, khe 3dp, cung tối thiểu 4dp.
 * Vượt mục tiêu: phần dư chạy vòng trong nét 6dp đầu tròn, trên rãnh cùng màu 18%.
 * Chuyển động vẽ lần lượt để GĐ3.
 */
@Composable
fun CalorieRing(
    segments: List<RingSegment>,
    targetKcal: Double,
    overColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    center: @Composable BoxScope.() -> Unit = {},
) {
    val track = CaliTheme.colors.ringTrack
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val radius = RADIUS.toPx()
            val stroke = STROKE.toPx()
            val c = this.center
            drawArcAt(c, radius, track, 0f, 360f, stroke, StrokeCap.Butt)

            val total = segments.sumOf { it.kcal }
            if (total > 0 && targetKcal > 0) {
                // Không vượt: toàn vòng = mục tiêu. Vượt: vòng ngoài đầy, chia theo tổng đã ăn.
                val scale = max(total, targetKcal)
                val gapDeg = degFor(GAP.toPx(), radius)
                val minDeg = degFor(MIN_ARC.toPx(), radius)
                var start = -90f
                segments.forEachIndexed { i, s ->
                    val sweepFull = (s.kcal / scale * 360.0).toFloat()
                    val gap = if (i < segments.lastIndex || total >= targetKcal) gapDeg else 0f
                    val sweep = max(minDeg, sweepFull - gap)
                    drawArcAt(c, radius, s.color, start, sweep, stroke, StrokeCap.Butt)
                    start += sweepFull
                }

                if (total > targetKcal) {
                    val innerR = radius - stroke / 2 - INNER_OFFSET.toPx() - INNER_STROKE.toPx() / 2
                    val innerStroke = INNER_STROKE.toPx()
                    drawArcAt(c, innerR, overColor.copy(alpha = 0.18f), 0f, 360f, innerStroke, StrokeCap.Butt)
                    val excess = ((total - targetKcal) / targetKcal * 360.0).toFloat().coerceAtMost(360f)
                    drawArcAt(c, innerR, overColor, -90f, max(degFor(MIN_ARC.toPx(), innerR), excess), innerStroke, StrokeCap.Round)
                }
            }
        }
        center()
    }
}

private fun degFor(arcLength: Float, radius: Float): Float = (arcLength / radius * 180f / Math.PI).toFloat()

private fun DrawScope.drawArcAt(c: Offset, r: Float, color: Color, start: Float, sweep: Float, stroke: Float, cap: StrokeCap) =
    drawArc(
        color = color,
        startAngle = start,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(c.x - r, c.y - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = stroke, cap = cap),
    )

private val RADIUS = 98.dp
private val STROKE = 14.dp
private val GAP = 3.dp
private val MIN_ARC = 4.dp
private val INNER_STROKE = 6.dp
private val INNER_OFFSET = 4.dp

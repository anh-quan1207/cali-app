package com.cali.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
 * Vòng calo (DESIGN.md, mockup s10). 220dp, bán kính 98dp, nét 14dp, đầu vuông, khe 3dp sau mỗi đoạn, cung tối thiểu 4dp.
 * Toàn vòng = mục tiêu; các đoạn theo giờ ăn bị cắt tại mục tiêu. Vượt: phần dư chạy vòng trong
 * (bán kính 79dp, nét 6dp đầu tròn) trên rãnh cùng màu 18%. Các đoạn vẽ lần lượt theo giờ ăn khi mở.
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
    val reduceMotion = rememberReducedMotion()
    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(Unit) { if (!reduceMotion) progress.animateTo(1f, tween(700, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))) }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val radius = RADIUS.toPx()
            val stroke = STROKE.toPx()
            val c = this.center
            drawArcAt(c, radius, track, 0f, 360f, stroke, StrokeCap.Butt)
            if (targetKcal <= 0) return@Canvas

            val circ = (2 * Math.PI * radius).toFloat()
            val gap = GAP.toPx()
            val minArc = MIN_ARC.toPx()
            val drawUpTo = progress.value * circ
            var pos = 0f
            var acc = 0.0
            for (s in segments) {
                val k = (targetKcal - acc).coerceAtMost(s.kcal).coerceAtLeast(0.0)
                acc += s.kcal
                if (k <= 0) continue
                val len = (k / targetKcal * circ).toFloat()
                val dash = max(minArc, len - gap).coerceAtMost(max(0f, drawUpTo - pos))
                if (dash > 0f) drawArcAt(c, radius, s.color, -90f + pos / circ * 360f, dash / circ * 360f, stroke, StrokeCap.Butt)
                pos += len
            }

            val total = segments.sumOf { it.kcal }
            if (total > targetKcal) {
                val r2 = radius - stroke - INNER_OFFSET.toPx()
                val innerStroke = INNER_STROKE.toPx()
                val circ2 = (2 * Math.PI * r2).toFloat()
                drawArcAt(c, r2, overColor.copy(alpha = 0.18f), 0f, 360f, innerStroke, StrokeCap.Butt)
                val len2 = (minOf(total - targetKcal, targetKcal) / targetKcal * circ2).toFloat()
                val dash2 = max(0.01f, len2 - innerStroke) * progress.value
                // đầu tròn: lùi nửa nét ở mỗi đầu để chiều dài thấy được đúng tỉ lệ
                drawArcAt(c, r2, overColor, -90f + (innerStroke / 2) / circ2 * 360f, dash2 / circ2 * 360f, innerStroke, StrokeCap.Round)
            }
        }
        center()
    }
}

/** Tôn trọng "Xoá hiệu ứng" của hệ thống (animator duration scale = 0). */
@Composable
fun rememberReducedMotion(): Boolean {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return remember {
        android.provider.Settings.Global.getFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
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
private val INNER_OFFSET = 5.dp

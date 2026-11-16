package com.cali.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing

/** Thang BMI châu Á 15–35, cao 8dp, năm đoạn đầu vuông khe 3dp, kim 4×18dp (DESIGN.md Shapes). */
@Composable
fun BmiScale(bmi: Double, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val bounds = listOf(15.0, 18.5, 23.0, 25.0, 30.0, 35.0)
    val current = bounds.zipWithNext().indexOfFirst { (a, b) -> bmi >= a && bmi < b }.let { if (it < 0) (if (bmi < 15) 0 else 4) else it }
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(23.dp)) {
            val gap = 3.dp.toPx()
            val barH = 8.dp.toPx()
            val top = 5.dp.toPx()
            val span = bounds.last() - bounds.first()
            bounds.zipWithNext().forEachIndexed { i, (a, b) ->
                val x0 = ((a - bounds.first()) / span * size.width).toFloat() + if (i > 0) gap / 2 else 0f
                val x1 = ((b - bounds.first()) / span * size.width).toFloat() - if (i < 4) gap / 2 else 0f
                drawRect(cs.surfaceContainerHighest, Offset(x0, top), Size(x1 - x0, barH))
                if (i == current) {
                    drawRect(cs.secondaryContainer, Offset(x0, top), Size(x1 - x0, barH))
                    val s = 1.5.dp.toPx()
                    drawRect(cs.primary, Offset(x0 + s / 2, top + s / 2), Size(x1 - x0 - s, barH - s), style = Stroke(s))
                }
            }
            val x = ((bmi.coerceIn(15.0, 35.0) - 15.0) / span * size.width).toFloat()
            val w = 4.dp.toPx()
            drawRoundRect(cs.onSurface, Offset(x - w / 2, 0f), Size(w, 18.dp.toPx()), CornerRadius(2.dp.toPx()))
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(18.dp)) {
            listOf(18.5 to "18,5", 23.0 to "23", 25.0 to "25", 30.0 to "30").forEach { (v, label) ->
                val x = maxWidth * ((v - 15.0) / 20.0).toFloat()
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.offset(x = x - 18.dp).width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

/** Thanh chồng tỉ lệ năng lượng đạm / tinh bột / béo, cao 10dp, đầu vuông, khe 3dp. */
@Composable
fun EnergyBar(parts: List<Pair<Double, Color>>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(10.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        parts.filter { it.first > 0 }.forEach { (kcal, color) ->
            Box(Modifier.weight(kcal.toFloat()).height(10.dp).background(color))
        }
    }
}

/** Chú giải macro: chấm 8dp + tên, "122 g · 28%". */
@Composable
fun MacroLegend(name: String, color: Color, grams: Int, percent: Int, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            buildAnnotatedString {
                withStyle(MaterialTheme.typography.titleMedium.toSpanStyle()) { append(VnFormat.int(grams)) }
                withStyle(SpanStyle(color = cs.onSurfaceVariant, fontSize = MaterialTheme.typography.labelMedium.fontSize)) { append(" g · $percent%") }
            },
            color = cs.onSurface,
        )
    }
}

/** Lưới chỉ số 4 cột (S02): nhãn / giá trị / đơn vị, ngăn bằng đường 1dp outline-var. */
@Composable
fun MetricGrid(items: List<Triple<String, String, String>>, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, (label, value, unit) ->
            if (i > 0) Box(Modifier.width(1.dp).height(48.dp).background(cs.outlineVariant))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(unit, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}


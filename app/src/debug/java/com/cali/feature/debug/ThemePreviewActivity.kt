package com.cali.feature.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.ui.component.CalorieRing
import com.cali.core.ui.component.RingSegment
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing

/** Màn tạm của GĐ1: bảng màu, kiểu chữ, vòng calo tĩnh với bộ số mẫu (sáng 820 + trưa 490 / 1.740). */
class ThemePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { CaliTheme { ThemePreviewScreen() } }
    }
}

private const val TARGET = 1740.0
private const val BREAKFAST = 820.0
private const val LUNCH = 490.0

@Composable
fun ThemePreviewScreen() {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val eaten = BREAKFAST + LUNCH
    Surface(Modifier.fillMaxSize(), color = cs.surface) {
        Column(
            Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.gutter, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.preview_title), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
            Spacer(Modifier.height(Spacing.xl))

            CalorieRing(
                segments = listOf(RingSegment(BREAKFAST, cc.mealSang), RingSegment(LUNCH, cc.mealTrua)),
                targetKcal = TARGET,
                overColor = cs.error,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.preview_remaining), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                    Text(VnFormat.int(TARGET - eaten), style = MaterialTheme.typography.displayLarge, color = cs.onSurface)
                    Text(stringResource(R.string.unit_kcal), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xl)) {
                Stat(stringResource(R.string.preview_eaten), VnFormat.int(eaten))
                Stat(stringResource(R.string.preview_target), VnFormat.int(TARGET))
            }
            Spacer(Modifier.height(Spacing.xl))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                MacroBar(stringResource(R.string.macro_protein), 78, 122, cc.protein, Modifier.weight(1f))
                MacroBar(stringResource(R.string.macro_carb), 160, 196, cc.carb, Modifier.weight(1f))
                MacroBar(stringResource(R.string.macro_fat), 41, 52, cc.fat, Modifier.weight(1f))
            }

            Spacer(Modifier.height(Spacing.xl))
            SectionTitle(stringResource(R.string.preview_section_colors))
            listOf(
                "primary" to cs.primary, "on-primary" to cs.onPrimary,
                "primary-container" to cs.primaryContainer, "on-primary-container" to cs.onPrimaryContainer,
                "secondary-container" to cs.secondaryContainer, "on-secondary-container" to cs.onSecondaryContainer,
                "surface" to cs.surface, "surface-low" to cs.surfaceContainerLow,
                "surface-cont" to cs.surfaceContainer, "surface-highest" to cs.surfaceContainerHighest,
                "on-surface" to cs.onSurface, "on-surface-var" to cs.onSurfaceVariant,
                "outline" to cs.outline, "outline-var" to cs.outlineVariant,
                "error" to cs.error, "error-container" to cs.errorContainer,
                "warn" to cc.warn, "warn-container" to cc.warnContainer,
                "protein" to cc.protein, "carb" to cc.carb, "fat" to cc.fat,
                "meal-sang" to cc.mealSang, "meal-trua" to cc.mealTrua,
                "meal-toi" to cc.mealToi, "meal-phu" to cc.mealPhu,
            ).chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    pair.forEach { (name, color) -> Swatch(name, color, Modifier.weight(1f)) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(Spacing.sm))
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionTitle(stringResource(R.string.preview_section_type))
            val t = MaterialTheme.typography
            listOf<Pair<String, TextStyle>>(
                "displayLarge" to t.displayLarge, "displayMedium" to t.displayMedium, "displaySmall" to t.displaySmall,
                "headlineMedium" to t.headlineMedium, "titleLarge" to t.titleLarge, "titleMedium" to t.titleMedium,
                "bodyLarge" to t.bodyLarge, "bodyMedium" to t.bodyMedium, "bodySmall" to t.bodySmall,
                "labelLarge" to t.labelLarge, "labelMedium" to t.labelMedium, "labelSmall" to t.labelSmall,
            ).forEach { (name, style) ->
                Text(name, style = t.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
                Text(
                    text = if (style.fontSize.value >= 36) "1.740" else stringResource(R.string.preview_type_sample),
                    style = style,
                    color = cs.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MacroBar(name: String, value: Int, target: Int, color: Color, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(Spacing.xs + 2.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(VnFormat.int(value), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(" / ${VnFormat.int(target)} g", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        Spacer(Modifier.height(Spacing.xs))
        Box(Modifier.fillMaxWidth().height(6.dp).background(cs.surfaceContainerHighest)) {
            Box(Modifier.fillMaxWidth((value.toFloat() / target).coerceIn(0f, 1f)).height(6.dp).background(color))
        }
        Spacer(Modifier.height(Spacing.xs))
        val left = target - value
        Text(
            text = if (left >= 0) stringResource(R.string.macro_left, VnFormat.int(left)) else stringResource(R.string.macro_over, VnFormat.int(-left)),
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm))
}

@Composable
private fun Swatch(name: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(MaterialTheme.shapes.small).background(color))
        Spacer(Modifier.width(Spacing.sm))
        Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Start)
    }
}

@Preview(name = "Sáng", widthDp = 360, heightDp = 1600)
@Composable
private fun PreviewLight() = CaliTheme(darkTheme = false) { ThemePreviewScreen() }

@Preview(name = "Tối", widthDp = 360, heightDp = 1600)
@Composable
private fun PreviewDark() = CaliTheme(darkTheme = true) { ThemePreviewScreen() }

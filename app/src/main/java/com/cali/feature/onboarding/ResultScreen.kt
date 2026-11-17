package com.cali.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalType
import com.cali.core.data.repo.GoalPreview
import com.cali.core.ui.component.AmberBox
import com.cali.core.ui.component.CalorieRing
import com.cali.core.ui.component.EnergyBar
import com.cali.core.ui.component.MacroLegend
import com.cali.core.ui.component.MetricGrid
import com.cali.core.ui.component.PrimaryButton
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import kotlin.math.abs
import kotlin.math.roundToInt

/** S02 — số kcal/ngày trong vòng calo trống của S10. */
@Composable
fun ResultScreen(
    s: OnboardingUiState,
    preview: GoalPreview?,
    onBack: () -> Unit,
    onToggleHow: () -> Unit,
    onStart: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(Modifier.fillMaxWidth().height(64.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))
            }
        }
        if (preview == null) return@Column
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ResultBody(s, preview, onToggleHow)
        }
        Text(
            stringResource(R.string.s02_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = Spacing.sm),
        )
        PrimaryButton(
            text = stringResource(R.string.s02_start),
            onClick = onStart,
            enabled = !s.saving,
            modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.lg),
        )
    }
}

@Composable
private fun ResultBody(s: OnboardingUiState, p: GoalPreview, onToggleHow: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val r = p.result
    val target = r.target.targetKcal

    Text(stringResource(R.string.s02_title), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
    Spacer(Modifier.height(Spacing.lg))
    CalorieRing(segments = emptyList(), targetKcal = target.toDouble(), overColor = cs.error) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(VnFormat.int(target), style = MaterialTheme.typography.displayLarge, color = cs.onSurface)
            Text(stringResource(R.string.s02_kcal_per_day), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
        }
    }
    Spacer(Modifier.height(Spacing.lg))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Flag, null, tint = cs.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.sm))
        val etaText = if (p.profile.goalType == GoalType.MAINTAIN || p.etaDate == null || p.weeks == null) {
            stringResource(R.string.s02_eta_maintain, VnFormat.kg(p.profile.weightKg))
        } else {
            stringResource(R.string.s02_eta, VnFormat.kg(p.profile.targetWeightKg!!), VnFormat.date(p.etaDate), p.weeks)
        }
        Text(etaText, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
    }

    r.target.clampReason?.let { reason ->
        Spacer(Modifier.height(Spacing.md))
        val res = if (reason == ClampReason.MAX_SURPLUS) R.string.s02_clamped_surplus else R.string.s02_clamped
        AmberBox(stringResource(res, VnFormat.rate(p.profile.weeklyRateKg), VnFormat.int(target), VnFormat.decimal2(r.target.actualWeeklyRateKg)))
    }

    Spacer(Modifier.height(Spacing.lg))
    val m = r.macros
    val pk = m.proteinG * 4.0
    val fk = m.fatG * 9.0
    val ck = m.carbG * 4.0
    EnergyBar(listOf(pk to cc.protein, ck to cc.carb, fk to cc.fat))
    Spacer(Modifier.height(Spacing.sm))
    Row(Modifier.fillMaxWidth()) {
        fun pct(k: Double) = (k / target * 100).roundToInt()
        MacroLegend(stringResource(R.string.macro_protein), cc.protein, m.proteinG, pct(pk), Modifier.weight(1f))
        MacroLegend(stringResource(R.string.macro_carb), cc.carb, m.carbG, pct(ck), Modifier.weight(1f))
        MacroLegend(stringResource(R.string.macro_fat), cc.fat, m.fatG, pct(fk), Modifier.weight(1f))
    }

    Spacer(Modifier.height(Spacing.lg))
    MetricGrid(
        listOf(
            Triple(stringResource(R.string.metric_bmi), VnFormat.decimal1(r.bmi), stringResource(r.bmiClass.label())),
            Triple(stringResource(R.string.metric_bmr), VnFormat.int(r.bmr.roundToInt()), stringResource(R.string.unit_kcal)),
            Triple(stringResource(R.string.metric_tdee), VnFormat.int(r.tdee.roundToInt()), stringResource(R.string.unit_kcal)),
            // Nước: mặc định 1.500 ml (G5-8), không theo công thức; đổi ở Cài đặt → Nước.
            Triple(stringResource(R.string.metric_water), VnFormat.decimal1(com.cali.core.data.prefs.Settings.DEFAULT_WATER_GOAL_ML / 1000.0), stringResource(R.string.unit_l_day)),
        ),
    )

    Spacer(Modifier.height(Spacing.md))
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onToggleHow),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.s02_how), style = MaterialTheme.typography.titleMedium, color = cs.primary, modifier = Modifier.weight(1f))
        Icon(if (s.howExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = cs.primary)
    }
    AnimatedVisibility(s.howExpanded) {
        val ctx = LocalResources.current
        val delta = abs(r.tdee - r.target.rawTarget).roundToInt()
        val lines = buildList {
            add(ctx.getString(R.string.s02_how_bmr, VnFormat.int(r.bmr.roundToInt())))
            add(ctx.getString(R.string.s02_how_tdee, VnFormat.int(r.tdee.roundToInt()), VnFormat.factor(p.profile.activityLevel.factor), ctx.getString(p.profile.activityLevel.label()).lowercase()))
            add(
                when (p.profile.goalType) {
                    GoalType.LOSE -> ctx.getString(R.string.s02_how_target_lose, VnFormat.int(target), VnFormat.int(delta), VnFormat.rate(p.profile.weeklyRateKg))
                    GoalType.GAIN -> ctx.getString(R.string.s02_how_target_gain, VnFormat.int(target), VnFormat.int(delta), VnFormat.rate(p.profile.weeklyRateKg))
                    GoalType.MAINTAIN -> ctx.getString(R.string.s02_how_target_maintain, VnFormat.int(target))
                },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            lines.forEach { Text(AnnotatedString.fromHtml(it), style = MaterialTheme.typography.bodySmall, color = cs.onSurface) }
        }
    }
    if (s.saveFailed) {
        Spacer(Modifier.height(Spacing.sm))
        Text(stringResource(R.string.error_save), style = MaterialTheme.typography.bodySmall, color = cs.error)
    }
    Spacer(Modifier.height(Spacing.lg))
}

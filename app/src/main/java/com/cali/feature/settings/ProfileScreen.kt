package com.cali.feature.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalType

import com.cali.core.ui.component.AmberBox
import com.cali.core.ui.component.EnergyBar
import com.cali.core.ui.component.MacroLegend
import com.cali.core.ui.component.NumberField
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import kotlin.math.roundToInt

/** S41 — Hồ sơ & mục tiêu. */
@Composable
fun ProfileRoute(onBack: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val d by vm.data.collectAsStateWithLifecycle()
    val sheet by vm.sheet.collectAsStateWithLifecycle()
    val pending by vm.pending.collectAsStateWithLifecycle()
    val draft by vm.draft.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    Surface(Modifier.fillMaxSize(), color = cs.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s41_title), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
            }
            val data = d ?: return@Column
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg)) {
                CurrentGoalCard(data)
                Spacer(Modifier.height(Spacing.xl))
                ProfileGroup(data, vm)
                Spacer(Modifier.height(Spacing.xl))
                GoalGroup(data, vm)
                Spacer(Modifier.height(Spacing.xl))
                OverrideGroup(data, draft, vm)
                Spacer(Modifier.height(Spacing.xl))
            }
            sheet?.let { EditSheet(it, data, vm) }
            pending?.let { ApplySheet(it, data, onCancel = vm::cancelPending, onApply = vm::applyPending) }
        }
    }
}

@Composable
private fun CurrentGoalCard(d: ProfileData) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val g = d.goal
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.lg)) {
        Text(stringResource(R.string.s41_current_since, VnFormat.dayMonth(g.effectiveFrom)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(VnFormat.int(g.targetKcal), style = MaterialTheme.typography.displaySmall, color = cs.onSurface)
            Spacer(Modifier.width(Spacing.xs))
            Text(stringResource(R.string.unit_kcal_day), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
        }
        Spacer(Modifier.height(Spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Dot(cc.protein, stringResource(R.string.macro_protein), g.proteinG)
            Dot(cc.carb, stringResource(R.string.macro_carb), g.carbG)
            Dot(cc.fat, stringResource(R.string.macro_fat), g.fatG)
        }
        Spacer(Modifier.height(Spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WaterDrop, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(Spacing.xs))
            Text(stringResource(R.string.s41_water, VnFormat.decimal1((d.settings.waterGoalOverrideMl ?: g.waterMl) / 1000.0)), style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
        }
        HorizontalDivider(Modifier.padding(vertical = Spacing.md), color = cs.outlineVariant)
        val p = d.profile
        val rate = if (g.clampReason != null || g.isKcalOverridden) kotlin.math.abs(g.tdee - g.targetKcal) * 7 / 7700 else p.weeklyRateKg
        val weeks = com.cali.core.calc.GoalPlanner.weeksRounded(d.weight.weightKg, p.targetWeightKg, rate)
        val eta = com.cali.core.calc.NutritionCalculator.etaDate(d.today, d.weight.weightKg, p.targetWeightKg, rate)
        if (p.goalType != GoalType.MAINTAIN && weeks != null && eta != null) {
            InfoLine(Icons.Outlined.Flag, stringResource(R.string.s41_eta, VnFormat.kg(p.targetWeightKg!!), VnFormat.date(eta), weeks))
            Spacer(Modifier.height(Spacing.sm))
        }
        InfoLine(
            Icons.Outlined.Info,
            stringResource(R.string.s41_based_on, VnFormat.kg(g.weightKgUsed), VnFormat.dayMonth(g.effectiveFrom), VnFormat.kg(d.weight.weightKg)),
        )
    }
}

@Composable
private fun Dot(color: Color, name: String, grams: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
        Spacer(Modifier.width(Spacing.xs))
        Text(
            buildAnnotatedString {
                append("$name ")
                withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleMedium.fontWeight)) { append(VnFormat.int(grams)) }
                append(" g")
            },
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun GroupTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.md))
}

@Composable
fun GroupBlock(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)) { content() }
}

@Composable
fun RowDivider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

/** Hàng cài đặt: nhãn ← giá trị tabular (+ đơn vị), chevron nếu chạm được. */
@Composable
private fun ValueRow(label: String, value: String, unit: String? = null, sub: String? = null, onClick: (() -> Unit)?) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.md, bottom = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
        if (unit != null) {
            Spacer(Modifier.width(3.dp))
            Text(unit, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant, modifier = Modifier.padding(start = Spacing.xs))
        else Spacer(Modifier.width(Spacing.sm))
    }
}

@Composable
private fun SwitchRow(label: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .toggleable(value = checked, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = onChange)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ProfileGroup(d: ProfileData, vm: ProfileViewModel) {
    val p = d.profile
    GroupTitle(stringResource(R.string.s41_group_profile))
    GroupBlock {
        ValueRow(stringResource(R.string.s41_sex), stringResource(p.sex.label()), onClick = { vm.open(ProfileSheet.SEX) })
        RowDivider()
        ValueRow(stringResource(R.string.s41_birth_year), p.birthYear.toString(), stringResource(R.string.s41_age_suffix, d.today.year - p.birthYear), onClick = { vm.open(ProfileSheet.BIRTH_YEAR) })
        RowDivider()
        ValueRow(stringResource(R.string.s41_height), VnFormat.kg(p.heightCm), stringResource(R.string.unit_cm), onClick = { vm.open(ProfileSheet.HEIGHT) })
        RowDivider()
        ValueRow(
            stringResource(R.string.s41_weight), VnFormat.kg(d.weight.weightKg), stringResource(R.string.unit_kg),
            sub = stringResource(R.string.s41_weight_sub, VnFormat.dayMonth(d.weight.date)), onClick = null,
        )
        RowDivider()
        ValueRow(
            stringResource(R.string.s41_activity), "×" + VnFormat.factor(p.activityLevel.factor),
            sub = stringResource(p.activityLevel.label()), onClick = { vm.open(ProfileSheet.ACTIVITY) },
        )
    }
}

@Composable
private fun GoalGroup(d: ProfileData, vm: ProfileViewModel) {
    val p = d.profile
    GroupTitle(stringResource(R.string.s41_group_goal))
    GroupBlock {
        ValueRow(stringResource(R.string.s41_goal), stringResource(p.goalType.label()), onClick = { vm.open(ProfileSheet.GOAL) })
        if (p.goalType != GoalType.MAINTAIN) {
            RowDivider()
            ValueRow(stringResource(R.string.s41_target_weight), VnFormat.kg(p.targetWeightKg ?: 0.0), stringResource(R.string.unit_kg), onClick = { vm.open(ProfileSheet.GOAL) })
            RowDivider()
            val label = com.cali.core.calc.GoalPlanner.rates(p.goalType).firstOrNull { it.first == p.weeklyRateKg }?.second
            val delta = VnFormat.int((p.weeklyRateKg * 7700 / 7).roundToInt())
            ValueRow(
                stringResource(R.string.s41_rate), VnFormat.rate(p.weeklyRateKg), stringResource(R.string.unit_kg_week),
                sub = label?.let {
                    stringResource(if (p.goalType == GoalType.LOSE) R.string.s41_rate_sub else R.string.s41_rate_sub_gain, stringResource(it.label()), delta)
                },
                onClick = { vm.open(ProfileSheet.RATE) },
            )
        }
        RowDivider()
        SwitchRow(
            stringResource(R.string.s41_auto_recalc),
            stringResource(if (p.autoRecalcOnWeighIn) R.string.s41_auto_recalc_on else R.string.s41_auto_recalc_off),
            p.autoRecalcOnWeighIn, vm::setAutoRecalc,
        )
    }
}

@Composable
private fun OverrideGroup(d: ProfileData, dr: OverrideDraft, vm: ProfileViewModel) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    GroupTitle(stringResource(R.string.s41_group_override))
    GroupBlock {
        SwitchRow(stringResource(R.string.s41_override_kcal), stringResource(R.string.s41_override_kcal_sub), dr.kcalOn, vm::setKcalOn)
        if (dr.kcalOn) {
            Column(Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.md)) {
                NumberField(
                    value = dr.kcalText, onValueChange = vm::setKcalText,
                    label = stringResource(R.string.field_target_kcal), unit = stringResource(R.string.unit_kcal_day),
                    error = if (dr.kcalError) stringResource(R.string.error_kcal_range) else null,
                    supporting = stringResource(R.string.s41_kcal_range), decimal = false,
                    imeAction = ImeAction.Done, onDone = vm::proposeOverride,
                    modifier = Modifier.onBlur(vm::proposeOverride),
                )
                val ov = vm.overridePreview()
                val t = ov?.result?.target
                if (ov != null && t?.clampReason != null) {
                    Spacer(Modifier.height(Spacing.sm))
                    val text = when (t.clampReason) {
                        ClampReason.MAX_DEFICIT -> stringResource(
                            R.string.s41_override_clamped, VnFormat.int(dr.kcal!!), VnFormat.int((ov.result.tdee - dr.kcal!!).roundToInt()),
                            VnFormat.int((ov.result.tdee * 0.25).roundToInt()), VnFormat.int(t.targetKcal), VnFormat.decimal2(t.actualWeeklyRateKg),
                        )
                        ClampReason.FLOOR -> stringResource(R.string.s41_override_floor, VnFormat.int(dr.kcal!!), VnFormat.int(t.targetKcal))
                        ClampReason.MAX_SURPLUS -> stringResource(R.string.s41_override_surplus, VnFormat.int(dr.kcal!!), VnFormat.int(t.targetKcal))
                    }
                    AmberBox(text)
                }
            }
        }
        RowDivider()
        SwitchRow(stringResource(R.string.s41_override_macro), stringResource(R.string.s41_override_macro_sub), dr.macroOn, vm::setMacroOn)
        if (dr.macroOn) {
            Column(Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.lg)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    PctField(stringResource(R.string.macro_protein), dr.p, vm::setP, vm::proposeOverride, Modifier.weight(1f))
                    PctField(stringResource(R.string.macro_carb), dr.c, vm::setC, vm::proposeOverride, Modifier.weight(1f))
                    PctField(stringResource(R.string.macro_fat), dr.f, vm::setF, vm::proposeOverride, Modifier.weight(1f))
                }
                Spacer(Modifier.height(Spacing.sm))
                val kcalBase = vm.overridePreview()?.result?.target?.targetKcal ?: d.goal.targetKcal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dr.macroValid) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.s41_macro_total_ok, dr.macroSum, VnFormat.int(kcalBase)), style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
                    } else {
                        Text(stringResource(R.string.s41_macro_total_bad, dr.macroSum), style = MaterialTheme.typography.labelMedium, color = cs.error)
                    }
                }
                if (dr.macroValid) {
                    val (p, c, f) = dr.pct
                    Spacer(Modifier.height(Spacing.md))
                    EnergyBar(listOf(p!!.toDouble() to cc.protein, c!!.toDouble() to cc.carb, f!!.toDouble() to cc.fat))
                    Spacer(Modifier.height(Spacing.sm))
                    Row {
                        MacroLegend(stringResource(R.string.macro_protein), cc.protein, (kcalBase * p / 100.0 / 4).roundToInt(), p, Modifier.weight(1f))
                        MacroLegend(stringResource(R.string.macro_carb), cc.carb, (kcalBase * c / 100.0 / 4).roundToInt(), c, Modifier.weight(1f))
                        MacroLegend(stringResource(R.string.macro_fat), cc.fat, (kcalBase * f / 100.0 / 9).roundToInt(), f, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PctField(label: String, value: String, onChange: (String) -> Unit, onCommit: () -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        suffix = { Text("%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        textStyle = MaterialTheme.typography.titleLarge,
        singleLine = true,
        shape = MaterialTheme.shapes.extraSmall,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onCommit() }),
        modifier = modifier.onBlur(onCommit),
    )
}

/** Chạy [action] khi ô mất focus sau khi đã được focus (không chạy lúc vừa vẽ). */
fun Modifier.onBlur(action: () -> Unit): Modifier = composed {
    var wasFocused by remember { mutableStateOf(false) }
    onFocusChanged {
        if (it.isFocused) wasFocused = true
        else if (wasFocused) {
            wasFocused = false
            action()
        }
    }
}

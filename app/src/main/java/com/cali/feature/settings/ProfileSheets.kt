package com.cali.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.GoalPlanner
import com.cali.core.calc.InputLimits
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.ClampReason
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.Sex
import com.cali.core.data.repo.ProfileInput
import com.cali.core.data.repo.toInput
import com.cali.core.ui.component.AmberBox
import com.cali.core.ui.component.IconChoiceRow
import com.cali.core.ui.component.NumberField
import com.cali.core.ui.component.RateRow
import com.cali.core.ui.component.YearWheel
import com.cali.core.ui.component.description
import com.cali.core.ui.component.icon
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import kotlin.math.roundToInt

/** Sheet sửa một mục hồ sơ. Xong → sheet Đang dùng / Mới. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(which: ProfileSheet, d: ProfileData, vm: ProfileViewModel) {
    val start = d.profile.toInput()
    var draft by remember(which) { mutableStateOf(start) }
    var heightText by remember(which) { mutableStateOf(VnFormat.kg(start.heightCm)) }
    var targetText by remember(which) { mutableStateOf(start.targetWeightKg?.let(VnFormat::kg) ?: "") }

    val target = InputLimits.parseDecimal(targetText)
    val weight = d.weight.weightKg
    val directionOk = when (draft.goalType) {
        GoalType.LOSE -> target != null && target < weight
        GoalType.GAIN -> target != null && target > weight
        GoalType.MAINTAIN -> true
    }
    val valid = when (which) {
        ProfileSheet.HEIGHT -> InputLimits.heightValid(InputLimits.parseDecimal(heightText))
        ProfileSheet.GOAL -> draft.goalType == GoalType.MAINTAIN || (InputLimits.weightValid(target) && directionOk)
        else -> true
    }
    fun result(): ProfileInput = when (which) {
        ProfileSheet.HEIGHT -> draft.copy(heightCm = InputLimits.parseDecimal(heightText)!!)
        ProfileSheet.GOAL -> if (draft.goalType == GoalType.MAINTAIN) draft.copy(targetWeightKg = null, weeklyRateKg = 0.0)
        else draft.copy(targetWeightKg = target)
        else -> draft
    }

    SheetScaffold(
        title = when (which) {
            ProfileSheet.SEX -> stringResource(R.string.s41_sex)
            ProfileSheet.BIRTH_YEAR -> stringResource(R.string.s41_birth_year)
            ProfileSheet.HEIGHT -> stringResource(R.string.s41_height)
            ProfileSheet.ACTIVITY -> stringResource(R.string.s41_activity)
            ProfileSheet.GOAL -> stringResource(R.string.s41_goal)
            ProfileSheet.RATE -> stringResource(R.string.s41_sheet_rate_title)
        },
        lead = if (which == ProfileSheet.RATE) stringResource(R.string.s41_sheet_rate_lead, VnFormat.dayMonth(d.weight.date), VnFormat.kg(weight)) else null,
        onDismiss = vm::closeSheet,
        confirm = stringResource(R.string.action_done),
        confirmEnabled = valid,
        onConfirm = { vm.proposeProfile(result()) },
    ) {
        when (which) {
            ProfileSheet.SEX -> Sex.entries.forEach { s ->
                IconChoiceRow(s.icon(), stringResource(s.label()), "", draft.sex == s, { draft = draft.copy(sex = s) })
            }
            ProfileSheet.BIRTH_YEAR -> {
                val ctx = LocalResources.current
                YearWheel(InputLimits.birthYears(d.today.year), draft.birthYear, { draft = draft.copy(birthYear = it) }, { y -> ctx.getString(R.string.s01_year_age, y, d.today.year - y) })
            }
            ProfileSheet.HEIGHT -> NumberField(
                value = heightText, onValueChange = { heightText = it },
                label = stringResource(R.string.field_height), unit = stringResource(R.string.unit_cm),
                error = if (heightText.isNotBlank() && !InputLimits.heightValid(InputLimits.parseDecimal(heightText))) stringResource(R.string.error_height) else null,
            )
            ProfileSheet.ACTIVITY -> ActivityLevel.entries.forEach { a ->
                IconChoiceRow(a.icon(), stringResource(a.label()), stringResource(a.description()), draft.activityLevel == a, { draft = draft.copy(activityLevel = a) }, trailing = "×" + VnFormat.factor(a.factor))
            }
            ProfileSheet.GOAL -> {
                GoalType.entries.forEach { g ->
                    IconChoiceRow(g.icon(), stringResource(g.label()), stringResource(g.description()).substringBefore(" · "), draft.goalType == g, {
                        draft = draft.copy(goalType = g, weeklyRateKg = if (g == start.goalType) start.weeklyRateKg else GoalPlanner.defaultRate(g))
                    })
                }
                if (draft.goalType != GoalType.MAINTAIN) {
                    Spacer(Modifier.height(Spacing.sm))
                    NumberField(
                        value = targetText, onValueChange = { targetText = it },
                        label = stringResource(R.string.field_target_weight), unit = stringResource(R.string.unit_kg),
                        error = when {
                            targetText.isBlank() -> null
                            !InputLimits.weightValid(target) -> stringResource(R.string.error_weight)
                            !directionOk && draft.goalType == GoalType.LOSE -> stringResource(R.string.error_target_lose, VnFormat.kg(weight))
                            !directionOk -> stringResource(R.string.error_target_gain, VnFormat.kg(weight))
                            else -> null
                        },
                    )
                }
            }
            ProfileSheet.RATE -> vm.rateOptions(draft, weight).forEach { o ->
                RateRow(o, selected = draft.weeklyRateKg == o.rateKg, onClick = { draft = draft.copy(weeklyRateKg = o.rateKg) }, weeksUnderRate = true)
            }
        }
    }
}

/** Sheet "Áp dụng mục tiêu mới?" — Đang dùng / Mới (cùng sheet tính lại của S31). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplySheet(p: PendingApply, d: ProfileData, onCancel: () -> Unit, onApply: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val g = d.goal
    val r = p.preview.result
    val newKcal = r.target.targetKcal
    SheetScaffold(
        title = stringResource(R.string.s41_apply_title),
        lead = run {
            val wDate = VnFormat.dayMonth(d.weight.date)
            val w = VnFormat.kg(d.weight.weightKg)
            val used = VnFormat.kg(g.weightKgUsed)
            val usedDate = VnFormat.dayMonth(g.effectiveFrom)
            when (val reason = p.reason) {
                is ApplyReason.Rate -> stringResource(R.string.s41_apply_lead_rate, VnFormat.rate(reason.from), VnFormat.rate(reason.to), wDate, w, used, usedDate)
                is ApplyReason.Override -> {
                    val parts = listOfNotNull(
                        reason.kcal?.let { "${VnFormat.int(it)} kcal" },
                        reason.macro?.let { "macro ${it.protein} / ${it.carb} / ${it.fat}%" },
                    ).joinToString(" và ").ifEmpty { stringResource(R.string.s41_override_none) }
                    stringResource(R.string.s41_apply_lead_override, parts, wDate, w, used, usedDate)
                }
                ApplyReason.Profile -> stringResource(R.string.s41_apply_lead, wDate, w, used, usedDate)
            }
        },
        onDismiss = onCancel,
        confirm = stringResource(R.string.action_apply),
        confirmEnabled = true,
        onConfirm = onApply,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.s41_apply_current), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Text(VnFormat.int(g.targetKcal), style = MaterialTheme.typography.titleLarge, color = cs.onSurfaceVariant)
                Text(stringResource(R.string.unit_kcal_day), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.lg))
            Column(Modifier.weight(1.3f)) {
                Text(stringResource(R.string.s41_apply_new), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Text(VnFormat.int(newKcal), style = MaterialTheme.typography.displaySmall, color = cs.onSurface)
                val delta = newKcal - g.targetKcal
                Text(
                    (if (delta > 0) "+" else if (delta < 0) "−" else "±") + VnFormat.int(kotlin.math.abs(delta)) + " kcal · " + stringResource(R.string.unit_kcal_day),
                    style = MaterialTheme.typography.labelMedium, color = cs.primary,
                )
            }
        }
        Spacer(Modifier.height(Spacing.md))
        CompareTable(
            listOf(
                Triple(stringResource(R.string.metric_tdee), VnFormat.int(g.tdee.roundToInt()), VnFormat.int(r.tdee.roundToInt()) to stringResource(R.string.unit_kcal)),
                Triple(stringResource(R.string.macro_protein), g.proteinG.toString(), r.macros.proteinG.toString() to stringResource(R.string.unit_g)),
                Triple(stringResource(R.string.macro_carb), g.carbG.toString(), r.macros.carbG.toString() to stringResource(R.string.unit_g)),
                Triple(stringResource(R.string.macro_fat), g.fatG.toString(), r.macros.fatG.toString() to stringResource(R.string.unit_g)),
                Triple(stringResource(R.string.metric_water), VnFormat.decimal1(g.waterMl / 1000.0), VnFormat.decimal1(r.waterMl / 1000.0) to "L"),
            ),
        )
        r.target.clampReason?.let { reason ->
            Spacer(Modifier.height(Spacing.md))
            val kcalOv = p.kcalOverride
            val text = if (kcalOv != null && reason == ClampReason.MAX_DEFICIT) {
                stringResource(
                    R.string.s41_override_clamped, VnFormat.int(kcalOv), VnFormat.int((r.tdee - kcalOv).roundToInt()),
                    VnFormat.int((r.tdee * 0.25).roundToInt()), VnFormat.int(newKcal), VnFormat.decimal2(r.target.actualWeeklyRateKg),
                )
            } else {
                stringResource(
                    if (reason == ClampReason.MAX_SURPLUS) R.string.s02_clamped_surplus else R.string.s02_clamped,
                    VnFormat.rate(p.input.weeklyRateKg), VnFormat.int(newKcal), VnFormat.decimal2(r.target.actualWeeklyRateKg),
                )
            }
            AmberBox(text)
        }
        Spacer(Modifier.height(Spacing.md))
        Row {
            Icon(Icons.Outlined.Flag, null, tint = cs.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            val eta = p.preview.etaDate
            val weeks = p.preview.weeks
            val etaPart = if (eta != null && weeks != null && p.input.targetWeightKg != null) {
                stringResource(R.string.s41_apply_eta, VnFormat.kg(p.input.targetWeightKg), VnFormat.date(eta), weeks) + " "
            } else ""
            Text(
                etaPart + stringResource(R.string.s41_apply_from, VnFormat.dayMonth(d.today), VnFormat.int(g.targetKcal)),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurface,
            )
        }
    }
}

@Composable
private fun CompareTable(rows: List<Triple<String, String, Pair<String, String>>>) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.s41_apply_current), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text(stringResource(R.string.s41_apply_new), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    rows.forEach { (label, cur, newPair) ->
        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.55f))
        Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(cur, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                Text(newPair.first, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (newPair.first != cur) FontWeight(650) else null), color = cs.onSurface)
                Spacer(Modifier.width(3.dp))
                Text(newPair.second, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
        }
    }
}

/** Khung chung của sheet: tiêu đề, lời dẫn, nội dung cuộn, Huỷ / nút chính. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetScaffold(
    title: String,
    lead: String?,
    onDismiss: () -> Unit,
    confirm: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp), bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)),
    ) {
        Column(Modifier.padding(horizontal = Spacing.gutter).navigationBarsPadding()) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            if (lead != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(lead, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(Spacing.lg))
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) { content() }
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.lg), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.width(Spacing.sm))
                Button(onClick = onConfirm, enabled = confirmEnabled, shape = CircleShape, modifier = Modifier.height(56.dp)) {
                    Text(confirm, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = Spacing.sm))
                }
            }
        }
    }
}

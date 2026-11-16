package com.cali.feature.water

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.ui.component.hm
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import java.time.LocalTime

private fun parseMl(text: String): Int? = text.filter(Char::isDigit).takeIf { it.isNotEmpty() && it.length <= 6 }?.toInt()

/** Sheet "Thêm lượng khác" / "Sửa lần uống" (s15 f4–f6): chip giờ, ô ml, lỗi khoảng khi rời ô. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AmountSheet(
    s: WaterUiState,
    edit: WaterRow?,
    onDismiss: () -> Unit,
    onSave: (ml: Int, time: LocalTime?) -> Unit,
    onDelete: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf(edit?.entry?.amountMl?.toString().orEmpty()) }
    // null = mặc định (bây giờ / 14:30 cho ngày cũ); khác null khi người dùng chọn giờ.
    var time by remember { mutableStateOf(edit?.time) }
    var pickTime by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    val ml = parseMl(text)
    val valid = ml != null && ml in WaterViewModel.MIN_ML..WaterViewModel.MAX_ML
    // Lỗi chỉ hiện khi rời ô (mockup f5); trong lúc gõ chỉ tắt nút.
    val showError = touched && !focused && text.isNotEmpty() && !valid
    val goal = s.day.goalMl
    val after = s.stats.total - (edit?.entry?.amountMl ?: 0) + (ml ?: 0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.surfaceContainerLow,
    ) {
        Column(Modifier.padding(horizontal = Spacing.gutter).navigationBarsPadding().imePadding()) {
            Text(
                stringResource(if (edit != null) R.string.s15_sheet_edit else R.string.s15_sheet_add),
                style = MaterialTheme.typography.titleLarge, color = cs.onSurface,
            )
            Row(Modifier.padding(top = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                val shown = time ?: if (s.isToday) s.nowTime else WaterViewModel.PAST_DAY_TIME
                val label = if (time == null && s.isToday) stringResource(R.string.s15_chip_now, shown.hm()) else shown.hm()
                val chipCd = stringResource(R.string.s15_chip_cd, shown.hm())
                Row(
                    Modifier.heightIn(min = 48.dp).clip(MaterialTheme.shapes.small).clickable { pickTime = true }
                        .semantics(mergeDescendants = true) { contentDescription = chipCd },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        Modifier.height(32.dp).clip(MaterialTheme.shapes.small).background(cs.secondaryContainer).padding(start = Spacing.sm, end = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Schedule, null, tint = cs.onSecondaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.labelLarge, color = cs.onSecondaryContainer)
                        Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSecondaryContainer)
                    }
                }
                if (edit != null) {
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        edit.entry.source.let { stringResource(sourceLabel(it)) } + " · " + VnFormat.dayMonth(edit.entry.date),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    )
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v.filter(Char::isDigit).take(5) },
                label = { Text(stringResource(R.string.s15_amount)) },
                suffix = { Text(stringResource(R.string.s15_unit_ml)) },
                singleLine = true,
                isError = showError,
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (showError) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Rounded.Error, null, tint = cs.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(Spacing.xs))
                            Text(stringResource(R.string.s15_range_error, VnFormat.int(WaterViewModel.MIN_ML), VnFormat.int(WaterViewModel.MAX_ML)))
                        }
                    } else {
                        val min = VnFormat.int(WaterViewModel.MIN_ML)
                        val max = VnFormat.int(WaterViewModel.MAX_ML)
                        val delta = edit?.let { e -> ml?.let { it - e.entry.amountMl } }
                        val tpl = if (delta != null && delta != 0) stringResource(R.string.s15_support_delta, min, max, "\u0001", "\u0002", VnFormat.signedInt(delta))
                        else stringResource(R.string.s15_support, min, max, "\u0001", "\u0002")
                        Text(boldValues(tpl, VnFormat.int(after), VnFormat.int(goal)))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg).focusRequester(focus)
                    .onFocusChanged { f -> if (focused && !f.isFocused) touched = true; focused = f.isFocused },
            )
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                if (edit != null) {
                    val delCd = stringResource(R.string.s15_delete_cd, edit.time.hm())
                    TextButton(onClick = onDelete, modifier = Modifier.height(48.dp).semantics { contentDescription = delCd }) {
                        Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.action_delete), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge) }
                Spacer(Modifier.width(Spacing.sm))
                Button(
                    onClick = { if (valid) onSave(ml!!, time) },
                    enabled = valid,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp),
                ) {
                    Text(
                        when {
                            edit != null -> stringResource(R.string.action_save)
                            valid -> stringResource(R.string.s15_add_n, VnFormat.int(ml!!))
                            else -> stringResource(R.string.s15_add_plain)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
    if (pickTime) {
        TimePickSheet(
            title = stringResource(R.string.s15_time_title),
            initial = time ?: if (s.isToday) s.nowTime else WaterViewModel.PAST_DAY_TIME,
            onDismiss = { pickTime = false },
            onDone = { time = it; pickTime = false },
        )
    }
}

private fun sourceLabel(src: com.cali.core.data.db.entity.WaterSource) = when (src) {
    com.cali.core.data.db.entity.WaterSource.APP -> R.string.s15_src_app
    com.cali.core.data.db.entity.WaterSource.NOTIFICATION -> R.string.s15_src_notification
    com.cali.core.data.db.entity.WaterSource.WIDGET -> R.string.s15_src_widget
}

/** Sheet chọn giờ (TimeInput M3), dùng ở S15 và S43. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickSheet(title: String, initial: LocalTime, onDismiss: () -> Unit, onDone: (LocalTime) -> Unit, error: ((LocalTime) -> String?)? = null) {
    val cs = MaterialTheme.colorScheme
    val tp = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
    val t = LocalTime.of(tp.hour, tp.minute)
    val err = error?.invoke(t)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.surfaceContainerLow,
    ) {
        Column(Modifier.padding(horizontal = Spacing.gutter).navigationBarsPadding()) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
            Spacer(Modifier.height(Spacing.lg))
            TimeInput(tp)
            if (err != null) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Error, null, tint = cs.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text(err, style = MaterialTheme.typography.bodySmall, color = cs.error)
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.lg), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge) }
                Spacer(Modifier.width(Spacing.sm))
                Button(onClick = { onDone(t) }, enabled = err == null, shape = CircleShape, modifier = Modifier.height(48.dp)) {
                    Text(stringResource(R.string.action_done), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** Sheet "Mục tiêu nước" (s15 f11): cách tính 02 §8 sau một chạm. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalSheet(s: WaterUiState, onDismiss: () -> Unit, onChange: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val e = s.explain
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.surfaceContainerLow,
    ) {
        Column(Modifier.padding(horizontal = Spacing.gutter).navigationBarsPadding()) {
            Text(stringResource(R.string.s15_goal_title), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
            Row(Modifier.padding(top = Spacing.md), verticalAlignment = Alignment.Bottom) {
                Text(VnFormat.int(s.day.goalMl), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (s.day.goalAuto) R.string.s15_goal_auto else R.string.s15_goal_custom),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            Column(Modifier.padding(top = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (e != null) {
                    if (!s.day.goalAuto) {
                        GoalLine(Icons.Rounded.Calculate, boldValues(stringResource(R.string.s15_goal_custom_line, "\u0001", "\u0002"), VnFormat.int(s.day.goalMl), VnFormat.int(e.autoMl)))
                    } else {
                        val raw = e.weightKg * 35
                        val bonus = when (e.activity) { ActivityLevel.ACTIVE -> 300; ActivityLevel.VERY_ACTIVE -> 500; else -> 0 }
                        val act = e.activity?.let { stringResource(activityLabel(it)) }.orEmpty()
                        val tail = when {
                            e.autoMl == 1500 || e.autoMl == 4000 -> stringResource(R.string.s15_goal_clamped)
                            bonus > 0 -> stringResource(R.string.s15_goal_bonus, act, VnFormat.int(bonus))
                            else -> stringResource(R.string.s15_goal_no_bonus, act)
                        }
                        GoalLine(
                            Icons.Rounded.Calculate,
                            boldValues(stringResource(R.string.s15_goal_calc, VnFormat.decimal1(e.weightKg), "\u0001", "\u0002", tail), VnFormat.int(raw), VnFormat.int(e.autoMl)),
                        )
                    }
                    GoalLine(Icons.Rounded.MonitorWeight, AnnotatedString(stringResource(R.string.s15_goal_weight, VnFormat.dayMonth(e.since), VnFormat.decimal1(e.weightKg))))
                }
                GoalLine(Icons.Rounded.EventAvailable, boldValues(stringResource(R.string.s15_goal_week, "\u0001"), stringResource(R.string.s15_goal_week_b)))
            }
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.lg), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.action_close), style = MaterialTheme.typography.labelLarge) }
                Spacer(Modifier.width(Spacing.sm))
                Button(onClick = onChange, shape = CircleShape, modifier = Modifier.height(48.dp)) {
                    Text(stringResource(R.string.s15_goal_change), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun activityLabel(a: ActivityLevel) = when (a) {
    ActivityLevel.SEDENTARY -> R.string.activity_sedentary
    ActivityLevel.LIGHT -> R.string.activity_light
    ActivityLevel.MODERATE -> R.string.activity_moderate
    ActivityLevel.ACTIVE -> R.string.activity_active
    ActivityLevel.VERY_ACTIVE -> R.string.activity_very_active
}

/** \u0001, \u0002 … → các giá trị đậm theo thứ tự. */
@Composable
internal fun boldValues(template: String, vararg values: String): AnnotatedString {
    val st = SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    return buildAnnotatedString {
        template.forEach { c ->
            val i = c.code - 1
            if (i in values.indices) withStyle(st) { append(values[i]) } else append(c)
        }
    }
}

@Composable
private fun GoalLine(icon: ImageVector, text: AnnotatedString) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
    }
}

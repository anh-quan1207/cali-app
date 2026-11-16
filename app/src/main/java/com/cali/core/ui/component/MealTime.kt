package com.cali.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.model.MealType
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Nguồn của giờ ăn, hiện bằng chữ cạnh chip (05 §4.1). */
enum class TimeSource { NOW, MEAL_DEFAULT, USER, SAVED }

/** Giờ ăn · bữa đang chọn trên S12 / S13 / S14. Ngày của món = ngày của giờ ăn. */
data class MealTime(
    val date: LocalDate,
    val time: LocalTime,
    val meal: MealType,
    val mealManual: Boolean,
    val source: TimeSource,
)

private val HM = DateTimeFormatter.ofPattern("HH:mm")
fun LocalTime.hm(): String = format(HM)

/** Chip "🕐 12:35 · ■ Trưa ▾" (+ " · 12/11" khi không phải hôm nay) và nguồn bằng chữ bên cạnh. */
@Composable
fun MealTimeChip(value: MealTime, today: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val meal = stringResource(value.meal.label())
    val dayPart = if (value.date != today) " · " + VnFormat.dayMonth(value.date) else ""
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .clip(MaterialTheme.shapes.small)
                .background(cs.secondaryContainer)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = "Giờ ăn ${value.time.hm()}$dayPart, bữa $meal. Chạm để đổi" }
                .heightIn(min = 40.dp)
                .padding(start = Spacing.md, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Schedule, null, tint = cs.onSecondaryContainer, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("${value.time.hm()} · ", style = MaterialTheme.typography.labelLarge, color = cs.onSecondaryContainer)
            MealSquare(value.meal)
            Spacer(Modifier.width(6.dp))
            Text(meal + dayPart, style = MaterialTheme.typography.labelLarge, color = cs.onSecondaryContainer)
            Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSecondaryContainer)
        }
        Spacer(Modifier.width(Spacing.md))
        val src = when (value.source) {
            TimeSource.NOW -> stringResource(R.string.s13_source_now)
            TimeSource.MEAL_DEFAULT -> stringResource(R.string.s13_source_meal, meal)
            TimeSource.USER -> stringResource(R.string.s13_source_set)
            TimeSource.SAVED -> stringResource(R.string.s13_source_saved)
        }
        Icon(Icons.Rounded.PanTool, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(Spacing.xs))
        Text(src, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
    }
}

/** Sheet "Giờ ăn & bữa" (s12 khung 4): TimeInput M3, segmented bữa, ngày. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealTimeSheet(
    initial: MealTime,
    resolver: MealTypeResolver,
    today: LocalDate,
    onDismiss: () -> Unit,
    onDone: (MealTime) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val tp = rememberTimePickerState(initial.time.hour, initial.time.minute, is24Hour = true)
    var meal by remember { mutableStateOf(initial.meal) }
    var manual by remember { mutableStateOf(initial.mealManual) }
    var date by remember { mutableStateOf(initial.date) }
    var showDate by remember { mutableStateOf(false) }
    val time = LocalTime.of(tp.hour, tp.minute)
    LaunchedEffect(tp.hour, tp.minute) { if (!manual) meal = resolver.fromTime(LocalTime.of(tp.hour, tp.minute)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.surfaceContainerLow,
    ) {
        Column(Modifier.padding(horizontal = Spacing.gutter).navigationBarsPadding()) {
            Text(stringResource(R.string.time_sheet_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
            Spacer(Modifier.height(Spacing.lg))
            TimeInput(tp)
            Row(
                Modifier.clip(MaterialTheme.shapes.small).clickable { showDate = true }.heightIn(min = 48.dp).padding(end = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    stringResource(R.string.time_day) + ": " + dayLabel(date, today),
                    style = MaterialTheme.typography.labelLarge, color = cs.primary,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                MEAL_PICK_ORDER.forEachIndexed { i, m ->
                    SegmentedButton(
                        selected = meal == m,
                        onClick = {
                            meal = m
                            manual = m != resolver.fromTime(time)
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, MEAL_PICK_ORDER.size),
                        icon = { MealSquare(m) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text(stringResource(m.label()), style = MaterialTheme.typography.labelLarge, maxLines = 1) }
                }
            }
            Spacer(Modifier.height(Spacing.md))
            Row {
                Icon(if (manual) Icons.Rounded.PanTool else Icons.Rounded.Sync, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    if (manual) stringResource(R.string.time_manual_hint) else stringResource(R.string.time_auto_hint, rangeLabel(resolver, time), stringResource(meal.label())),
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                )
            }
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.lg), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.titleSmall) }
                Spacer(Modifier.width(Spacing.sm))
                Button(
                    onClick = {
                        val changed = time != initial.time || date != initial.date || meal != initial.meal
                        onDone(MealTime(date, time, meal, manual, if (changed) TimeSource.USER else initial.source))
                    },
                    shape = CircleShape,
                    modifier = Modifier.height(56.dp),
                ) { Text(stringResource(R.string.action_done), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(horizontal = Spacing.sm)) }
            }
        }
    }
    if (showDate) {
        val dp = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(today)
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dp.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    showDate = false
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(dp) }
    }
}

@Composable
fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(R.string.today) + ", " + VnFormat.dayMonth(date)
    today.minusDays(1) -> stringResource(R.string.yesterday) + ", " + VnFormat.dayMonth(date)
    else -> VnFormat.weekday(date) + ", " + VnFormat.dayMonth(date)
}

/** "10:00–13:59" của khung chứa [t]. */
private fun rangeLabel(r: MealTypeResolver, t: LocalTime): String {
    val b = r.boundaries
    val idx = b.indexOfLast { !t.isBefore(it.start) }.let { if (it < 0) b.lastIndex else it }
    val start = b[idx].start
    val end = b[(idx + 1) % b.size].start.minusMinutes(1)
    return start.hm() + "–" + end.hm()
}

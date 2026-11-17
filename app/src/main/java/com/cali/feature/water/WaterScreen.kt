package com.cali.feature.water

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.data.db.entity.WaterSource
import com.cali.core.data.repo.WaterWeekDay
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.component.SwipeToDelete
import com.cali.core.ui.component.hm
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/** S15 Nước trong ngày. [onOpenSettings] mở S43. */
@Composable
fun WaterRoute(onBack: () -> Unit, onOpenSettings: () -> Unit, vm: WaterViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val st = s ?: return
    WaterScreen(st, vm, snack, onBack, onOpenSettings)
}

/** Sheet đang mở. */
private sealed interface WaterSheet {
    data object Add : WaterSheet
    data class Edit(val row: WaterRow) : WaterSheet
    data object Goal : WaterSheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaterScreen(
    s: WaterUiState,
    vm: WaterViewModel,
    snack: SnackbarHostState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    val scrolled by remember { androidx.compose.runtime.derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0 } }
    val barColor by animateColorAsState(if (scrolled) cs.surfaceContainer else cs.surface, label = "bar")
    var sheet by remember { mutableStateOf<WaterSheet?>(null) }
    var pickDate by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(cs.surface)) {
        AppBar(s, barColor, onBack, onPick = { pickDate = true }, onPrev = { vm.shift(-1) }, onNext = { vm.shift(1) })
        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = list,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = Spacing.md, end = Spacing.md, bottom = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                item(key = "hero") { HeroCard(s, onGoal = { sheet = WaterSheet.Goal }, onRemind = onOpenSettings) }
                item(key = "list") { DrinksCard(s, onEdit = { sheet = WaterSheet.Edit(it) }, onDelete = { vm.delete(it.entry.id) }) }
                item(key = "week") { WeekCard(s, onPick = vm::goTo) }
                item(key = "foot") {
                    Text(
                        stringResource(R.string.s15_foot), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm),
                    )
                }
            }
            SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
        }
        Dock(s, onAdd = vm::quickAdd, onDeleteLast = vm::deleteLast, onOther = { sheet = WaterSheet.Add })
    }

    when (val sh = sheet) {
        WaterSheet.Add -> AmountSheet(s, edit = null, onDismiss = { sheet = null }, onSave = { ml, t -> vm.add(ml, t); sheet = null }, onDelete = {})
        is WaterSheet.Edit -> AmountSheet(
            s, edit = sh.row, onDismiss = { sheet = null },
            onSave = { ml, t -> vm.update(sh.row.entry.id, ml, t ?: sh.row.time); sheet = null },
            onDelete = { vm.delete(sh.row.entry.id); sheet = null },
        )
        WaterSheet.Goal -> GoalSheet(s, onDismiss = { sheet = null }, onChange = { sheet = null; onOpenSettings() })
        null -> Unit
    }

    if (pickDate) {
        val dp = rememberDatePickerState(
            initialSelectedDateMillis = s.day.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(s.today)
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dp.selectedDateMillis?.let { vm.goTo(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    pickDate = false
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = { TextButton(onClick = { vm.goTo(s.today); pickDate = false }) { Text(stringResource(R.string.today)) } },
        ) { DatePicker(dp) }
    }
}

// ---------- thanh trên: ← tiêu đề ngày ‹ › (S33) ----------

@Composable
private fun AppBar(s: WaterUiState, color: Color, onBack: () -> Unit, onPick: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val date = s.day.date
    val title = if (s.isToday) stringResource(R.string.s15_title_today) else stringResource(R.string.s15_title_day, VnFormat.dayMonth(date))
    val sub = if (s.isToday) VnFormat.weekdayDayMonth(date) else stringResource(R.string.s15_sub_past, VnFormat.weekday(date), relativeDay(date, s.today))
    val pickCd = stringResource(R.string.s15_pick_day_cd, title, sub)
    Row(
        Modifier.fillMaxWidth().background(color).statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back), tint = cs.onSurfaceVariant) }
        Column(
            Modifier.weight(1f).clip(MaterialTheme.shapes.medium).clickable(onClick = onPick).heightIn(min = 48.dp)
                .padding(horizontal = Spacing.sm, vertical = 2.dp)
                .semantics(mergeDescendants = true) { contentDescription = pickCd },
            verticalArrangement = Arrangement.Center,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sub, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
        IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.s15_prev_day), tint = cs.onSurfaceVariant) }
        IconButton(onClick = onNext, enabled = !s.isToday) {
            Icon(Icons.Rounded.ChevronRight, stringResource(R.string.s15_next_day), tint = if (s.isToday) cs.onSurface.copy(alpha = 0.38f) else cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun relativeDay(date: LocalDate, today: LocalDate): String {
    val n = ChronoUnit.DAYS.between(date, today)
    return if (n == 1L) stringResource(R.string.yesterday) else stringResource(R.string.s15_days_ago, n.toInt())
}

// ---------- thẻ đầu: tổng ngày ----------

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = Spacing.lg, end = Spacing.lg, top = 14.dp, bottom = Spacing.lg),
    ) { content() }
}

@Composable
private fun HeroCard(s: WaterUiState, onGoal: () -> Unit, onRemind: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val st = s.stats
    val goal = s.day.goalMl
    val done = goal > 0 && st.total >= goal
    Card {
        Row(Modifier.heightIn(min = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            if (done) {
                Icon(Icons.Rounded.CheckCircle, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.s15_meta_done, VnFormat.int(st.overMl)), style = MaterialTheme.typography.labelLarge, color = cs.onSurface)
            } else {
                Icon(Icons.Rounded.WaterDrop, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (s.isToday) R.string.s15_meta_today else R.string.s15_meta_past, s.rows.size),
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                )
            }
        }
        Row(Modifier.padding(top = Spacing.sm), verticalAlignment = Alignment.Bottom) {
            Text(VnFormat.int(st.total), style = MaterialTheme.typography.displayLarge, color = cs.onSurface)
            Text(
                stringResource(R.string.s15_unit_ml), style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            val goalCd = stringResource(R.string.s15_of_goal_cd, st.percent, VnFormat.int(goal))
            Column(
                Modifier.clip(MaterialTheme.shapes.medium).clickable(role = Role.Button, onClick = onGoal).heightIn(min = 48.dp)
                    .padding(horizontal = Spacing.xs)
                    .semantics(mergeDescendants = true) { contentDescription = goalCd },
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(stringResource(R.string.s15_pct, st.percent), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.s15_of_goal, VnFormat.int(goal)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Rounded.Info, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
        }
        val laneCd = stringResource(R.string.s15_lane_cd, s.rows.size, VnFormat.int(st.total), VnFormat.int(goal)) +
            if (st.overMl > 0) stringResource(R.string.s15_lane_over_cd, VnFormat.int(st.overMl)) else ""
        WaterLane(st.laneSegments, goal, st.overMl, Modifier.padding(top = 18.dp).semantics { contentDescription = laneCd })
        Text(
            if (done) boldValues(stringResource(R.string.s15_reached_at, "\u0001"), s.rows[st.reachedIndex ?: 0].time.hm())
            else boldValues(stringResource(R.string.s15_left, "\u0001", st.cupsLeft.toString(), VnFormat.int(s.cupSmall)), VnFormat.int(st.leftMl)),
            style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm),
        )
        s.reminder?.let { r ->
            HorizontalDivider(Modifier.padding(top = Spacing.md), color = cs.outlineVariant.copy(alpha = 0.55f))
            ReminderRow(r, onRemind)
        }
    }
}


/** Làn nước: dài = mục tiêu, mỗi lần uống một đoạn primary đầu vuông, khe 3dp; vượt mục tiêu chạy làn 6dp riêng, vẫn primary. */
@Composable
private fun WaterLane(segments: List<Pair<Int, Int>>, goal: Int, overMl: Int, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val track = cs.surfaceContainerHighest
    val mark = cs.onSurface
    val fill = cs.primary
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(18.dp)) {
            val gap = 3.dp.toPx()
            val laneTop = 4.dp.toPx()
            val laneH = 10.dp.toPx()
            val w = size.width
            drawRect(track, Offset(0f, laneTop), Size(w, laneH))
            if (goal > 0) segments.forEach { (a, b) ->
                val x = a.toFloat() / goal * w
                val raw = (b - a).toFloat() / goal * w - if (b < goal) gap else 0f
                drawRect(fill, Offset(x, laneTop), Size(maxOf(4.dp.toPx(), raw), laneH))
            }
            val mw = 2.dp.toPx()
            drawRect(mark, Offset(w - mw / 2f - 1f, 0f), Size(mw, size.height))
        }
        if (overMl > 0 && goal > 0) {
            Canvas(Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp)) {
                val r = CornerRadius(3.dp.toPx())
                drawRoundRect(fill.copy(alpha = 0.18f), cornerRadius = r)
                drawRoundRect(fill, size = Size(maxOf(6.dp.toPx(), overMl.toFloat() / goal * size.width), size.height), cornerRadius = r)
            }
        }
    }
}

@Composable
private fun ReminderRow(r: ReminderLine, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val (icon, text) = when (r) {
        is ReminderLine.Next -> Icons.Outlined.Notifications to boldValues(
            stringResource(if (r.tomorrow) R.string.s15_remind_next_tomorrow else R.string.s15_remind_next, "\u0001"), r.time.hm(),
        )
        is ReminderLine.GoalMet -> Icons.Outlined.NotificationsPaused to boldValues(stringResource(R.string.s15_remind_done, "\u0001"), r.time.hm())
        ReminderLine.NoPermission -> Icons.Outlined.NotificationsOff to boldValues(stringResource(R.string.s15_remind_no_perm, "\u0001"), stringResource(R.string.s15_remind_no_perm_b))
        ReminderLine.Off -> Icons.Outlined.NotificationsOff to AnnotatedString(stringResource(R.string.s15_remind_off))
    }
    val cd = stringResource(R.string.s15_remind_cd, text.text)
    Row(
        Modifier.padding(top = Spacing.xs).bleed(Spacing.sm)
            .clip(MaterialTheme.shapes.medium).clickable(onClick = onClick).heightIn(min = 48.dp).padding(horizontal = Spacing.sm)
            .semantics(mergeDescendants = true) { contentDescription = cd },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.md))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

/** Tràn ra hai bên [by] (vùng chạm rộng hơn lề thẻ, như `margin: 0 -8px` của mockup). */
private fun Modifier.bleed(by: androidx.compose.ui.unit.Dp) = layout { measurable, constraints ->
    val px = by.roundToPx()
    val wide = constraints.copy(
        minWidth = constraints.maxWidth + 2 * px,
        maxWidth = constraints.maxWidth + 2 * px,
    )
    val p = measurable.measure(wide)
    layout(constraints.maxWidth, p.height) { p.place(-px, 0) }
}

// ---------- Các lần uống ----------

@Composable
private fun WaterSource.label(): String = stringResource(
    when (this) {
        WaterSource.APP -> R.string.s15_src_app
        WaterSource.NOTIFICATION -> R.string.s15_src_notification
        WaterSource.WIDGET -> R.string.s15_src_widget
    },
)

private fun WaterSource.icon(): ImageVector = when (this) {
    WaterSource.APP -> Icons.Outlined.WaterDrop
    WaterSource.NOTIFICATION -> Icons.Outlined.Notifications
    WaterSource.WIDGET -> Icons.Outlined.Widgets
}

@Composable
private fun DrinksCard(s: WaterUiState, onEdit: (WaterRow) -> Unit, onDelete: (WaterRow) -> Unit) {
    val cs = MaterialTheme.colorScheme
    // Hàng tràn hết chiều ngang thẻ để nền vuốt xoá phủ kín (S31 record rows).
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(top = 14.dp, bottom = Spacing.sm)) {
        Text(
            stringResource(R.string.s15_list), style = MaterialTheme.typography.titleMedium, color = cs.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.lg).semantics { heading() },
        )
        Spacer(Modifier.height(6.dp))
        if (s.rows.isEmpty()) {
            Text(
                stringResource(R.string.s15_list_empty), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = Spacing.lg, vertical = Spacing.md),
            )
        }
        s.rows.forEachIndexed { i, row ->
            // key theo id để vuốt xoá không kéo theo hàng khác
            androidx.compose.runtime.key(row.entry.id) {
                SwipeToDelete(onDelete = { onDelete(row) }) {
                    DrinkRow(row, divider = i > 0, onClick = { onEdit(row) })
                }
            }
        }
    }
}

@Composable
private fun DrinkRow(row: WaterRow, divider: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val src = row.entry.source.label()
    val cd = stringResource(R.string.s15_row_cd, row.time.hm(), VnFormat.int(row.entry.amountMl), src.lowercase())
    Column(Modifier.background(cs.surfaceContainerLow)) {
        if (divider) HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.55f))
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 48.dp).padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                .semantics(mergeDescendants = true) { contentDescription = cd },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                row.time.hm(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface,
                modifier = Modifier.width(56.dp),
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(row.entry.source.icon(), null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text(src, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                row.reachedAt?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = cs.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.s15_reached_tag, VnFormat.int(it)), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = cs.primary)
                    }
                }
            }
            Text(VnFormat.int(row.entry.amountMl), style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
            Text(
                stringResource(R.string.s15_unit_ml), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 3.dp),
            )
        }
    }
}

// ---------- Tuần này: cột 7 ngày (S35) ----------

@Composable
private fun WeekCard(s: WaterUiState, onPick: (LocalDate) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val week = s.week
    if (week.size != 7) return
    val start = week.first().date
    val end = week.last().date
    val past = week.filter { it.date.isBefore(s.today) }
    val hit = past.count { it.goalMl > 0 && it.totalMl >= it.goalMl }
    val goal = s.day.goalMl
    val under = lerp(cs.surfaceContainerLow, cs.primary, 0.7f)
    val pend = lerp(CaliTheme.colors.ringTrack, cs.primary, 0.4f)
    val short = stringArrayResource(R.array.weekday_short)
    Card {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 24.dp)) {
            val inThisWeek = !s.today.isBefore(start) && !s.today.isAfter(end)
            Text(
                stringResource(if (inThisWeek) R.string.s15_week_this else R.string.s15_week, VnFormat.dayMonth(start), VnFormat.dayMonth(end)),
                style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() },
            )
            if (past.isNotEmpty()) Text(stringResource(R.string.s15_week_hit, hit, past.size), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        val top = maxOf(3200, (week.maxOf { it.totalMl } * 1.1).toInt(), (goal * 1.2).toInt())
        val plotH = 96.dp
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 32.dp).height(plotH)) {
            val colW = maxWidth / 7
            // vạch mục tiêu
            Canvas(Modifier.fillMaxSize()) {
                if (goal > 0) {
                    val y = size.height - goal.toFloat() / top * size.height
                    drawLine(cs.onSurfaceVariant, Offset(0f, y), Offset(size.width, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
                }
            }
            week.forEachIndexed { i, d ->
                val future = d.date.isAfter(s.today)
                if (future) return@forEachIndexed
                val isToday = d.date == s.today
                val ok = d.goalMl > 0 && d.totalMl >= d.goalMl
                val h = plotH * (d.totalMl.toFloat() / top)
                val state = stringResource(if (isToday) R.string.s15_col_today else if (ok) R.string.s15_col_hit else R.string.s15_col_miss)
                val cd = stringResource(R.string.s15_col_cd, short[i], VnFormat.rate(d.totalMl / 1000.0), state)
                Box(
                    Modifier.offset(x = colW * i).width(colW).fillMaxSize().clickable { onPick(d.date) }
                        .semantics(mergeDescendants = true) { contentDescription = cd },
                ) {
                    if (d.totalMl > 0) {
                        Text(
                            VnFormat.rate(d.totalMl / 1000.0), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().offset(y = plotH - h - 18.dp),
                        )
                    }
                    Box(
                        Modifier.align(Alignment.BottomCenter).width(20.dp).height(h)
                            .background(if (isToday) pend else if (ok) cs.primary else under)
                            .let { if (isToday) it.border(1.5.dp, cs.primary) else it },
                    )
                }
            }
        }
        HorizontalDivider(color = cs.outlineVariant)
        Row(Modifier.fillMaxWidth()) {
            week.forEachIndexed { i, d ->
                val cur = d.date == s.day.date
                val future = d.date.isAfter(s.today)
                Column(Modifier.weight(1f).height(26.dp).padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        short[i],
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (cur) FontWeight.Bold else FontWeight.Medium),
                        color = when { cur -> cs.onSurface; future -> cs.onSurface.copy(alpha = 0.38f); else -> cs.onSurfaceVariant },
                    )
                    if (cur) Box(Modifier.size(4.dp).clip(CircleShape).background(cs.primary))
                }
            }
        }
        FlowRow(
            Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LegendItem(cs.primary, null, stringResource(R.string.s15_legend_hit), hit.toString())
            LegendItem(under, null, stringResource(R.string.s15_legend_miss), (past.size - hit).toString())
            LegendItem(pend, cs.primary, stringResource(R.string.s15_legend_today), null)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(10.dp)) {
                    drawLine(cs.onSurfaceVariant, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 2f)))
                }
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.s15_legend_goal, VnFormat.rate(goal / 1000.0)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LegendItem(fill: Color, stroke: Color?, label: String, count: String?) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) {
            drawRect(fill)
            if (stroke != null) drawRect(stroke, style = Stroke(1.5.dp.toPx()))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            buildAnnotatedString {
                append(label)
                if (count != null) {
                    append(" ")
                    withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.SemiBold)) { append(count) }
                }
            },
            style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
        )
    }
}

// ---------- chân ghim: +250 / Xoá lần vừa thêm / + (G5-8) ----------

@Composable
private fun Dock(s: WaterUiState, onAdd: (Int) -> Unit, onDeleteLast: () -> Unit, onOther: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val day = VnFormat.dayMonth(s.day.date)
    Column(Modifier.fillMaxWidth().background(cs.surfaceContainer).navigationBarsPadding().padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.md, bottom = Spacing.md)) {
        if (!s.isToday) {
            Row(Modifier.padding(start = Spacing.xs, end = Spacing.xs, bottom = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.History, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.s15_past_note, day, WaterViewModel.PAST_DAY_TIME.hm()), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
        val cdSmall = if (s.isToday) stringResource(R.string.s15_add_cd, VnFormat.int(s.cupSmall)) else stringResource(R.string.s15_add_cd_day, VnFormat.int(s.cupSmall), day)
        val canDelete = s.day.entries.isNotEmpty()
        val deleteCd = stringResource(R.string.water_delete_last_cd)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { onAdd(s.cupSmall) }, shape = CircleShape,
                modifier = Modifier.weight(1.25f).height(48.dp).semantics { contentDescription = cdSmall },
            ) {
                Icon(Icons.Rounded.WaterDrop, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s15_add, VnFormat.int(s.cupSmall)), style = MaterialTheme.typography.labelLarge)
            }
            FilledTonalButton(
                onClick = onDeleteLast, enabled = canDelete, shape = CircleShape,
                modifier = Modifier.weight(1f).height(48.dp).semantics { contentDescription = deleteCd },
            ) {
                Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.water_delete_last), style = MaterialTheme.typography.labelLarge)
            }
            OutlinedIconButton(
                onClick = onOther, modifier = Modifier.size(48.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cs.outlineVariant)),
            ) { Icon(Icons.Rounded.Add, stringResource(R.string.s15_add_other_cd), tint = cs.primary) }
        }
    }
}

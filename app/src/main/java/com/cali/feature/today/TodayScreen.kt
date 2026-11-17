package com.cali.feature.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.calc.RingStatus
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.ui.component.CalorieRing
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.component.FoodRow
import com.cali.core.ui.component.MEAL_CARD_ORDER
import com.cali.core.ui.component.MacroBars
import com.cali.core.ui.component.MacroValue
import com.cali.core.ui.component.MealCard
import com.cali.core.ui.component.SwipeToDelete
import com.cali.core.ui.component.label
import com.cali.core.ui.component.ringParts
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import com.cali.feature.weight.WeightSheet
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

/** S10 Hôm nay. [onQuickAdd] mở S13 cho ngày đang xem, [meal] = bữa khi bấm "+" trên thẻ bữa. */
@Composable
fun TodayRoute(
    onQuickAdd: (date: LocalDate, meal: MealType?) -> Unit,
    onEditFood: (id: Long) -> Unit,
    onOpenWater: (date: LocalDate) -> Unit,
    onCapture: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenWeekly: (LocalDate) -> Unit = {},
    /** S33: mở cho một ngày cụ thể, có nút quay lại. */
    fixedDate: LocalDate? = null,
    onBack: (() -> Unit)? = null,
    vm: TodayViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshToday() }
    LaunchedEffect(fixedDate) { fixedDate?.let(vm::goTo) }
    val st = s ?: return
    TodayScreen(st, vm, snack, onQuickAdd, onEditFood, onOpenWater, onCapture, onOpenChat, onOpenWeekly, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScreen(
    s: TodayUiState,
    vm: TodayViewModel,
    snack: SnackbarHostState,
    onQuickAdd: (LocalDate, MealType?) -> Unit,
    onEditFood: (Long) -> Unit,
    onOpenWater: (LocalDate) -> Unit,
    onCapture: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenWeekly: (LocalDate) -> Unit,
    onBack: (() -> Unit)?,
) {
    val cs = MaterialTheme.colorScheme
    val list = rememberLazyListState()
    // Hàng nút là mục thứ 3 (vòng, macro, nút): khuất thì hiện FAB và thanh trên tonal.
    val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 2 } }
    val barColor by animateColorAsState(if (list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0) cs.surfaceContainer else cs.surface, label = "bar")
    var pickDate by remember { mutableStateOf(false) }
    var weigh by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val day = s.day
    val sum = day.summary
    val date = day.date
    val isToday = date == s.today

    Box(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.fillMaxSize()) {
            // Thanh ngày (05 §2 S10 mục 1); S33 có nút quay lại, tiêu đề trái.
            if (onBack != null) DayBackBar(s, barColor, onBack, onPick = { pickDate = true }, onPrev = { vm.shift(-1) }, onNext = { vm.shift(1) })
            else Row(
                Modifier.fillMaxWidth().background(barColor).height(64.dp).padding(horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { vm.shift(-1) }) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.s10_prev_day), tint = cs.onSurfaceVariant) }
                Column(
                    Modifier.weight(1f).clip(MaterialTheme.shapes.medium).clickable { pickDate = true }
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        when (date) {
                            s.today -> stringResource(R.string.today)
                            s.today.minusDays(1) -> stringResource(R.string.yesterday)
                            else -> VnFormat.dayMonth(date)
                        },
                        style = MaterialTheme.typography.titleLarge, color = cs.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(VnFormat.weekdayDayMonth(date), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                        Icon(Icons.Rounded.ArrowDropDown, stringResource(R.string.s10_pick_day), tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = { vm.shift(1) }, enabled = !isToday) {
                    Icon(Icons.Rounded.ChevronRight, stringResource(R.string.s10_next_day), tint = if (isToday) cs.onSurface.copy(alpha = 0.38f) else cs.onSurfaceVariant)
                }
            }

            LazyColumn(state = list, contentPadding = PaddingValues(bottom = 96.dp), modifier = Modifier.fillMaxSize()) {
                item(key = "ring") { if (date.isBefore(s.today)) PastRingBlock(s) else RingBlock(s) }
                item(key = "macros") {
                    val g = day.goal
                    MacroBars(
                        MacroValue(sum.proteinG, g?.proteinG ?: 0),
                        MacroValue(sum.carbG, g?.carbG ?: 0),
                        MacroValue(sum.fatG, g?.fatG ?: 0),
                        Modifier.padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.lg),
                        past = date.isBefore(s.today),
                    )
                }
                item(key = "actions") {
                    Row(Modifier.padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.lg), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Button(
                            onClick = onCapture,
                            modifier = Modifier.weight(1.25f).heightIn(min = 48.dp),
                            contentPadding = PaddingValues(horizontal = Spacing.lg),
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            Text(stringResource(R.string.s10_capture), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                        FilledTonalButton(
                            onClick = onOpenChat,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.s10_chat), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                        OutlinedIconButton(
                            onClick = { onQuickAdd(date, null) },
                            modifier = Modifier.size(48.dp),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cs.outlineVariant)),
                        ) { Icon(Icons.Rounded.Add, stringResource(R.string.s10_quick_add), tint = cs.primary) }
                    }
                }
                s.week?.let { wk ->
                    item(key = "week") { WeekTileView(wk, s.today, onOpen = { onOpenWeekly(wk.weekStart) }) }
                }
                item(key = "section") {
                    Row(Modifier.fillMaxWidth().padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.gutter, bottom = Spacing.sm), verticalAlignment = Alignment.Bottom) {
                        Text(stringResource(R.string.s10_meals), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
                        Text(stringResource(R.string.s10_meals_hint), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                }
                if (day.entries.isEmpty()) {
                    item(key = "empty") { EmptyHero(isToday) }
                } else {
                    items(MEAL_CARD_ORDER, key = { "meal-$it" }) { meal ->
                        val entries = day.entries.filter { it.mealType == meal }
                        val total = sum.meals.getValue(meal)
                        MealCard(meal, total.kcal, total.count, onAdd = { onQuickAdd(date, meal) }, modifier = Modifier.padding(horizontal = Spacing.md).padding(bottom = Spacing.sm)) {
                            entries.forEach { e -> FoodItem(e, vm, onEditFood) }
                        }
                    }
                }
                // GĐ5: ô Nước + ô Cân cạnh nhau (mockup s10 .tiles).
                item(key = "tiles") {
                    Row(
                        Modifier.padding(start = Spacing.md, end = Spacing.md, top = Spacing.sm).height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        WaterTileView(s.water, onOpen = { onOpenWater(date) }, onAdd = vm::addWater, modifier = Modifier.weight(1f).fillMaxHeight())
                        WeightTileView(s.weight, onLog = { weigh = true }, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
                item(key = "disclaimer") {
                    Text(
                        stringResource(R.string.disclaimer_kcal), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(start = Spacing.xl, end = Spacing.xl, top = 18.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = scrolled,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = Spacing.lg, bottom = Spacing.lg),
        ) {
            ExtendedFloatingActionButton(
                onClick = onCapture,
                icon = { Icon(Icons.Rounded.PhotoCamera, null) },
                text = { Text(stringResource(R.string.s10_capture), style = MaterialTheme.typography.labelLarge) },
                containerColor = cs.primaryContainer,
                contentColor = cs.onPrimaryContainer,
            )
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = if (scrolled) 80.dp else 0.dp))
    }

    if (pickDate) {
        val dp = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
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
            dismissButton = {
                TextButton(onClick = { vm.goTo(s.today); pickDate = false }) { Text(stringResource(R.string.today)) }
            },
        ) { DatePicker(dp) }
    }
    if (weigh) WeightSheet(onDismiss = { weigh = false })
}

@Composable
private fun RingBlock(s: TodayUiState) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val sum = s.day.summary
    val target = sum.targetKcal ?: 0
    val state = sum.status
    val stateColor = when (state) {
        RingStatus.OVER_LIGHT -> cc.warn
        RingStatus.OVER_MUCH -> cs.error
        else -> cs.onSurface
    }
    val over = state == RingStatus.OVER_LIGHT || state == RingStatus.OVER_MUCH
    Column(Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally) {
        val ringCd = stringResource(R.string.s10_ring_cd, VnFormat.int(sum.totalKcal.roundToInt()), VnFormat.int(target))
        CalorieRing(
            segments = ringParts(sum.segments),
            targetKcal = target.toDouble(),
            overColor = stateColor,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = ringCd },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(if (over) R.string.s10_over_title else R.string.s10_remaining),
                    style = MaterialTheme.typography.labelLarge, color = if (over) stateColor else cs.onSurfaceVariant,
                )
                Text(
                    if (over) "+" + VnFormat.int(sum.overKcal) else VnFormat.int(if (target > 0) sum.remainingKcal else 0),
                    style = MaterialTheme.typography.displayLarge, color = if (over) stateColor else cs.onSurface,
                    maxLines = 1,
                )
                Text(stringResource(R.string.unit_kcal), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
        Row(Modifier.padding(top = Spacing.xs), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            Stat(stringResource(R.string.s10_eaten), VnFormat.int(sum.totalKcal.roundToInt()))
            Stat(stringResource(R.string.s10_target), if (target > 0) VnFormat.int(target) else "—")
        }
        if (over) {
            val container = if (state == RingStatus.OVER_LIGHT) cc.warnContainer else cs.errorContainer
            val onContainer = if (state == RingStatus.OVER_LIGHT) cc.onWarnContainer else cs.onErrorContainer
            Row(
                Modifier.padding(top = 10.dp).clip(MaterialTheme.shapes.small).background(container).heightIn(min = 32.dp).padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (state == RingStatus.OVER_LIGHT) Icons.Rounded.Info else Icons.Rounded.Warning, null, tint = onContainer, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (state == RingStatus.OVER_LIGHT) R.string.s10_over_light else R.string.s10_over_much, sum.overPercent),
                    style = MaterialTheme.typography.labelLarge, color = onContainer,
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
private fun FoodItem(e: FoodEntryEntity, vm: TodayViewModel, onEditFood: (Long) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    var moveMenu by remember { mutableStateOf(false) }
    Box {
        SwipeToDelete(onDelete = { vm.delete(e) }) {
            FoodRow(
                time = vm.timeLabel(e),
                name = e.name,
                portion = e.portionDesc,
                kcal = e.kcal,
                source = e.source,
                onClick = { onEditFood(e.id) },
                onLongClick = { menu = true },
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.s10_menu_edit)) }, onClick = { menu = false; onEditFood(e.id) })
            DropdownMenuItem(
                text = { Text(stringResource(R.string.s10_menu_move)) },
                trailingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowRight, null) },
                onClick = { menu = false; moveMenu = true },
            )
            DropdownMenuItem(
                text = { Text(stringResource(if (e.myFoodId != null) R.string.s10_menu_saved else R.string.s10_menu_save)) },
                enabled = e.myFoodId == null,
                onClick = { menu = false; vm.saveToMyFoods(e) },
            )
        }
        DropdownMenu(expanded = moveMenu, onDismissRequest = { moveMenu = false }) {
            MEAL_CARD_ORDER.filter { it != e.mealType }.forEach { m ->
                val name = stringResource(m.label())
                DropdownMenuItem(text = { Text(name) }, onClick = { moveMenu = false; vm.move(e, m, name) })
            }
        }
    }
}

@Composable
private fun EmptyHero(isToday: Boolean) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.padding(horizontal = Spacing.md).padding(top = Spacing.xs, bottom = Spacing.sm).fillMaxWidth()
            .clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.gutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.large).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.PhotoCamera, null, tint = cs.onPrimaryContainer)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(stringResource(if (isToday) R.string.s10_empty_title else R.string.s10_empty_title_day), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(stringResource(R.string.s10_empty_body), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

/** Ô Nước (S10 mục 6, bật ở GĐ5): đã uống / mục tiêu, thanh, +nhỏ / +lớn. Chạm ô mở S15. Vượt mục tiêu vẫn primary. */
@Composable
private fun WaterTileView(w: WaterTile, onOpen: () -> Unit, onAdd: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val openCd = stringResource(R.string.s10_water_open_cd, VnFormat.int(w.totalMl), VnFormat.int(w.goalMl))
    Column(
        modifier.clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).clickable(onClick = onOpen)
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = Spacing.md),
    ) {
        Column(Modifier.semantics(mergeDescendants = true) { contentDescription = openCd }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WaterDrop, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.s10_water), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = Spacing.sm)) {
                Text(VnFormat.int(w.totalMl), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight(650)), color = cs.onSurface)
                Text(" " + stringResource(R.string.s10_water_of, VnFormat.int(w.goalMl)), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            val frac = if (w.goalMl > 0) (w.totalMl.toFloat() / w.goalMl).coerceIn(0f, 1f) else 0f
            Box(Modifier.padding(top = 6.dp).fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.extraSmall).background(cs.surfaceContainerHighest)) {
                Box(Modifier.fillMaxWidth(frac).fillMaxHeight().clip(MaterialTheme.shapes.extraSmall).background(cs.primary))
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(w.cupSmall, w.cupLarge).forEach { ml ->
                val cd = stringResource(R.string.s10_water_add_cd, VnFormat.int(ml))
                FilledTonalButton(
                    onClick = { onAdd(ml) },
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { contentDescription = cd },
                ) { Text(stringResource(R.string.s10_water_add, VnFormat.int(ml)), style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}

/** Ô cân (S10 mục 7). Từ GĐ5 nằm cạnh ô Nước, nút "Ghi cân" ở dưới như mockup. Thay đổi cân luôn trung tính. */
@Composable
private fun WeightTileView(w: WeightTile?, onLog: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.MonitorWeight, null, tint = cs.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.s10_weight), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurfaceVariant)
        }
        Column(Modifier.padding(top = Spacing.sm)) {
            if (w == null) {
                Text(stringResource(R.string.s10_weight_none), style = MaterialTheme.typography.titleLarge, color = cs.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(VnFormat.kg(w.latest.weightKg), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight(650)), color = cs.onSurface)
                    Text(" kg", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
                val sub = w.previous?.let { p ->
                    stringResource(R.string.s10_weight_vs_last, VnFormat.signedKg(w.latest.weightKg - p.weightKg), VnFormat.dayMonth(p.date))
                } ?: stringResource(R.string.s10_weight_last, VnFormat.dayMonth(w.latest.date))
                Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = onLog,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(top = 10.dp).fillMaxWidth().heightIn(min = 48.dp),
            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cs.outlineVariant)),
        ) { Text(stringResource(R.string.s10_log_weight), style = MaterialTheme.typography.labelLarge, color = cs.primary) }
    }
}

// ---------- GĐ6: S33 Chi tiết ngày ----------

/** S33: ← tiêu đề "Thứ Tư, 12/11" + phụ "Hôm qua ▾", ‹ › chuyển ngày. */
@Composable
private fun DayBackBar(s: TodayUiState, bg: androidx.compose.ui.graphics.Color, onBack: () -> Unit, onPick: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val date = s.day.date
    val isToday = date == s.today
    Row(Modifier.fillMaxWidth().background(bg).statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = cs.onSurface) }
        Column(Modifier.weight(1f).clip(MaterialTheme.shapes.medium).clickable(onClick = onPick).padding(vertical = 2.dp)) {
            Text(VnFormat.weekdayDayMonth(date), style = MaterialTheme.typography.titleLarge, color = cs.onSurface, modifier = Modifier.semantics { heading() })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (date) {
                        s.today -> stringResource(R.string.today)
                        s.today.minusDays(1) -> stringResource(R.string.yesterday)
                        else -> VnFormat.date(date)
                    },
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                )
                Icon(Icons.Rounded.ArrowDropDown, stringResource(R.string.s10_pick_day), tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
        IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.s10_prev_day), tint = cs.onSurfaceVariant) }
        IconButton(onClick = onNext, enabled = !isToday) {
            Icon(Icons.Rounded.ChevronRight, stringResource(R.string.s10_next_day), tint = if (isToday) cs.onSurface.copy(alpha = 0.38f) else cs.onSurfaceVariant)
        }
    }
}

/** Vòng ngày đã qua (00-direction S33): giữa vòng là chênh lệch có dấu + loại ngày theo 08 §2; chưa ghi thì hiện mục tiêu. */
@Composable
private fun PastRingBlock(s: TodayUiState) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val sum = s.day.summary
    val target = sum.targetKcal ?: 0
    val count = s.day.entries.size
    val status = if (target > 0) com.cali.core.calc.WeeklyStatsCalculator.dayStatus(count, sum.totalKcal, target) else com.cali.core.calc.DayStatus.NOT_LOGGED
    val delta = (sum.totalKcal - target).roundToInt()
    val ringState = sum.status
    val overColor = when (ringState) {
        RingStatus.OVER_LIGHT -> cc.warn
        RingStatus.OVER_MUCH -> cs.error
        else -> cs.onSurface
    }
    val suspect = count > 0 && target > 0 && sum.totalKcal < target * 0.5
    Column(Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally) {
        val ringCd = stringResource(R.string.s10_ring_cd, VnFormat.int(sum.totalKcal.roundToInt()), VnFormat.int(target))
        CalorieRing(
            segments = ringParts(sum.segments),
            targetKcal = target.toDouble(),
            overColor = overColor,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = ringCd },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val label = when (status) {
                    com.cali.core.calc.DayStatus.UNDER -> stringResource(R.string.s33_under)
                    com.cali.core.calc.DayStatus.OVER -> stringResource(R.string.s33_over)
                    com.cali.core.calc.DayStatus.ON_TARGET -> stringResource(R.string.s33_on)
                    else -> stringResource(R.string.s33_none)
                }
                val logged = status == com.cali.core.calc.DayStatus.UNDER || status == com.cali.core.calc.DayStatus.OVER || status == com.cali.core.calc.DayStatus.ON_TARGET
                val value = if (logged) VnFormat.signedInt(delta) else if (target > 0) VnFormat.int(target) else "—"
                val isOver = status == com.cali.core.calc.DayStatus.OVER
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (isOver) overColor else cs.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.displayLarge, color = if (isOver) overColor else cs.onSurface, maxLines = 1)
                Text(stringResource(R.string.unit_kcal), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
        Row(Modifier.padding(top = Spacing.xs), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            Stat(stringResource(R.string.s10_eaten), VnFormat.int(sum.totalKcal.roundToInt()))
            Stat(stringResource(R.string.s10_target), if (target > 0) VnFormat.int(target) else "—")
        }
        if (count > 0 && target > 0) {
            val pct = kotlin.math.abs(delta * 100.0 / target).roundToInt()
            val underColor = lerp(cs.surfaceContainerLow, cs.primary, 0.55f)
            val isOver = status == com.cali.core.calc.DayStatus.OVER
            val swatch = when (status) {
                com.cali.core.calc.DayStatus.UNDER -> underColor
                com.cali.core.calc.DayStatus.OVER -> cs.error
                else -> cs.primary
            }
            val name = when (status) {
                com.cali.core.calc.DayStatus.UNDER -> stringResource(R.string.gd6_legend_under)
                com.cali.core.calc.DayStatus.OVER -> stringResource(R.string.gd6_legend_over)
                else -> stringResource(R.string.gd6_legend_on)
            }
            val detail = when (status) {
                com.cali.core.calc.DayStatus.UNDER -> stringResource(R.string.s33_chip_under, pct)
                com.cali.core.calc.DayStatus.OVER -> stringResource(R.string.s33_chip_over, pct)
                else -> stringResource(R.string.s33_chip_on, pct)
            }
            val container = when {
                isOver && ringState == RingStatus.OVER_LIGHT -> cc.warnContainer
                isOver -> cs.errorContainer
                else -> cs.surfaceContainerHigh
            }
            val onContainer = when {
                isOver && ringState == RingStatus.OVER_LIGHT -> cc.onWarnContainer
                isOver -> cs.onErrorContainer
                else -> cs.onSurface
            }
            Row(
                Modifier.padding(top = 10.dp).clip(MaterialTheme.shapes.small).background(container).heightIn(min = 32.dp).padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(12.dp).background(swatch))
                Spacer(Modifier.width(Spacing.sm))
                Text(name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = onContainer)
                Spacer(Modifier.width(6.dp))
                Text(detail, style = MaterialTheme.typography.labelLarge, color = onContainer)
            }
            if (suspect) {
                Row(
                    Modifier.padding(top = 6.dp).clip(MaterialTheme.shapes.small).background(cc.warnContainer).heightIn(min = 32.dp).padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Warning, null, tint = cc.onWarnContainer, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.s33_chip_suspect), style = MaterialTheme.typography.labelLarge, color = cc.onWarnContainer)
                }
            }
        }
    }
}

/** Thẻ tuần (S33, S10 Thứ Hai–Ba): ô điểm + tên tuần + Tạm tính / xếp loại; hàng "Tạo lại" khi nhận xét AI đã cũ. */
@Composable
private fun WeekTileView(w: WeekTile, today: LocalDate, onOpen: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val end = w.weekStart.plusDays(6)
    Column(
        Modifier.padding(start = Spacing.md, end = Spacing.md, top = Spacing.lg).fillMaxWidth()
            .clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.medium).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
                Text(w.score.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = cs.onPrimaryContainer)
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                val inWeek = !today.isBefore(w.weekStart) && !today.isAfter(end)
                Text(
                    when {
                        w.lastWeek -> stringResource(R.string.s10_last_week, VnFormat.dayMonth(w.weekStart), VnFormat.dayMonth(end))
                        inWeek -> stringResource(R.string.gd6_this_week_range, VnFormat.dayMonth(w.weekStart), VnFormat.dayMonth(end))
                        else -> stringResource(R.string.gd6_week_range, VnFormat.dayMonth(w.weekStart), VnFormat.dayMonth(end))
                    },
                    style = MaterialTheme.typography.titleMedium, color = cs.onSurface,
                )
                if (w.partial) {
                    Text(
                        stringResource(R.string.gd6_partial_left, w.remainingDays), style = MaterialTheme.typography.labelMedium, color = cs.onSecondaryContainer,
                        modifier = Modifier.padding(top = 4.dp).clip(MaterialTheme.shapes.small).background(cs.secondaryContainer).padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                } else {
                    Text(
                        stringResource(
                            R.string.s10_last_week_sub, com.cali.feature.stats.gradeText(w.grade),
                            if (w.deltaKcal >= 0) stringResource(R.string.s10_delta_over, VnFormat.int(w.deltaKcal)) else stringResource(R.string.s10_delta_under, VnFormat.int(-w.deltaKcal)),
                            w.onTargetDays, w.countedDays,
                        ),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    )
                }
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
        }
        if (w.reviewStale) {
            HorizontalDivider(color = cs.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(start = Spacing.md, end = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s33_regen), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpen, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.s33_regen_action)) }
            }
        }
    }
}

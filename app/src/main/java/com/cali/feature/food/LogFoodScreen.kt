package com.cali.feature.food

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NoFood
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Warning
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.ui.component.HighlightedName
import com.cali.core.ui.component.MacroDots
import com.cali.core.ui.component.MealTimeChip
import com.cali.core.ui.component.MealTimeSheet
import com.cali.core.ui.component.ProjectionBar
import com.cali.core.ui.component.SwipeToDelete
import com.cali.core.ui.component.dayLabel
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun LogFoodRoute(onClose: () -> Unit, onRetake: () -> Unit = onClose, onAskAi: (String) -> Unit = {}, vm: LogFoodViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val done by vm.done.collectAsStateWithLifecycle()
    LaunchedEffect(done) { if (done) onClose() }
    BackHandler { if (!vm.back()) onClose() }
    val s = ui ?: return
    var timeSheet by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        when (s.stage) {
            LogStage.PICK -> PickScreen(s, vm, onClose, onTime = { timeSheet = true }, onAskAi = onAskAi)
            LogStage.MANUAL -> ManualScreen(s, vm, onTime = { timeSheet = true })
            LogStage.CONFIRM -> ConfirmScreen(s, vm, snack, onClose, onTime = { timeSheet = true }, onRetake = onRetake)
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp))
    }
    if (timeSheet) {
        MealTimeSheet(s.mealTime, vm.resolver, s.today, onDismiss = { timeSheet = false }, onDone = { vm.setMealTime(it); timeSheet = false })
    }
}

@Composable
private fun subtitle(s: LogUi): String? {
    val d = s.day ?: return null
    if (d.goal == null) return null
    val sum = d.summary
    return when {
        s.mealTime.date != s.today -> stringResource(R.string.s13_sub_day, dayLabel(s.mealTime.date, s.today), VnFormat.int(sum.remainingKcal))
        sum.overKcal > 0 -> stringResource(R.string.s13_sub_today_over, VnFormat.int(sum.overKcal))
        else -> stringResource(R.string.s13_sub_today, VnFormat.int(sum.remainingKcal))
    }
}

// ---------------- S13 ----------------

@Composable
private fun PickScreen(s: LogUi, vm: LogFoodViewModel, onClose: () -> Unit, onTime: () -> Unit, onAskAi: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    val snack = remember { SnackbarHostState() }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            SheetHeader(
                stringResource(R.string.s13_title), subtitle(s),
                if (s.items.isEmpty()) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                stringResource(if (s.items.isEmpty()) R.string.action_close else R.string.action_back),
                onNav = { if (!vm.back()) onClose() },
            )
            TextField(
                value = s.query,
                onValueChange = vm::setQuery,
                placeholder = {
                    Text(if (s.savedCount > 0) stringResource(R.string.s13_search_hint, s.savedCount) else stringResource(R.string.s13_search_hint_empty))
                },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = if (s.query.isNotEmpty()) { { IconButton(onClick = { vm.setQuery("") }) { Icon(Icons.Rounded.Close, stringResource(R.string.action_close)) } } } else null,
                singleLine = true,
                shape = CircleShape,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = cs.surfaceContainerHighest,
                    focusedContainerColor = cs.surfaceContainerHighest,
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md).heightIn(min = 56.dp),
            )
            MealTimeChip(s.mealTime, s.today, onTime, Modifier.padding(start = Spacing.gutter, end = Spacing.lg, top = Spacing.md))
            val results = s.searchResults
            if (results == null) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter, vertical = Spacing.md)) {
                    PickTab.entries.forEachIndexed { i, t ->
                        SegmentedButton(
                            selected = s.tab == t, onClick = { vm.setTab(t) },
                            shape = SegmentedButtonDefaults.itemShape(i, PickTab.entries.size),
                            icon = {},
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(
                                stringResource(
                                    when (t) {
                                        PickTab.RECENT -> R.string.s13_tab_recent
                                        PickTab.FREQUENT -> R.string.s13_tab_frequent
                                        PickTab.FAVORITE -> R.string.s13_tab_favorite
                                    },
                                ),
                                style = MaterialTheme.typography.labelLarge, maxLines = 1,
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(Spacing.md))
            }
            Box(Modifier.weight(1f)) {
                if (results != null && results.isEmpty()) {
                    NoMatch(s.query, onManual = vm::openManual, onAi = { onAskAi(s.query.trim()) })
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize().padding(horizontal = Spacing.md).clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow),
                        contentPadding = PaddingValues(bottom = Spacing.lg),
                    ) {
                        if (results != null) {
                            rows(results, vm)
                        } else {
                            when (s.tab) {
                                PickTab.RECENT -> if (s.recent.isEmpty()) {
                                    item { EmptyTab(stringResource(R.string.s13_empty_recent)) }
                                } else {
                                    s.recent.forEach { (d, list) ->
                                        item(key = "h$d") { DayHeader(d, s.today) }
                                        rows(list, vm)
                                    }
                                }
                                PickTab.FREQUENT -> if (s.frequent.isEmpty()) item { EmptyTab(stringResource(R.string.s13_empty_frequent)) } else rows(s.frequent, vm)
                                PickTab.FAVORITE -> if (s.favorites.isEmpty()) item { EmptyTab(stringResource(R.string.s13_empty_favorite)) } else rows(s.favorites, vm)
                            }
                        }
                    }
                }
            }
            if (results == null || results.isNotEmpty()) {
                // Chân ghim: Hỏi AI (GĐ4) · Nhập tay
                Row(
                    Modifier.fillMaxWidth().background(cs.surfaceContainer).navigationBarsPadding().imePadding().padding(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    FilledTonalButton(
                        onClick = { onAskAi(s.query.trim()) },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s13_ask_ai), style = MaterialTheme.typography.titleSmall)
                    }
                    OutlinedButton(
                        onClick = vm::openManual,
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cs.outlineVariant)),
                    ) {
                        Icon(Icons.Rounded.EditNote, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s13_manual), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp))
    }
}

private fun LazyListScope.rows(list: List<PickRow>, vm: LogFoodViewModel) {
    items(list, key = { it.key }) { r -> PickRowView(r, onClick = { vm.pick(r) }) }
}

@Composable
private fun DayHeader(d: LocalDate, today: LocalDate) {
    Text(
        when (d) {
            today -> stringResource(R.string.today)
            today.minusDays(1) -> stringResource(R.string.yesterday)
            else -> VnFormat.weekdayDayMonth(d)
        },
        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Spacing.lg, top = Spacing.md, bottom = Spacing.xs).semantics { heading() },
    )
}

@Composable
private fun PickRowView(r: PickRow, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 64.dp).padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                HighlightedName(r.name, r.match)
                MacroDots(r.values.proteinG, r.values.carbG, r.values.fatG, prefix = r.portion, modifier = Modifier.padding(top = 2.dp))
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(VnFormat.int(r.values.kcal.roundToInt()), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = cs.onSurface)
            Text(" kcal", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant, modifier = Modifier.padding(start = Spacing.sm))
        }
        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.55f))
    }
}

@Composable
private fun EmptyTab(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(Spacing.gutter))
}

@Composable
private fun NoMatch(query: String, onManual: () -> Unit, onAi: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(stringResource(R.string.s13_no_match, query.trim()), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
        Text(stringResource(R.string.s13_no_match_body), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.sm))
        FilledTonalButton(onClick = onAi, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.s13_ask_ai), style = MaterialTheme.typography.titleSmall)
        }
        OutlinedButton(onClick = onManual, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Icon(Icons.Rounded.EditNote, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.s13_manual), style = MaterialTheme.typography.titleSmall)
        }
    }
}

// ---------------- Nhập tay ----------------

@Composable
private fun ManualScreen(s: LogUi, vm: LogFoodViewModel, onTime: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SheetHeader(stringResource(R.string.manual_title), subtitle(s), Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back), onNav = { vm.back() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.gutter)) {
            MealTimeChip(s.mealTime, s.today, onTime, Modifier.padding(top = Spacing.xs, bottom = Spacing.md))
            FoodFields(s.manual, vm::updateManual, showErrors = s.manualShowErrors)
            Spacer(Modifier.height(Spacing.xl))
        }
        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).navigationBarsPadding().imePadding().padding(Spacing.lg)) {
            Button(onClick = vm::submitManual, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(R.string.action_continue), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(Spacing.sm))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
            }
        }
    }
}

// ---------------- S12 không AI ----------------

@Composable
private fun ConfirmScreen(s: LogUi, vm: LogFoodViewModel, snack: SnackbarHostState, onClose: () -> Unit, onTime: () -> Unit, onRetake: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val scope = rememberCoroutineScope()
    val ctx = LocalResources.current
    val total = s.items.sumOf { it.values.kcal }
    val mealName = stringResource(s.mealTime.meal.label())
    Column(Modifier.fillMaxSize()) {
        SheetHeader(stringResource(R.string.s12_title), null, Icons.Rounded.Close, stringResource(R.string.action_close), onNav = onClose)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            item(key = "head") {
                if (s.ai?.noFood == true && s.items.isEmpty()) {
                    NoFoodCard(s.ai.comment, onRetake = onRetake, onManual = vm::openManual)
                    return@item
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThumbOrIcon(s.ai?.thumbnail)
                    Spacer(Modifier.width(Spacing.lg))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(VnFormat.int(total.roundToInt()), style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight(650)), color = cs.onSurface)
                            Text(" kcal", style = MaterialTheme.typography.titleSmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        val p = s.items.sumOf { it.values.proteinG }
                        val c = s.items.sumOf { it.values.carbG }
                        val f = s.items.sumOf { it.values.fatG }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MacroLabel(stringResource(R.string.macro_protein), p, cc.protein)
                            MacroLabel(stringResource(R.string.macro_carb), c, cc.carb)
                            MacroLabel(stringResource(R.string.macro_fat), f, cc.fat, unit = " g")
                        }
                    }
                }
            }
            if (s.ai?.noFood == true && s.items.isEmpty()) return@LazyColumn
            item(key = "chip") { MealTimeChip(s.mealTime, s.today, onTime) }
            s.ai?.let { ai ->
                if (ai.question != null) item(key = "q") { QuestionCard(ai, onSend = vm::answer, onSkip = vm::skipQuestion) }
                val note = ai.answerNote ?: ai.comment
                if (note != null) item(key = "comment") { AiComment(note) }
                if (ai.question == null) item(key = "fix") { CorrectCard(ai, onSend = vm::correct) }
            }
            s.projection?.let { p ->
                item(key = "proj") {
                    ProjectionBar(
                        title = if (s.mealTime.date == s.today) stringResource(R.string.s12_after_save) else stringResource(R.string.s12_after_save_day, VnFormat.dayMonth(s.mealTime.date)),
                        projection = p,
                        targetKcal = s.day?.goal?.targetKcal,
                        baseParts = s.day?.summary?.segments.orEmpty(),
                        pendingMeal = s.mealTime.meal,
                    )
                }
            }
            item(key = "count") {
                Text(stringResource(R.string.s12_count, s.items.size), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.padding(top = Spacing.md).semantics { heading() })
            }
            if (s.items.isEmpty()) item { EmptyTab(stringResource(R.string.s12_empty)) }
            items(s.items, key = { it.key }) { item ->
                SwipeToDelete(onDelete = {
                    vm.remove(item.key)?.let { removed ->
                        scope.launch {
                            val r = snack.showSnackbar(ctx.getString(R.string.s10_deleted, removed.name), ctx.getString(R.string.action_undo))
                            if (r == SnackbarResult.ActionPerformed) vm.undoRemove()
                        }
                    }
                }, modifier = Modifier.clip(MaterialTheme.shapes.large)) {
                    DraftCard(item, onFactor = { vm.setFactor(item.key, it) }, onBookmark = { vm.toggleBookmark(item.key) }) { AiItemNotes(item) }
                }
            }
            item(key = "add") {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(top = Spacing.xs)) {
                    OutlinedButton(onClick = { vm.addMore(false) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s12_add_item), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                    OutlinedButton(onClick = { vm.addMore(true) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s12_from_my_foods), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                }
            }
        }
        if (s.ai?.noFood == true && s.items.isEmpty()) return@Column
        BottomSaveBar(
            label = stringResource(R.string.s12_save_to_meal, mealName),
            trailing = VnFormat.int(total.roundToInt()) + " kcal",
            enabled = s.items.isNotEmpty(),
            onClick = vm::save,
        )
    }
}

@Composable
private fun MacroLabel(name: String, g: Double, color: androidx.compose.ui.graphics.Color, unit: String = "") {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
    Spacer(Modifier.width(4.dp))
    Text("$name ", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
    Text(g.roundToInt().toString() + unit, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = cs.onSurface)
    Spacer(Modifier.width(Spacing.md))
}


// ---------------- S12 phần AI ----------------

@Composable
private fun ThumbOrIcon(jpeg: ByteArray?) {
    val cs = MaterialTheme.colorScheme
    val bmp = remember(jpeg) { jpeg?.let { runCatching { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }.getOrNull() } }
    Box(Modifier.size(72.dp).clip(MaterialTheme.shapes.large).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
        if (bmp != null) {
            androidx.compose.foundation.Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Rounded.Restaurant, null, tint = cs.onPrimaryContainer, modifier = Modifier.size(32.dp))
        }
    }
}

/** Câu hỏi làm rõ (s12 f3): hộp hổ phách, ô trả lời + gửi, dòng "bỏ qua và lưu luôn". */
@Composable
private fun QuestionCard(ai: AiPanel, onSend: (String) -> Unit, onSkip: () -> Unit) {
    val cc = CaliTheme.colors
    val cs = MaterialTheme.colorScheme
    var answer by remember(ai.question) { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cc.warnContainer).padding(Spacing.lg)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.AutoMirrored.Rounded.HelpOutline, null, tint = cc.onWarnContainer, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(ai.question.orEmpty(), style = MaterialTheme.typography.titleSmall, color = cc.onWarnContainer, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(Spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedTextField(
                value = answer,
                onValueChange = { answer = it.take(200) },
                placeholder = { Text(stringResource(R.string.s12ai_answer_hint)) },
                singleLine = true,
                enabled = !ai.answering,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend(answer) }),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedContainerColor = cs.surface, unfocusedContainerColor = cs.surface),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Spacing.sm))
            if (ai.answering) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
            } else {
                androidx.compose.material3.FilledTonalIconButton(onClick = { onSend(answer) }, enabled = answer.isNotBlank(), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.Send, stringResource(R.string.s12ai_send))
                }
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        if (ai.answerError != null) {
            Text(stringResource(aiErrorText(ai.answerError)), style = MaterialTheme.typography.bodySmall, color = cc.onWarnContainer)
        }
        Text(
            stringResource(R.string.s12ai_skip),
            style = MaterialTheme.typography.bodySmall, color = cc.onWarnContainer,
            modifier = Modifier.clickable(onClick = onSkip).padding(vertical = Spacing.xs),
        )
    }
}

/** "Kết quả chưa đúng? Nhờ AI sửa" (09 §8 G6-18): thu gọn thành một dòng, mở ra ô nhập + gửi. */
@Composable
private fun CorrectCard(ai: AiPanel, onSend: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    // Sửa xong thì thu gọn và xoá chữ đã gõ.
    androidx.compose.runtime.LaunchedEffect(ai.corrections) { if (ai.corrections > 0) { open = false; text = "" } }
    if (!open) {
        Row(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable { open = true }.heightIn(min = 48.dp).padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Edit, null, tint = cs.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(
                stringResource(if (ai.corrections > 0) R.string.s12ai_fix_again else R.string.s12ai_fix_open),
                style = MaterialTheme.typography.labelLarge, color = cs.primary,
            )
        }
        return
    }
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.lg)) {
        Text(stringResource(R.string.s12ai_fix_title), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
        Text(stringResource(R.string.s12ai_fix_note), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(Spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(200) },
                placeholder = { Text(stringResource(R.string.s12ai_fix_hint)) },
                enabled = !ai.correcting,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend(text) }),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedContainerColor = cs.surface, unfocusedContainerColor = cs.surface),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Spacing.sm))
            if (ai.correcting) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
            } else {
                androidx.compose.material3.FilledTonalIconButton(onClick = { onSend(text) }, enabled = text.isNotBlank(), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.Send, stringResource(R.string.s12ai_fix_send))
                }
            }
        }
        if (ai.correcting) {
            Text(stringResource(R.string.s12ai_fix_working), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = Spacing.sm))
        }
        if (ai.correctError != null) {
            Text(
                stringResource(R.string.s12ai_fix_error, stringResource(aiErrorText(ai.correctError))),
                style = MaterialTheme.typography.bodySmall, color = cs.error, modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        Text(
            stringResource(R.string.s12ai_fix_cancel),
            style = MaterialTheme.typography.labelLarge, color = cs.primary,
            modifier = Modifier.padding(top = Spacing.sm).clip(MaterialTheme.shapes.small).clickable(enabled = !ai.correcting) { open = false }
                .heightIn(min = 48.dp).padding(vertical = Spacing.md, horizontal = Spacing.xs),
        )
    }
}

@Composable
private fun AiComment(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.md), verticalAlignment = Alignment.Top) {
        Icon(Icons.Rounded.AutoAwesome, null, tint = cs.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
    }
}

/** Nhãn "Cần kiểm tra" + giả định của AI + cờ số liệu (Amber Check, không chặn Lưu). */
@Composable
private fun AiItemNotes(item: DraftItem) {
    if (item.confidence == null) return
    val cc = CaliTheme.colors
    val cs = MaterialTheme.colorScheme
    val low = item.confidence < com.cali.core.ai.AiReview.LOW_CONFIDENCE
    Column(Modifier.padding(top = Spacing.xs, end = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (low) Pill(Icons.AutoMirrored.Rounded.HelpOutline, stringResource(R.string.s12ai_needs_check))
        if (com.cali.core.calc.AmberFlag.MACRO_MISMATCH in item.flags) Pill(Icons.Rounded.Warning, stringResource(R.string.amber_mismatch))
        if (com.cali.core.calc.AmberFlag.VERY_HIGH in item.flags) Pill(Icons.Rounded.Warning, stringResource(R.string.s12ai_very_high))
        if (com.cali.core.calc.AmberFlag.INVALID_NUMBER_FIXED in item.flags) Pill(Icons.Rounded.Warning, stringResource(R.string.s12ai_fixed))
        val a = item.assumptions?.trim()
        if (!a.isNullOrEmpty() && (low || item.flags.isNotEmpty())) {
            Row {
                Icon(Icons.Rounded.Info, null, tint = cc.warn, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.s12ai_assumption, a), style = MaterialTheme.typography.bodySmall, color = cs.onSurface)
            }
        }
    }
}

@Composable
private fun Pill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val cc = CaliTheme.colors
    Row(
        Modifier.clip(MaterialTheme.shapes.small).background(cc.warnContainer).padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = cc.onWarnContainer, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(Spacing.xs))
        Text(text, style = MaterialTheme.typography.labelLarge, color = cc.onWarnContainer)
    }
}

/** s12 f7: không nhận ra món → nhận xét của AI + Chụp lại / Nhập tay. */
@Composable
private fun NoFoodCard(comment: String?, onRetake: () -> Unit, onManual: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.NoFood, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(Spacing.sm))
        Text(stringResource(R.string.s12ai_no_food), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
        if (!comment.isNullOrBlank()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(stringResource(R.string.s12ai_quote, comment), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        Spacer(Modifier.height(Spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FilledTonalButton(onClick = onRetake, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                Icon(Icons.Rounded.PhotoCamera, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s12ai_retake), style = MaterialTheme.typography.titleSmall)
            }
            OutlinedButton(onClick = onManual, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                Icon(Icons.Rounded.EditNote, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s13_manual), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/** Câu báo lỗi AI dùng chung (S11, S12, S20). */
fun aiErrorText(type: com.cali.core.ai.AiErrorType): Int = when (type) {
    com.cali.core.ai.AiErrorType.NO_NETWORK -> R.string.ai_err_network
    com.cali.core.ai.AiErrorType.TIMEOUT -> R.string.ai_err_timeout
    com.cali.core.ai.AiErrorType.NO_KEY -> R.string.ai_err_no_key
    com.cali.core.ai.AiErrorType.INVALID_KEY -> R.string.ai_err_key
    com.cali.core.ai.AiErrorType.RATE_LIMITED, com.cali.core.ai.AiErrorType.OVERLOADED -> R.string.ai_err_busy
    com.cali.core.ai.AiErrorType.QUOTA -> R.string.ai_err_quota
    com.cali.core.ai.AiErrorType.REFUSED -> R.string.ai_err_refused
    com.cali.core.ai.AiErrorType.INVALID_RESPONSE -> R.string.ai_err_invalid
    com.cali.core.ai.AiErrorType.UNKNOWN -> R.string.ai_err_unknown
}

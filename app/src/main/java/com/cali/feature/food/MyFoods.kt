package com.cali.feature.food

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.data.db.TextNormalizer
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.repo.MyFoodRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.component.HighlightedName
import com.cali.core.ui.component.MacroDots
import com.cali.core.ui.component.SwipeToDelete
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

enum class MyFoodSort { FREQUENT, RECENT, AZ }

data class MyFoodsUi(
    val all: List<MyFoodEntity>,
    val favorites: List<MyFoodEntity>,
    val others: List<MyFoodEntity>,
    val query: String,
    val sort: MyFoodSort,
    val today: LocalDate,
)

@HiltViewModel
class MyFoodsViewModel @Inject constructor(
    private val repo: MyFoodRepository,
    private val undo: UndoCenter,
    private val clock: Clock,
    @dagger.hilt.android.qualifiers.ApplicationContext private val app: android.content.Context,
) : ViewModel() {
    val query = MutableStateFlow("")
    val sort = MutableStateFlow(MyFoodSort.FREQUENT)
    val messages = undo.messages

    val ui: StateFlow<MyFoodsUi?> = combine(repo.observeAll(), query, sort) { all, q, s ->
        val nq = TextNormalizer.normalize(q)
        val filtered = if (nq.isEmpty()) all else all.filter { it.nameNormalized.contains(nq) }
        val sorted = when (s) {
            MyFoodSort.FREQUENT -> filtered.sortedWith(compareByDescending<MyFoodEntity> { it.useCount }.thenByDescending { it.lastUsedAt ?: 0 }.thenBy { it.name })
            MyFoodSort.RECENT -> filtered.sortedWith(compareByDescending<MyFoodEntity> { it.lastUsedAt ?: it.createdAt }.thenBy { it.name })
            MyFoodSort.AZ -> filtered.sortedBy { TextNormalizer.normalize(it.name) }
        }
        MyFoodsUi(all, sorted.filter { it.isFavorite }, sorted.filterNot { it.isFavorite }, q, s, LocalDate.now(clock))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite(f: MyFoodEntity) = viewModelScope.launch { repo.toggleFavorite(f.id) }

    fun delete(f: MyFoodEntity) = viewModelScope.launch {
        repo.delete(f.id).onSuccess { d -> undo.post(UiMessage(R.string.s34_deleted, listOf(f.name)) { repo.restore(d) }) }
    }

    fun lastUsedLabel(f: MyFoodEntity, today: LocalDate): String? {
        val at = f.lastUsedAt ?: return null
        val t = LocalDateTime.ofInstant(Instant.ofEpochMilli(at), clock.zone)
        return when (t.toLocalDate()) {
            today -> app.getString(R.string.s34_last_today, "%02d:%02d".format(t.hour, t.minute))
            today.minusDays(1) -> app.getString(R.string.s34_last_yesterday)
            else -> VnFormat.dayMonth(t.toLocalDate())
        }
    }
}

/** S34 Món của tôi. */
@Composable
fun MyFoodsRoute(onBack: () -> Unit, onOpen: (id: Long?) -> Unit, vm: MyFoodsViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    val s = ui ?: return
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.s34_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
            }
            if (s.all.isEmpty()) {
                EmptyMyFoods()
            } else {
                TextField(
                    value = s.query,
                    onValueChange = { vm.query.value = it.take(60) },
                    placeholder = { Text(stringResource(R.string.s13_search_hint, s.all.size)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = if (s.query.isNotEmpty()) { { IconButton(onClick = { vm.query.value = "" }) { Icon(Icons.Rounded.Close, stringResource(R.string.action_close)) } } } else null,
                    singleLine = true,
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        unfocusedContainerColor = cs.surfaceContainerHighest, focusedContainerColor = cs.surfaceContainerHighest,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md).heightIn(min = 56.dp),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter, vertical = Spacing.md)) {
                    MyFoodSort.entries.forEachIndexed { i, t ->
                        SegmentedButton(
                            selected = s.sort == t, onClick = { vm.sort.value = t },
                            shape = SegmentedButtonDefaults.itemShape(i, MyFoodSort.entries.size), icon = {},
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(stringResource(when (t) { MyFoodSort.FREQUENT -> R.string.s34_sort_frequent; MyFoodSort.RECENT -> R.string.s34_sort_recent; MyFoodSort.AZ -> R.string.s34_sort_az }), maxLines = 1)
                        }
                    }
                }
                if (s.favorites.isEmpty() && s.others.isEmpty()) {
                    Text(stringResource(R.string.s34_no_match, s.query.trim()), style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant, modifier = Modifier.padding(Spacing.gutter))
                }
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    if (s.favorites.isNotEmpty()) {
                        item(key = "hf") { Section(stringResource(R.string.s34_favorites), stringResource(R.string.s34_favorites_hint)) }
                        groupItems(s.favorites, s, vm, onOpen)
                    }
                    if (s.others.isNotEmpty()) {
                        item(key = "ho") {
                            Section(
                                if (s.favorites.isEmpty()) stringResource(R.string.s34_title) else stringResource(R.string.s34_others, s.others.size),
                                stringResource(when (s.sort) { MyFoodSort.FREQUENT -> R.string.s34_others_frequent; MyFoodSort.RECENT -> R.string.s34_others_recent; MyFoodSort.AZ -> R.string.s34_others_az }),
                            )
                        }
                        groupItems(s.others, s, vm, onOpen)
                    }
                }
            }
        }
        Column(Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(Spacing.lg), horizontalAlignment = Alignment.End) {
            ExtendedFloatingActionButton(
                onClick = { onOpen(null) },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text(stringResource(R.string.s34_add), style = MaterialTheme.typography.labelLarge) },
                containerColor = cs.primaryContainer, contentColor = cs.onPrimaryContainer,
            )
            SnackbarHost(snack)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.groupItems(list: List<MyFoodEntity>, s: MyFoodsUi, vm: MyFoodsViewModel, onOpen: (Long?) -> Unit) {
    itemsIndexed(list, key = { _, f -> f.id }) { i, f ->
        val r = 16.dp
        val shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = if (i == 0) r else 0.dp, topEnd = if (i == 0) r else 0.dp,
            bottomStart = if (i == list.lastIndex) r else 0.dp, bottomEnd = if (i == list.lastIndex) r else 0.dp,
        )
        Box(Modifier.padding(horizontal = Spacing.md)) {
            SwipeToDelete(onDelete = { vm.delete(f) }, modifier = Modifier.clip(shape)) {
                MyFoodRow(f, s, vm, onClick = { onOpen(f.id) })
            }
        }
    }
}

@Composable
private fun Section(title: String, hint: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.lg, bottom = Spacing.sm), verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
        Text(hint, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
    }
}

@Composable
private fun MyFoodRow(f: MyFoodEntity, s: MyFoodsUi, vm: MyFoodsViewModel, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.background(cs.surfaceContainerLow)) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 72.dp).padding(start = Spacing.lg, top = Spacing.sm, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                HighlightedName(f.name, TextNormalizer.matchRange(f.name, s.query))
                MacroDots(f.proteinG, f.carbG, f.fatG, prefix = listOfNotNull(f.portionDesc, f.grams?.let { "~" + VnFormat.int(it.roundToInt()) + " g" }).joinToString(" · ").ifBlank { null }, modifier = Modifier.padding(top = 2.dp))
                val last = vm.lastUsedLabel(f, s.today)
                Text(
                    if (f.useCount > 0 && last != null) stringResource(R.string.s34_uses_last, f.useCount, last) else stringResource(R.string.s34_uses_never),
                    style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(VnFormat.int(f.kcal.roundToInt()), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = cs.onSurface)
            Text(" kcal", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            IconButton(onClick = { vm.toggleFavorite(f) }, modifier = Modifier.size(48.dp)) {
                Icon(
                    if (f.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    stringResource(if (f.isFavorite) R.string.s34_star_on else R.string.s34_star_off, f.name),
                    tint = if (f.isFavorite) cs.primary else cs.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.55f))
    }
}

@Composable
private fun EmptyMyFoods() {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(Spacing.xl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Spacer(Modifier.height(48.dp))
        Box(Modifier.size(72.dp).clip(MaterialTheme.shapes.large).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.BookmarkAdd, null, tint = cs.onPrimaryContainer, modifier = Modifier.size(32.dp))
        }
        Text(stringResource(R.string.s34_empty_title), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
        Text(stringResource(R.string.s34_empty_body), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ---------------- Món mới / Sửa món đã lưu ----------------

data class MyFoodEditUi(val original: MyFoodEntity?, val form: FoodFormState, val favorite: Boolean, val entryCount: Int, val showErrors: Boolean)

@HiltViewModel
class MyFoodEditViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repo: MyFoodRepository,
    private val undo: UndoCenter,
) : ViewModel() {
    private val id: Long = handle.get<Long>("id") ?: -1
    val ui = MutableStateFlow<MyFoodEditUi?>(null)
    val done = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val f = if (id > 0) repo.byId(id) else null
            ui.value = if (f == null) {
                MyFoodEditUi(null, FoodFormState(), false, 0, false)
            } else {
                MyFoodEditUi(f, FoodFormState.of(f.name, f.portionDesc, f.grams, com.cali.core.calc.FoodValues(f.kcal, f.proteinG, f.carbG, f.fatG)), f.isFavorite, repo.entryCount(f.id), false)
            }
        }
    }

    fun setForm(f: FoodFormState) { ui.value = ui.value?.copy(form = f) }
    fun setFavorite(b: Boolean) { ui.value = ui.value?.copy(favorite = b) }

    fun save() = viewModelScope.launch {
        val u = ui.value ?: return@launch
        val f = u.form
        if (!f.valid) {
            ui.value = u.copy(showErrors = true, form = f.copy(checked = true))
            return@launch
        }
        val v = f.values()
        val r = if (u.original == null) {
            repo.create(f.name, f.portion, f.gramsValue(), v, u.favorite).map { }
        } else {
            repo.update(
                u.original.copy(
                    name = f.name, portionDesc = f.portion.trim().ifBlank { null }, grams = f.gramsValue(),
                    kcal = v.kcal, proteinG = v.proteinG, carbG = v.carbG, fatG = v.fatG, isFavorite = u.favorite,
                ),
            )
        }
        r.onSuccess { done.value = true }
    }

    fun delete() = viewModelScope.launch {
        val o = ui.value?.original ?: return@launch
        repo.delete(o.id).onSuccess { d ->
            undo.post(UiMessage(R.string.s34_deleted, listOf(o.name)) { repo.restore(d) })
            done.value = true
        }
    }
}

@Composable
fun MyFoodEditRoute(onClose: () -> Unit, vm: MyFoodEditViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val done by vm.done.collectAsStateWithLifecycle()
    LaunchedEffect(done) { if (done) onClose() }
    val s = ui ?: return
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(cs.surface)) {
        SheetHeader(
            stringResource(if (s.original == null) R.string.s34_new_title else R.string.s34_edit_title), null,
            Icons.Rounded.Close, stringResource(R.string.action_close), onNav = onClose,
            action = if (s.original != null) { { IconButton(onClick = vm::delete) { Icon(Icons.Rounded.Delete, stringResource(R.string.action_delete)) } } } else null,
        )
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            FoodFields(s.form, vm::setForm, showErrors = s.showErrors)
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, tint = cs.primary)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.s34_favorite_field), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                    Text(stringResource(R.string.s34_favorite_sub), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Switch(checked = s.favorite, onCheckedChange = vm::setFavorite)
            }
            s.original?.let { o ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.History, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.s34_before, VnFormat.int(o.kcal.roundToInt())), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.md)) {
                    Icon(Icons.Rounded.Info, null, tint = cs.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.md))
                    Text(stringResource(R.string.s34_edit_note, s.entryCount), style = MaterialTheme.typography.bodySmall, color = cs.onSurface)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
        }
        Box(Modifier.fillMaxWidth().background(cs.surfaceContainer).navigationBarsPadding().imePadding().padding(Spacing.lg)) {
            Button(onClick = vm::save, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(if (s.original == null) R.string.s34_save_new else R.string.s14_save), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

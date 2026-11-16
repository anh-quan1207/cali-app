package com.cali.feature.food

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.FoodValues
import com.cali.core.calc.MealTypeResolver
import com.cali.core.calc.Projection
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.DayData
import com.cali.core.data.repo.DiaryRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.MealTime
import com.cali.core.ui.component.MealTimeChip
import com.cali.core.ui.component.MealTimeSheet
import com.cali.core.ui.component.ProjectionBar
import com.cali.core.ui.component.TimeSource
import com.cali.core.ui.component.dayLabel
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

data class EditUi(
    val original: FoodEntryEntity,
    val item: DraftItem,
    val form: FoodFormState,
    val mealTime: MealTime,
    val today: LocalDate,
    val day: DayData,
) {
    /** Món cũ có nằm trong ngày đang tính không (đổi ngày thì cộng như món mới). */
    private val sameDay get() = mealTime.date == original.date
    val projection get() = Projection.of(day.summary.totalKcal, if (sameDay) original.kcal else 0.0, item.values.kcal, day.goal?.targetKcal)
    val baseParts get() = day.summary.segments
}

/** S14 Sửa món: thẻ S12 cho một món đã lưu. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EditFoodViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val diary: DiaryRepository,
    settings: SettingsRepository,
    private val undo: UndoCenter,
) : ViewModel() {
    private val id: Long = handle.get<Long>("id") ?: -1
    private val original = MutableStateFlow<FoodEntryEntity?>(null)
    private val item = MutableStateFlow<DraftItem?>(null)
    private val form = MutableStateFlow(FoodFormState())
    private val mealTime = MutableStateFlow<MealTime?>(null)
    val missing = MutableStateFlow(false)
    val done = MutableStateFlow(false)
    var resolver = MealTypeResolver()
        private set

    init {
        viewModelScope.launch {
            resolver = MealTypeResolver(settings.current().mealTimes)
            val e = diary.byId(id) ?: run { missing.value = true; return@launch }
            original.value = e
            val v = FoodValues(e.kcal, e.proteinG, e.carbG, e.fatG)
            item.value = DraftItem(1, e.name, e.portionDesc, e.grams, v, source = e.source, myFoodId = e.myFoodId)
            form.value = FoodFormState.of(e.name, e.portionDesc, e.grams, v)
            val at = diary.toLocal(e.eatenAt)
            mealTime.value = MealTime(at.toLocalDate(), at.toLocalTime(), e.mealType, e.mealTypeManual, TimeSource.SAVED)
        }
    }

    val ui: StateFlow<EditUi?> = combine(original.filterNotNull(), item.filterNotNull(), form, mealTime.filterNotNull()) { o, i, f, m -> Quad(o, i, f, m) }
        .flatMapLatest { q -> diary.observeDay(q.m.date).map { day -> EditUi(q.o, q.i, q.f, q.m, diary.today(), day) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private data class Quad(val o: FoodEntryEntity, val i: DraftItem, val f: FoodFormState, val m: MealTime)

    fun setFactor(f: Double) = item.update { it?.copy(factor = f) }
    fun toggleBookmark() = item.update { it?.copy(bookmark = !it.bookmark) }
    fun setMealTime(m: MealTime) { mealTime.value = m }

    /** Sửa tên/kcal/macro: số mới thành khẩu phần chuẩn ×1. */
    fun setForm(f: FoodFormState) {
        form.value = f
        if (f.valid) {
            item.update { it?.copy(name = f.name.trim(), portion = f.portion.trim().ifBlank { null }, baseGrams = f.gramsValue(), base = f.values(), factor = 1.0) }
        }
    }

    fun save() = viewModelScope.launch {
        val o = original.value ?: return@launch
        val i = item.value ?: return@launch
        val m = mealTime.value ?: return@launch
        diary.update(o, LocalDateTime.of(m.date, m.time), m.meal, m.mealManual, i.values, i.name, i.portionLabel, i.grams).onSuccess {
            if (i.bookmark && o.myFoodId == null) diary.saveToMyFoods(o.id)
            undo.post(UiMessage(R.string.s10_logged, listOf(1, VnFormat.int(i.values.kcal.roundToInt()))) { diary.put(o) })
            done.value = true
        }
    }

    fun delete() = viewModelScope.launch {
        diary.delete(id).onSuccess { removed ->
            undo.post(UiMessage(R.string.s10_deleted, listOf(removed.name)) { diary.restore(removed) })
            done.value = true
        }
    }
}

@Composable
fun EditFoodRoute(onClose: () -> Unit, vm: EditFoodViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val done by vm.done.collectAsStateWithLifecycle()
    val missing by vm.missing.collectAsStateWithLifecycle()
    LaunchedEffect(done, missing) { if (done || missing) onClose() }
    val s = ui ?: return
    val cs = MaterialTheme.colorScheme
    var timeSheet by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val p = s.projection
    Column(Modifier.fillMaxSize().background(cs.surface)) {
        SheetHeader(
            stringResource(R.string.s14_title), null, Icons.Rounded.Close, stringResource(R.string.action_close), onNav = onClose,
            action = { IconButton(onClick = vm::delete) { Icon(Icons.Rounded.Delete, stringResource(R.string.action_delete), tint = cs.onSurface) } },
        )
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.gutter), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            MealTimeChip(s.mealTime, s.today, onClick = { timeSheet = true }, modifier = Modifier.padding(top = Spacing.xs))
            DraftCard(s.item, onFactor = vm::setFactor, onBookmark = vm::toggleBookmark)
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { expanded = !expanded }.heightIn(min = 48.dp).padding(horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.EditNote, null, tint = cs.primary)
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s14_edit_values), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = cs.primary, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = cs.primary)
            }
            AnimatedVisibility(expanded) { FoodFields(s.form, vm::setForm, showErrors = true) }
            val dayTitle = if (s.mealTime.date == s.today) stringResource(R.string.s12_after_save) else stringResource(R.string.s14_after_day, dayLabel(s.mealTime.date, s.today))
            val delta = (s.item.values.kcal - if (s.mealTime.date == s.original.date) s.original.kcal else 0.0).roundToInt()
            ProjectionBar(dayTitle, p, s.day.goal?.targetKcal, s.baseParts, s.mealTime.meal) {
                Row {
                    Text(
                        stringResource(R.string.s14_before, VnFormat.int(s.day.summary.totalKcal.roundToInt())),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f),
                    )
                    Text(stringResource(R.string.s14_delta, VnFormat.signedInt(delta)), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
        }
        Box { BottomSaveBar(stringResource(R.string.s14_save), VnFormat.int(s.item.values.kcal.roundToInt()) + " kcal", enabled = s.form.valid, onClick = vm::save) }
    }
    if (timeSheet) {
        MealTimeSheet(s.mealTime, vm.resolver, s.today, onDismiss = { timeSheet = false }, onDone = { vm.setMealTime(it); timeSheet = false })
    }
}

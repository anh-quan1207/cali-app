package com.cali.feature.weight

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.NutritionCalculator
import com.cali.core.calc.WeightInput
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.repo.RecalcProposal
import com.cali.core.data.repo.WeightRepository
import com.cali.core.data.repo.toInput
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.NumberField
import com.cali.core.ui.component.dayLabel
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import com.cali.feature.settings.ApplyReason
import com.cali.feature.settings.ApplySheet
import com.cali.feature.settings.PendingApply
import com.cali.feature.settings.ProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

data class WeighContext(
    val today: LocalDate,
    val profile: UserProfileEntity?,
    val existing: WeightEntryEntity?,
    val previous: WeightEntryEntity?,
)

/** Sau khi lưu mà cần hỏi tính lại mục tiêu: dữ liệu cho sheet Đang dùng / Mới. */
data class RecalcAsk(val pending: PendingApply, val data: ProfileData)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repo: WeightRepository,
    private val profileRepo: ProfileRepository,
    private val settings: SettingsRepository,
    private val undo: UndoCenter,
) : ViewModel() {
    val date = MutableStateFlow(profileRepo.today())
    private val info = MutableStateFlow<Pair<WeightEntryEntity?, WeightEntryEntity?>>(null to null)

    val context: StateFlow<WeighContext?> = combine(date, info, profileRepo.profile) { d, (ex, prev), p ->
        WeighContext(profileRepo.today(), p, ex, prev)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val ask = MutableStateFlow<RecalcAsk?>(null)
    private var lastKg = 0.0

    init {
        viewModelScope.launch { date.collect { d -> info.value = repo.byDate(d) to repo.previousBefore(d) } }
    }

    fun save(kg: Double, onDone: () -> Unit) = viewModelScope.launch {
        val d = date.value
        lastKg = kg
        repo.save(d, kg).onSuccess { out ->
            val applied = out.applied
            when {
                applied != null -> undo.post(UiMessage(R.string.weigh_goal_applied, listOf(VnFormat.int(applied.targetKcal))))
                out.proposal != null -> {
                    ask.value = buildAsk(out.proposal, out.entry)
                    return@launch
                }
                else -> undo.post(UiMessage(R.string.weigh_saved, listOf(VnFormat.kg(kg))))
            }
            onDone()
        }
    }

    private suspend fun buildAsk(p: RecalcProposal, entry: WeightEntryEntity): RecalcAsk? {
        val profile = profileRepo.profile.first() ?: return null
        val s = settings.current()
        val pending = PendingApply(profile.toInput(), s.kcalOverride, s.macroPctOverride, p.preview, ApplyReason.Profile)
        return RecalcAsk(pending, ProfileData(profile, entry, p.current, s, profileRepo.today()))
    }

    fun applyRecalc(onDone: () -> Unit) = viewModelScope.launch {
        repo.applyProposal().onSuccess { undo.post(UiMessage(R.string.weigh_goal_applied, listOf(VnFormat.int(it.targetKcal)))) }
        ask.value = null
        onDone()
    }

    fun skipRecalc(onDone: () -> Unit) {
        undo.post(UiMessage(R.string.weigh_saved, listOf(VnFormat.kg(lastKg))))
        ask.value = null
        onDone()
    }
}

/** Sheet "Ghi cân nặng" của S31 (GĐ3 chỉ có sheet, biểu đồ ở GĐ6). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightSheet(onDismiss: () -> Unit, vm: WeightViewModel = hiltViewModel()) {
    val ctx by vm.context.collectAsStateWithLifecycle()
    val ask by vm.ask.collectAsStateWithLifecycle()
    val c = ctx ?: return
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }
    var pickDate by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val date = vm.date.collectAsStateWithLifecycle().value
    val kg = WeightInput.parse(text)
    val valid = kg != null && WeightInput.inRange(kg)
    val suggestion = kg?.let(WeightInput::suggest)

    ask?.let { a ->
        ApplySheet(a.pending, a.data, onCancel = { vm.skipRecalc(onDismiss) }, onApply = { vm.applyRecalc(onDismiss) })
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.surfaceContainerLow,
    ) {
        LaunchedEffect(Unit) { focus.requestFocus() }
        Column(Modifier.padding(horizontal = Spacing.gutter).navigationBarsPadding().imePadding()) {
            Text(stringResource(R.string.weigh_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
            Spacer(Modifier.height(Spacing.lg))
            Row(
                Modifier.clip(MaterialTheme.shapes.small).background(cs.secondaryContainer).clickable { pickDate = true }
                    .heightIn(min = 40.dp).padding(start = Spacing.md, end = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = cs.onSecondaryContainer, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    if (date == c.today) stringResource(R.string.weigh_today_chip, VnFormat.weekdayDayMonth(date)) else dayLabel(date, c.today),
                    style = MaterialTheme.typography.labelLarge, color = cs.onSecondaryContainer,
                )
                Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSecondaryContainer)
            }
            Spacer(Modifier.height(Spacing.md))
            val support = if (valid && c.profile != null) {
                val bmi = NutritionCalculator.bmi(kg!!, c.profile.heightCm)
                val cls = stringResource(NutritionCalculator.bmiClass(bmi).label())
                val prev = c.previous
                if (prev != null) {
                    stringResource(R.string.weigh_support_prev, VnFormat.decimal1(bmi), cls, VnFormat.signedKg(kg - prev.weightKg), VnFormat.dayMonth(prev.date))
                } else {
                    stringResource(R.string.weigh_support, VnFormat.decimal1(bmi), cls)
                }
            } else {
                c.existing?.let { stringResource(R.string.weigh_existing, VnFormat.dayMonth(date), VnFormat.kg(it.weightKg)) }
            }
            NumberField(
                value = text,
                onValueChange = { text = it },
                label = stringResource(R.string.weigh_field),
                unit = stringResource(R.string.unit_kg),
                error = if (touched && kg != null && !valid) stringResource(R.string.weigh_range_error) else null,
                supporting = support,
                imeAction = ImeAction.Done,
                onDone = { touched = true },
                modifier = Modifier.focusRequester(focus).onFocusChanged { if (!it.isFocused && text.isNotEmpty()) touched = true },
            )
            if (touched && suggestion != null) {
                FilledTonalButton(
                    onClick = { text = VnFormat.kg(suggestion); touched = false },
                    modifier = Modifier.heightIn(min = 48.dp).padding(top = Spacing.xs),
                ) { Text(stringResource(R.string.weigh_suggest, VnFormat.kg(suggestion))) }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = Spacing.lg), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.titleSmall) }
                Spacer(Modifier.width(Spacing.sm))
                Button(
                    onClick = { if (valid) vm.save(kg!!, onDismiss) else touched = true },
                    enabled = kg != null,
                    shape = CircleShape,
                    modifier = Modifier.height(56.dp),
                ) {
                    Text(
                        stringResource(if (c.existing != null) R.string.weigh_replace else R.string.action_save),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = Spacing.sm),
                    )
                }
            }
        }
    }

    if (pickDate) {
        val dp = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(c.today)
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dp.selectedDateMillis?.let { vm.date.value = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    pickDate = false
                }) { Text(stringResource(R.string.action_done)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(dp) }
    }
}

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.calc.MealBoundary
import com.cali.core.calc.MealTimesRules
import com.cali.core.calc.MealTypeResolver
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.component.MealBadge
import com.cali.core.ui.component.label
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import com.cali.feature.stats.BackTitleBar
import com.cali.feature.water.TimePickSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

/** S44 Bữa ăn: giờ bắt đầu từng khung (05 §4.2), sửa được, kể cả trùng giờ. */
@HiltViewModel
class MealTimesViewModel @Inject constructor(private val settings: SettingsRepository, private val undo: UndoCenter) : ViewModel() {
    val times: StateFlow<List<MealBoundary>?> = settings.settings.map { it.mealTimes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages = undo.messages

    fun set(index: Int, time: LocalTime) = viewModelScope.launch {
        settings.update { s -> s.copy(mealTimes = MealTimesRules.withStart(s.mealTimes, index, time)) }
    }

    fun reset() = viewModelScope.launch {
        val before = settings.current().mealTimes
        settings.update { it.copy(mealTimes = MealTypeResolver.DEFAULT) }.onSuccess {
            undo.post(UiMessage(R.string.s44_reset_done) { settings.update { s -> s.copy(mealTimes = before) } })
        }
    }
}

@Composable
fun MealTimesRoute(onBack: () -> Unit, vm: MealTimesViewModel = hiltViewModel()) {
    val list by vm.times.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    val l = list ?: return
    val cs = MaterialTheme.colorScheme
    var editing by remember { mutableStateOf<Int?>(null) }
    val shadowed = MealTimesRules.shadowed(l)
    Box(Modifier.fillMaxSize().background(cs.surface).navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            BackTitleBar(stringResource(R.string.s44_title), onBack)
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(stringResource(R.string.s44_intro), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = Spacing.sm))
                Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow)) {
                    l.forEachIndexed { i, b ->
                        if (i > 0) HorizontalDivider(color = cs.outlineVariant)
                        val name = stringResource(b.meal.label())
                        val end = MealTimesRules.endOf(l, i).minusMinutes(1)
                        val cd = stringResource(R.string.s44_pick, name)
                        Column(
                            Modifier.fillMaxWidth().clickable { editing = i }.semantics(mergeDescendants = true) { contentDescription = cd }
                                .heightIn(min = 64.dp).padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MealBadge(b.meal, filled = true, size = 32.dp)
                                Spacer(Modifier.width(Spacing.md))
                                Column(Modifier.weight(1f)) {
                                    Text(name, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                                    Text(
                                        if (i in shadowed) stringResource(R.string.s44_starts, b.start.toString())
                                        else stringResource(R.string.s44_range, b.start.toString(), end.toString()),
                                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                                    )
                                }
                                Icon(Icons.Rounded.Schedule, null, tint = cs.onSurfaceVariant)
                            }
                            if (i in shadowed) {
                                val winner = l[MealTimesRules.winnerOf(l, i)]
                                val wName = stringResource(winner.meal.label())
                                Row(Modifier.padding(top = Spacing.sm), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Rounded.Warning, null, tint = CaliTheme.colors.warn, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(Spacing.sm))
                                    Text(stringResource(R.string.s44_same_time, wName), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = vm::reset, modifier = Modifier.padding(top = Spacing.sm).heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.s44_reset))
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }
    editing?.let { i ->
        val b = l[i]
        TimePickSheet(
            title = stringResource(R.string.s44_pick, stringResource(b.meal.label())),
            initial = b.start,
            onDismiss = { editing = null },
            onDone = { t -> vm.set(i, t); editing = null },
        )
    }
}

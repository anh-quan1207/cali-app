package com.cali.feature.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.notify.NotificationPermission
import com.cali.core.ui.component.hm
import com.cali.core.ui.format.VnFormat
import com.cali.feature.water.TimePickSheet
import com.cali.feature.water.boldValues
import com.cali.core.ui.theme.Spacing
import java.time.LocalTime

private enum class TimeField { START, END, WEEKLY }

/** S43 Nước & nhắc nhở (05 §2, mockup s40 khung S43). */
@Composable
fun WaterSettingsRoute(onBack: () -> Unit, vm: WaterSettingsViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    // Đã hỏi và bị từ chối → lần sau mở Cài đặt hệ thống thay vì hỏi lại.
    var denied by rememberSaveable { mutableStateOf(false) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!ok) denied = true
        vm.refresh()
    }
    val openSettings = {
        ctx.startActivity(
            Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, ctx.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
    val canAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !denied && !NotificationPermission.runtimeGranted(ctx)
    val requestPermission = { if (canAsk) ask.launch(Manifest.permission.POST_NOTIFICATIONS) else openSettings() }
    var timeField by remember { mutableStateOf<TimeField?>(null) }

    Column(Modifier.fillMaxSize().background(cs.surface)) {
        Row(Modifier.statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
            Spacer(Modifier.width(Spacing.xs))
            Text(stringResource(R.string.s43_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
        }
        val u = ui ?: return@Column
        val s = u.settings
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.md).navigationBarsPadding(),
        ) {
            if (!u.permitted) {
                Row(
                    Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(cs.secondaryContainer)
                        .padding(start = Spacing.lg, end = Spacing.xs, top = Spacing.sm, bottom = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.NotificationsOff, null, tint = cs.onSecondaryContainer, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.md))
                    Text(stringResource(R.string.s43_perm_strip), style = MaterialTheme.typography.bodySmall, color = cs.onSecondaryContainer, modifier = Modifier.weight(1f))
                    TextButton(onClick = requestPermission, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(if (canAsk) R.string.s43_perm_allow else R.string.s43_perm_settings), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(Spacing.md))
            }

            // ---- Mục tiêu nước ----
            SectionTitle(stringResource(R.string.s43_goal_group))
            Card {
                val auto = s.waterGoalOverrideMl == null
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = auto, onClick = { vm.setGoalAuto(true, u.goalMl) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2), modifier = Modifier.heightIn(min = 48.dp), icon = {},
                    ) { Text(stringResource(R.string.s43_goal_auto), style = MaterialTheme.typography.labelLarge) }
                    SegmentedButton(
                        selected = !auto, onClick = { if (auto) vm.setGoalAuto(false, u.autoGoal.takeIf { it > 0 } ?: 2000) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2), modifier = Modifier.heightIn(min = 48.dp), icon = {},
                    ) { Text(stringResource(R.string.s43_goal_custom), style = MaterialTheme.typography.labelLarge) }
                }
                if (auto) {
                    Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.Bottom) {
                        Text(VnFormat.int(u.goalMl), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.s43_per_day), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                    }
                    u.goal?.let { g ->
                        Line(
                            Icons.Rounded.Calculate,
                            AnnotatedString(stringResource(R.string.s43_goal_auto_line, VnFormat.decimal1(g.weightKgUsed), VnFormat.int(g.weightKgUsed * 35), VnFormat.dayMonth(g.effectiveFrom))),
                            neutral = true, top = 6.dp,
                        )
                    }
                } else {
                    MlField(
                        label = stringResource(R.string.s43_goal_custom_label), value = u.goalMl,
                        min = WaterSettingsViewModel.GOAL_MIN, max = WaterSettingsViewModel.GOAL_MAX,
                        onCommit = vm::setGoalCustom, modifier = Modifier.padding(top = Spacing.md).fillMaxWidth(),
                    )
                    Line(Icons.Rounded.Calculate, AnnotatedString(stringResource(R.string.s43_goal_custom_line, VnFormat.int(u.autoGoal))), neutral = true, top = 6.dp)
                }
            }

            // ---- Nút nhanh (một nút, G5-8) ----
            SectionTitle(stringResource(R.string.s43_cups_group))
            Card(top = Spacing.md) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    MlField(stringResource(R.string.s43_cup_small), s.waterCupSmallMl, WaterSettingsViewModel.CUP_MIN, WaterSettingsViewModel.CUP_MAX, vm::setCupSmall, Modifier.weight(1f))
                }
                Line(Icons.Rounded.TouchApp, AnnotatedString(stringResource(R.string.s43_cups_line, VnFormat.int(s.waterCupSmallMl))), neutral = true, top = 10.dp)
            }

            // ---- Nhắc uống nước ----
            SectionTitle(stringResource(R.string.s43_remind_group))
            Group {
                SwitchRow(stringResource(R.string.s43_remind_switch), stringResource(R.string.s43_remind_switch_sub), s.waterEnabled) { on ->
                    vm.setEnabled(on)
                    if (on && !u.permitted) requestPermission()
                }
                RowDivider()
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.s43_window), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface, modifier = Modifier.weight(1f))
                    TimeChip(s.waterStart, stringResource(R.string.s43_window_start_cd, s.waterStart.hm())) { timeField = TimeField.START }
                    Text(" – ", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    TimeChip(s.waterEnd, stringResource(R.string.s43_window_end_cd, s.waterEnd.hm())) { timeField = TimeField.END }
                }
                RowDivider()
                Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
                    Text(stringResource(R.string.s43_interval), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface, modifier = Modifier.padding(bottom = 10.dp))
                    val opts = WaterSettingsViewModel.INTERVALS
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        opts.forEachIndexed { i, m ->
                            SegmentedButton(
                                selected = s.waterIntervalMin == m, onClick = { vm.setInterval(m) },
                                shape = SegmentedButtonDefaults.itemShape(i, opts.size), modifier = Modifier.heightIn(min = 48.dp), icon = {},
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("$m", style = MaterialTheme.typography.labelLarge)
                                    Text(" " + stringResource(R.string.s43_minutes), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Line(
                        Icons.Rounded.Info,
                        AnnotatedString(stringResource(R.string.s43_interval_line, s.waterIntervalMin, VnFormat.int(u.goalMl), s.waterStart.hm())),
                        neutral = true, top = 10.dp,
                    )
                    when {
                        !s.waterEnabled -> Line(Icons.Outlined.NotificationsOff, AnnotatedString(stringResource(R.string.s43_off)), neutral = true)
                        !u.permitted -> Line(Icons.Outlined.NotificationsOff, boldValues(stringResource(R.string.s43_no_perm, "\u0001"), stringResource(R.string.s43_no_perm_b)), neutral = true)
                        u.next != null -> {
                            val tomorrow = u.next.toLocalDate().isAfter(u.today)
                            Line(Icons.Outlined.Notifications, boldValues(stringResource(if (tomorrow) R.string.s43_next_tomorrow else R.string.s43_next, "\u0001"), u.next.toLocalTime().hm()))
                        }
                    }
                }
            }

            // ---- Tổng kết tuần (GĐ5 chỉ lưu khoá; GĐ6 đặt lịch) ----
            SectionTitle(stringResource(R.string.s43_weekly_group))
            Group {
                SwitchRow(stringResource(R.string.s43_weekly_switch), stringResource(R.string.s43_weekly_switch_sub), s.weeklyNotifyEnabled, vm::setWeeklyEnabled)
                RowDivider()
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = Spacing.lg, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.s43_weekly_time), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                        val wkStart = u.nextMonday.minusWeeks(1)
                        Text(
                            stringResource(R.string.s43_weekly_next, VnFormat.dayMonth(u.nextMonday), VnFormat.dayMonth(wkStart), VnFormat.dayMonth(wkStart.plusDays(6))),
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        )
                    }
                    TimeChip(s.weeklyNotifyTime, stringResource(R.string.s43_weekly_time) + " " + s.weeklyNotifyTime.hm()) { timeField = TimeField.WEEKLY }
                }
                RowDivider()
                SwitchRow(stringResource(R.string.s43_weekly_ai), stringResource(R.string.s43_weekly_ai_sub), s.weeklyAiAuto, vm::setWeeklyAi)
            }
            Spacer(Modifier.height(Spacing.xl))
        }

        when (timeField) {
            TimeField.START -> {
                val same = stringResource(R.string.s43_window_same)
                TimePickSheet(stringResource(R.string.s43_window_start_title), s.waterStart, { timeField = null }, { vm.setStart(it); timeField = null }, error = { if (it == s.waterEnd) same else null })
            }
            TimeField.END -> {
                val same = stringResource(R.string.s43_window_same)
                TimePickSheet(stringResource(R.string.s43_window_end_title), s.waterEnd, { timeField = null }, { vm.setEnd(it); timeField = null }, error = { if (it == s.waterStart) same else null })
            }
            TimeField.WEEKLY -> TimePickSheet(stringResource(R.string.s43_weekly_time_title), s.weeklyNotifyTime, { timeField = null }, { vm.setWeeklyTime(it); timeField = null })
            null -> Unit
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = Spacing.sm, top = Spacing.lg, bottom = Spacing.md),
    )
}

@Composable
private fun Card(top: androidx.compose.ui.unit.Dp = Spacing.lg, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = Spacing.lg, end = Spacing.lg, top = top, bottom = Spacing.lg),
    ) { content() }
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)) { content() }
}

@Composable
private fun Line(icon: ImageVector, text: AnnotatedString, neutral: Boolean = false, top: androidx.compose.ui.unit.Dp = Spacing.sm) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.padding(top = top), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = if (neutral) cs.onSurfaceVariant else cs.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { onChange(!checked) }.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
        Spacer(Modifier.width(Spacing.md))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun TimeChip(t: LocalTime, cd: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.heightIn(min = 48.dp).clip(MaterialTheme.shapes.small).clickable(onClick = onClick).semantics(mergeDescendants = true) { contentDescription = cd },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.clip(MaterialTheme.shapes.small).background(cs.secondaryContainer).padding(horizontal = Spacing.md, vertical = 6.dp)) {
            Text(t.hm(), style = MaterialTheme.typography.labelLarge, color = cs.onSecondaryContainer)
        }
    }
}

/** Ô ml: lưu khi rời ô hoặc bấm Xong nếu hợp lệ; lỗi khoảng chỉ hiện khi rời ô. */
@Composable
private fun MlField(label: String, value: Int, min: Int, max: Int, onCommit: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    var text by remember(value) { mutableStateOf(value.toString()) }
    var focused by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val n = text.toIntOrNull()
    val valid = n != null && n in min..max
    val showError = touched && !focused && !valid
    fun commit() { if (valid && n != value) onCommit(n!!) }
    OutlinedTextField(
        value = text,
        onValueChange = { v -> text = v.filter(Char::isDigit).take(5) },
        label = { Text(label) },
        suffix = { Text(stringResource(R.string.s15_unit_ml)) },
        singleLine = true,
        isError = showError,
        textStyle = MaterialTheme.typography.titleLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
        supportingText = if (showError) {
            {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Error, null, tint = cs.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text(stringResource(R.string.s43_goal_error, VnFormat.int(min), VnFormat.int(max)))
                }
            }
        } else null,
        modifier = modifier.onFocusChanged { f ->
            if (focused && !f.isFocused) { touched = true; commit() }
            focused = f.isFocused
        },
    )
}

package com.cali.feature.settings

import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.data.db.TextNormalizer
import com.cali.core.data.prefs.SecureKeyStore
import com.cali.core.data.prefs.Settings
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.BackupError
import com.cali.core.data.repo.BackupRepository
import com.cali.core.data.repo.DeviceStats
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DataUi(val stats: DeviceStats, val settings: Settings, val today: LocalDate)

@HiltViewModel
class DataViewModel @Inject constructor(
    private val backup: BackupRepository,
    private val settings: SettingsRepository,
    private val keys: SecureKeyStore,
    private val undo: UndoCenter,
    private val clock: Clock,
    @dagger.hilt.android.qualifiers.ApplicationContext private val app: android.content.Context,
) : ViewModel() {
    val messages = undo.messages

    val ui: StateFlow<DataUi?> = combine(backup.stats(), settings.settings) { st, s -> DataUi(st, s, LocalDate.now(clock)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun suggestedName(): String = "cali-" + LocalDate.now(clock) + ".json"

    fun export(uri: Uri) = viewModelScope.launch {
        val name = displayName(uri) ?: suggestedName()
        val r = runCatching {
            withContext(Dispatchers.IO) { requireNotNull(app.contentResolver.openOutputStream(uri, "wt")) }.use { out -> backup.export(out, name).getOrThrow() }
        }
        undo.post(if (r.isSuccess) UiMessage(R.string.s45_exported, listOf(name)) else UiMessage(R.string.s45_export_failed))
    }

    fun import(uri: Uri, onRestored: () -> Unit) = viewModelScope.launch {
        val r = runCatching {
            withContext(Dispatchers.IO) { requireNotNull(app.contentResolver.openInputStream(uri)) }.use { backup.import(it) }
        }.getOrElse { Result.failure(BackupError.Unreadable(it)) }
        r.onSuccess { c ->
            undo.post(UiMessage(R.string.s45_imported, listOf(c.foodEntries, c.weights)))
            onRestored()
        }.onFailure { e ->
            undo.post(
                UiMessage(
                    when (e) {
                        is BackupError.NewerVersion -> R.string.s45_import_newer
                        is BackupError.NotCali -> R.string.s45_import_not_cali
                        else -> R.string.s45_import_bad
                    },
                ),
            )
        }
    }

    fun setThumbnails(on: Boolean) = viewModelScope.launch { settings.update { it.copy(saveThumbnails = on) } }

    fun wipe(onWiped: () -> Unit) = viewModelScope.launch {
        backup.wipeAll().onSuccess {
            keys.clear()
            onWiped()
        }
    }

    private fun displayName(uri: Uri): String? = runCatching {
        app.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }.getOrNull()

    fun lastExport(s: Settings, today: LocalDate): Pair<LocalDateTime, Long>? = s.lastBackupAt?.let {
        val t = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), clock.zone)
        t to ChronoUnit.DAYS.between(t.toLocalDate(), today)
    }
}

/** S45 Dữ liệu: sao lưu / khôi phục JSON, thống kê, ảnh thu nhỏ, Xoá toàn bộ 2 bước. */
@Composable
fun DataRoute(onBack: () -> Unit, onRestored: () -> Unit, onWiped: () -> Unit, vm: DataViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var wipeStep by remember { mutableStateOf(0) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> uri?.let(vm::export) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pendingImport = uri }
    val s = ui ?: return
    val cs = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.s45_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
            }
            // Sao lưu ra file
            Column(Modifier.padding(horizontal = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CloudUpload, null, tint = cs.onPrimaryContainer)
                    }
                    Spacer(Modifier.width(Spacing.lg))
                    Text(stringResource(R.string.s45_backup_title), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                }
                Spacer(Modifier.height(Spacing.md))
                val last = vm.lastExport(s.settings, s.today)
                InfoLine(
                    Icons.Rounded.History,
                    if (last == null) stringResource(R.string.s45_never_export) else {
                        val (t, days) = last
                        val ago = if (days <= 0) stringResource(R.string.s45_today_export) else stringResource(R.string.s45_days_ago, days.toInt())
                        stringResource(R.string.s45_last_export, VnFormat.date(t.toLocalDate()) + ", " + "%02d:%02d".format(t.hour, t.minute), ago) +
                            (s.settings.lastBackupFile?.let { "\n$it" } ?: "")
                    },
                )
                Spacer(Modifier.height(Spacing.sm))
                InfoLine(Icons.Rounded.Inventory2, stringResource(R.string.s45_what))
                Spacer(Modifier.height(Spacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    FilledTonalButton(onClick = { exportLauncher.launch(vm.suggestedName()) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                        Icon(Icons.Rounded.Download, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s45_export), style = MaterialTheme.typography.titleSmall)
                    }
                    OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                        Icon(Icons.Rounded.UploadFile, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s45_import), style = MaterialTheme.typography.titleSmall)
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                InfoLine(Icons.Rounded.Info, stringResource(R.string.s45_import_note))
            }
            // Trên máy
            Text(stringResource(R.string.s45_on_device), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.padding(start = Spacing.gutter, top = Spacing.xl, bottom = Spacing.sm))
            Row(Modifier.padding(horizontal = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(vertical = Spacing.md)) {
                Metric(stringResource(R.string.s45_days_logged), s.stats.daysLogged, Modifier.weight(1f))
                Metric(stringResource(R.string.s45_weights), s.stats.weights, Modifier.weight(1f))
                Metric(stringResource(R.string.s45_my_foods), s.stats.myFoods, Modifier.weight(1f))
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(
                Modifier.padding(horizontal = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow)
                    .clickable { vm.setThumbnails(!s.settings.saveThumbnails) }.heightIn(min = 72.dp).padding(horizontal = Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.s45_thumbs), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                    Text(stringResource(R.string.s45_thumbs_sub), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Switch(checked = s.settings.saveThumbnails, onCheckedChange = vm::setThumbnails)
            }
            Spacer(Modifier.height(Spacing.md))
            Box(Modifier.padding(horizontal = Spacing.gutter)) { InfoLine(Icons.Rounded.CloudQueue, stringResource(R.string.s45_auto_backup)) }
            // Xoá
            Text(stringResource(R.string.s45_wipe_group), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.padding(start = Spacing.gutter, top = Spacing.xl, bottom = Spacing.sm))
            Row(
                Modifier.padding(horizontal = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow)
                    .clickable { wipeStep = 1 }.heightIn(min = 72.dp).padding(horizontal = Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.DeleteForever, null, tint = cs.onSurfaceVariant)
                }
                Spacer(Modifier.width(Spacing.lg))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.s45_wipe), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Text(stringResource(R.string.s45_wipe_sub), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
            }
            Spacer(Modifier.height(Spacing.xl))
            Spacer(Modifier.navigationBarsPadding())
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            icon = { Icon(Icons.Rounded.UploadFile, null) },
            title = { Text(stringResource(R.string.s45_import_title), textAlign = TextAlign.Center) },
            text = { Text(stringResource(R.string.s45_import_body, s.stats.daysLogged, s.stats.weights, s.stats.myFoods)) },
            confirmButton = { TextButton(onClick = { pendingImport = null; vm.import(uri, onRestored) }) { Text(stringResource(R.string.s45_import_confirm)) } },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    when (wipeStep) {
        1 -> AlertDialog(
            onDismissRequest = { wipeStep = 0 },
            icon = { Icon(Icons.Rounded.DeleteForever, null) },
            title = { Text(stringResource(R.string.s45_wipe1_title), textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text(stringResource(R.string.s45_wipe1_body, s.stats.daysLogged, s.stats.weights, s.stats.myFoods))
                    val last = vm.lastExport(s.settings, s.today)
                    Text(
                        if (last == null) stringResource(R.string.s45_wipe1_no_backup) else stringResource(R.string.s45_wipe1_backup_hint, VnFormat.dayMonth(last.first.toLocalDate())),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedButton(onClick = { wipeStep = 0; exportLauncher.launch(vm.suggestedName()) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Icon(Icons.Rounded.Download, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s45_wipe1_export))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { wipeStep = 2 }) { Text(stringResource(R.string.action_continue)) } },
            dismissButton = { TextButton(onClick = { wipeStep = 0 }) { Text(stringResource(R.string.action_cancel)) } },
        )
        2 -> WipeConfirm(s.stats, onCancel = { wipeStep = 0 }, onConfirm = { wipeStep = 0; vm.wipe(onWiped) })
    }
}

/** Bước 2/2: gõ XOÁ (không dấu cũng được) mới bật nút filled error. */
@Composable
private fun WipeConfirm(stats: DeviceStats, onCancel: () -> Unit, onConfirm: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    val ok = TextNormalizer.normalize(text) == "xoa"
    val stepCd = stringResource(R.string.s45_wipe2_cd)
    AlertDialog(
        onDismissRequest = onCancel,
        modifier = Modifier.semantics { contentDescription = stepCd },
        icon = { Icon(Icons.Rounded.DeleteForever, null) },
        title = { Text(stringResource(R.string.s45_wipe2_title), textAlign = TextAlign.Center) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(stringResource(R.string.s45_wipe2_body, stats.daysLogged, stats.weights, stats.myFoods))
                OutlinedTextField(
                    value = text, onValueChange = { text = it.take(10) },
                    label = { Text(stringResource(R.string.s45_wipe2_field)) },
                    supportingText = { Text(stringResource(R.string.s45_wipe2_hint)) },
                    singleLine = true, shape = MaterialTheme.shapes.extraSmall,
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm, enabled = ok,
                colors = ButtonDefaults.buttonColors(containerColor = cs.error, contentColor = cs.onError),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Icon(Icons.Rounded.DeleteForever, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s45_wipe2_confirm), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val cs = MaterialTheme.colorScheme
    Row {
        Icon(icon, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
    }
}

@Composable
private fun Metric(label: String, value: Int, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = Spacing.lg)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        Text(VnFormat.int(value), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight(650)), color = cs.onSurface)
    }
}

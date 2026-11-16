package com.cali.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WifiTetheringError
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiException
import com.cali.core.ai.AiProvider
import com.cali.core.ai.SystemPromptSource
import com.cali.core.data.db.AppDatabase
import com.cali.core.data.db.dao.AiUsage
import com.cali.core.data.prefs.SecureKeyStore
import com.cali.core.data.prefs.Settings
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.component.CollectMessages
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ConnState {
    data object Idle : ConnState
    data object Testing : ConnState
    data class Ok(val at: LocalTime) : ConnState
    data class Failed(val type: AiErrorType, val message: String) : ConnState
}

data class AiSettingsUi(
    val settings: Settings,
    val hasKey: Boolean,
    val keyLast4: String?,
    val usage: AiUsage,
    val conn: ConnState,
)

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val keys: SecureKeyStore,
    private val provider: AiProvider,
    private val clock: Clock,
    private val undo: UndoCenter,
    db: AppDatabase,
) : ViewModel() {
    val messages = undo.messages
    private val last4 = MutableStateFlow<String?>(null)
    private val conn = MutableStateFlow<ConnState>(ConnState.Idle)
    private val monthStart = LocalDate.now(clock).withDayOfMonth(1).atStartOfDay(clock.zone).toInstant().toEpochMilli()

    val ui: StateFlow<AiSettingsUi?> = combine(
        settings.settings, keys.hasKey, last4, db.aiCallLogDao().observeUsageSince(monthStart), conn,
    ) { s, k, l4, u, c -> AiSettingsUi(s, k, l4, u, c) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { refreshLast4() }
    }

    /** Chỉ giữ 4 ký tự cuối trong bộ nhớ UI; key đầy đủ không bao giờ lên màn hình. */
    private suspend fun refreshLast4() {
        last4.value = keys.read().getOrNull()?.trim()?.takeLast(4)
    }

    /** Lưu key mới (nếu có) rồi gọi GET /v1/models. */
    fun test(newKey: String, onSaved: () -> Unit) = viewModelScope.launch {
        conn.value = ConnState.Testing
        if (newKey.isNotBlank()) {
            keys.save(newKey.trim()).onSuccess {
                refreshLast4()
                onSaved()
            }
        }
        conn.value = provider.testConnection().fold(
            onSuccess = { ConnState.Ok(LocalTime.now(clock).withSecond(0).withNano(0)) },
            onFailure = { e ->
                val type = (e as? AiException)?.type ?: AiErrorType.UNKNOWN
                ConnState.Failed(type, e.message.orEmpty())
            },
        )
    }

    fun saveKeyQuietly(newKey: String) = viewModelScope.launch {
        if (newKey.isNotBlank()) keys.save(newKey.trim()).onSuccess { refreshLast4() }
    }

    fun setVisionModel(v: String) = viewModelScope.launch { settings.update { it.copy(aiModelVision = v.trim().ifEmpty { Settings().aiModelVision }) } }
    fun setChatModel(v: String) = viewModelScope.launch { settings.update { it.copy(aiModelChat = v.trim().ifEmpty { Settings().aiModelChat }) } }
    fun setNotes(v: String) = viewModelScope.launch { settings.update { it.copy(personalNotes = v.take(NOTES_MAX)) } }
    fun keySavedMessage() = undo.post(UiMessage(R.string.s42_saved_key))

    companion object {
        const val NOTES_MAX = 500
    }
}

/** S42 Cài đặt AI (s40 f7, f8). */
@Composable
fun AiSettingsRoute(onBack: () -> Unit, onOpenPrompt: () -> Unit, vm: AiSettingsViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    CollectMessages(vm.messages, snack)
    var newKey by rememberSaveable { mutableStateOf("") }
    // Rời màn khi đã dán key mà chưa bấm Kiểm tra → vẫn lưu.
    val latestKey = androidx.compose.runtime.rememberUpdatedState(newKey)
    DisposableEffect(Unit) { onDispose { vm.saveKeyQuietly(latestKey.value) } }
    val s = ui ?: return
    val cs = MaterialTheme.colorScheme

    Scaffold(containerColor = cs.surface, snackbarHost = { SnackbarHost(snack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
            Row(Modifier.height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.s42_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
            }
            Card {
                ProviderField()
                Spacer(Modifier.height(Spacing.lg))
                KeyField(
                    value = newKey,
                    onChange = { newKey = it.trim() },
                    last4 = s.keyLast4,
                    invalid = (s.conn as? ConnState.Failed)?.type == AiErrorType.INVALID_KEY,
                )
                Spacer(Modifier.height(Spacing.lg))
                FilledTonalButton(
                    onClick = { vm.test(newKey) { newKey = ""; vm.keySavedMessage() } },
                    enabled = s.conn != ConnState.Testing,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    if (s.conn == ConnState.Testing) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(if (s.conn is ConnState.Failed) Icons.Rounded.WifiTetheringError else Icons.Rounded.Check, null, Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(if (s.conn == ConnState.Testing) R.string.s42_testing else R.string.s42_test), style = MaterialTheme.typography.titleSmall)
                }
                ConnLine(s.conn)
            }

            Section(stringResource(R.string.s42_models))
            Card {
                val d = Settings()
                ModelField(
                    label = stringResource(R.string.s42_model_vision),
                    value = s.settings.aiModelVision,
                    sub = if (s.settings.aiModelVision == d.aiModelVision) stringResource(R.string.s42_model_vision_sub_default)
                    else stringResource(R.string.s42_model_vision_sub_custom, d.aiModelVision),
                    onCommit = vm::setVisionModel,
                )
                Spacer(Modifier.height(Spacing.md))
                ModelField(
                    label = stringResource(R.string.s42_model_chat),
                    value = s.settings.aiModelChat,
                    sub = if (s.settings.aiModelChat == d.aiModelChat) stringResource(R.string.s42_model_chat_sub_default)
                    else stringResource(R.string.s42_model_chat_sub_custom, d.aiModelChat),
                    onCommit = vm::setChatModel,
                )
            }

            Section(stringResource(R.string.s42_instructions))
            Column(Modifier.padding(horizontal = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow)) {
                Row(
                    Modifier.fillMaxWidth().clickable(onClick = onOpenPrompt).heightIn(min = 64.dp).padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.s42_prompt), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                        Text(
                            stringResource(
                                if (s.settings.systemPromptOverride.isNullOrBlank()) R.string.s42_prompt_default else R.string.s42_prompt_edited,
                                SystemPromptSource.DEFAULT_VERSION,
                            ),
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            Card { NotesField(s.settings.personalNotes, vm::setNotes) }

            Section(stringResource(R.string.s42_usage))
            Card {
                val u = s.usage
                Text(
                    if (u.calls == 0) stringResource(R.string.s42_usage_none)
                    else stringResource(
                        R.string.s42_usage_line, u.calls, VnFormat.int(u.inputTokens.toInt()), VnFormat.int(u.outputTokens.toInt()),
                        VnFormat.decimal1(u.avgDurationMs / 1000.0),
                    ) + if (u.failures > 0) " · " + stringResource(R.string.s42_usage_failures, u.failures) else "",
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurface,
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            FootLine(Icons.Rounded.Insights, stringResource(R.string.s42_weekly_note))
            Spacer(Modifier.height(Spacing.sm))
            FootLine(Icons.Rounded.Lock, stringResource(R.string.s42_privacy))
            Spacer(Modifier.height(Spacing.xl))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.padding(horizontal = Spacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow).padding(Spacing.lg),
    ) { content() }
}

@Composable
private fun Section(text: String) {
    Text(
        text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = Spacing.gutter, top = Spacing.xl, bottom = Spacing.sm),
    )
}

@Composable
private fun FootLine(icon: ImageVector, text: String) {
    Row(Modifier.padding(horizontal = Spacing.gutter)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderField() {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = stringResource(R.string.s42_provider_neuralwatt),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.s42_provider)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            textStyle = MaterialTheme.typography.bodyLarge,
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(stringResource(R.string.s42_provider_neuralwatt)) },
                onClick = { open = false },
            )
        }
    }
}

@Composable
private fun KeyField(value: String, onChange: (String) -> Unit, last4: String?, invalid: Boolean) {
    val cs = MaterialTheme.colorScheme
    var visible by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.s42_key)) },
        placeholder = {
            Text(
                if (last4 != null) "••••••••••••$last4" else stringResource(R.string.s42_key_placeholder_new),
                fontFamily = if (last4 != null) FontFamily.Monospace else null,
                color = if (last4 != null) cs.onSurface else cs.onSurfaceVariant,
            )
        },
        singleLine = true,
        isError = invalid,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        trailingIcon = {
            Row {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        stringResource(if (visible) R.string.s42_key_hide else R.string.s42_key_show),
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        val text = clipboard.getClipEntry()?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
                        if (!text.isNullOrBlank()) onChange(text)
                    }
                }) { Icon(Icons.Rounded.ContentPaste, stringResource(R.string.s42_key_paste)) }
            }
        },
        supportingText = {
            if (invalid) {
                Row {
                    Icon(Icons.Rounded.ErrorOutline, null, Modifier.size(16.dp), tint = cs.error)
                    Spacer(Modifier.width(Spacing.xs))
                    Text(stringResource(R.string.s42_key_invalid))
                }
            } else {
                Text(stringResource(R.string.s42_key_help))
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ConnLine(conn: ConnState) {
    val cs = MaterialTheme.colorScheme
    val (icon, text) = when (conn) {
        ConnState.Idle, ConnState.Testing -> return
        is ConnState.Ok -> Icons.Rounded.CheckCircle to stringResource(R.string.s42_test_ok, "%02d:%02d".format(conn.at.hour, conn.at.minute))
        is ConnState.Failed -> when (conn.type) {
            AiErrorType.INVALID_KEY -> return // báo ở ô key (lỗi của ô nhập)
            AiErrorType.NO_KEY -> Icons.Rounded.Info to stringResource(R.string.s42_test_no_key)
            AiErrorType.NO_NETWORK, AiErrorType.TIMEOUT -> Icons.Rounded.WifiTetheringError to stringResource(R.string.s42_test_no_network)
            AiErrorType.RATE_LIMITED, AiErrorType.OVERLOADED -> Icons.Rounded.Info to stringResource(R.string.s42_test_rate)
            AiErrorType.QUOTA -> Icons.Rounded.Info to stringResource(R.string.s42_test_quota)
            else -> Icons.Rounded.Info to stringResource(R.string.s42_test_other, conn.message)
        }
    }
    Spacer(Modifier.height(Spacing.md))
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(20.dp), tint = if (conn is ConnState.Ok) cs.primary else cs.onSurfaceVariant)
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = cs.onSurface)
    }
}

/** Chuỗi sửa được; lưu khi rời ô hoặc bấm Xong. */
@Composable
private fun ModelField(label: String, value: String, sub: String, onCommit: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    val latest = androidx.compose.runtime.rememberUpdatedState(text)
    DisposableEffect(Unit) { onDispose { if (latest.value != value) onCommit(latest.value) } }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.trim() },
        label = { Text(label) },
        singleLine = true,
        supportingText = { Text(sub) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onCommit(text) }),
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().onLeave { if (text != value) onCommit(text) },
    )
}

@Composable
private fun NotesField(value: String, onCommit: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it.take(AiSettingsViewModel.NOTES_MAX)
            onCommit(text)
        },
        label = { Text(stringResource(R.string.s42_notes)) },
        minLines = 3,
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.s42_notes_help))
                Text("${text.length}/${AiSettingsViewModel.NOTES_MAX}")
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Gọi [block] khi ô mất focus (đã từng có focus). */
private fun Modifier.onLeave(block: () -> Unit): Modifier = composed {
    var had by remember { mutableStateOf(false) }
    onFocusChanged {
        if (it.isFocused) {
            had = true
        } else if (had) {
            had = false
            block()
        }
    }
}

package com.cali.feature.onboarding

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.data.prefs.SecureKeyStore
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.ui.component.IconTile
import com.cali.core.ui.component.PrimaryButton
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class QuickSetupUiState(
    val apiKey: String = "",
    val showKey: Boolean = false,
    val waterEnabled: Boolean = true,
    val waterStart: LocalTime = LocalTime.of(7, 0),
    val waterEnd: LocalTime = LocalTime.of(22, 0),
    val waterInterval: Int = 90,
    val saving: Boolean = false,
    val saveFailed: Boolean = false,
)

/** S03. Nút "Kiểm tra" key để GĐ4 (09 §8). */
@HiltViewModel
class QuickSetupViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val keyStore: SecureKeyStore,
) : ViewModel() {
    private val _state = MutableStateFlow(QuickSetupUiState())
    val state = _state.asStateFlow()
    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init {
        viewModelScope.launch {
            val s = settings.current()
            _state.update { it.copy(waterEnabled = s.waterEnabled, waterStart = s.waterStart, waterEnd = s.waterEnd, waterInterval = s.waterIntervalMin) }
        }
    }

    fun setKey(v: String) = _state.update { it.copy(apiKey = v.trim()) }
    fun toggleShow() = _state.update { it.copy(showKey = !it.showKey) }
    fun setWater(v: Boolean) = _state.update { it.copy(waterEnabled = v) }

    /** Xong: lưu key (nếu có, mã hoá) và công tắc nhắc nước. Không ghi key ra log. */
    fun finish() {
        val s = _state.value
        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val keyOk = if (s.apiKey.isNotBlank()) keyStore.save(s.apiKey).isSuccess else true
            val setOk = settings.update { it.copy(waterEnabled = s.waterEnabled) }.isSuccess
            _state.update { it.copy(saving = false, saveFailed = !(keyOk && setOk), apiKey = if (keyOk) "" else it.apiKey) }
            if (keyOk && setOk) _done.send(Unit)
        }
    }
}

@Composable
fun QuickSetupRoute(onFinished: () -> Unit, onBack: () -> Unit, vm: QuickSetupViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.done.collect { onFinished() } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.finish() }
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme

    Surface(Modifier.fillMaxSize(), color = cs.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.gutter)) {
                Text(stringResource(R.string.s03_title), style = MaterialTheme.typography.headlineMedium, color = cs.onSurface)
                Spacer(Modifier.height(Spacing.xs))
                Text(stringResource(R.string.s03_lead), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.gutter))

                SetupCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.AutoAwesome, selected = false)
                        Spacer(Modifier.width(Spacing.md))
                        Text(stringResource(R.string.s03_ai_title), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(stringResource(R.string.s03_ai_desc), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = s.apiKey,
                        onValueChange = vm::setKey,
                        label = { Text(stringResource(R.string.field_api_key)) },
                        placeholder = { Text(stringResource(R.string.s03_api_key_hint)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraSmall,
                        visualTransformation = if (s.showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = vm::toggleShow) {
                                    Icon(
                                        if (s.showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        stringResource(if (s.showKey) R.string.cd_hide_key else R.string.cd_show_key),
                                    )
                                }
                                IconButton(onClick = { pasteInto(ctx, vm::setKey) }) {
                                    Icon(Icons.Rounded.ContentPaste, stringResource(R.string.cd_paste))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                SetupCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.WaterDrop, selected = false)
                        Spacer(Modifier.width(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.s03_water_title), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                            Text(
                                stringResource(R.string.s03_water_sub, s.waterStart.toString(), s.waterEnd.toString(), s.waterInterval),
                                style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                            )
                        }
                        Switch(checked = s.waterEnabled, onCheckedChange = vm::setWater)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(stringResource(R.string.s03_water_permission), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                if (s.saveFailed) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(stringResource(R.string.error_save), style = MaterialTheme.typography.bodySmall, color = cs.error)
                }
            }
            PrimaryButton(
                text = stringResource(R.string.action_done),
                enabled = !s.saving,
                onClick = {
                    if (s.waterEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.finish()
                    }
                },
                modifier = Modifier.padding(horizontal = Spacing.gutter).padding(top = Spacing.lg),
            )
            TextButton(onClick = onFinished, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = Spacing.sm).height(48.dp)) {
                Text(stringResource(R.string.s03_later), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SetupCard(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(Spacing.lg),
    ) { content() }
}

private fun pasteInto(ctx: Context, set: (String) -> Unit) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString()?.let(set)
}

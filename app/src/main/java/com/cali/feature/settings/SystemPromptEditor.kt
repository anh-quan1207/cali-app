package com.cali.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.R
import com.cali.core.ai.SystemPromptSource
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.ui.UiMessage
import com.cali.core.ui.UndoCenter
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SystemPromptViewModel @Inject constructor(
    private val prompts: SystemPromptSource,
    private val settings: SettingsRepository,
    private val undo: UndoCenter,
) : ViewModel() {
    suspend fun load(): Pair<String, String> = prompts.defaultPrompt() to prompts.current()

    /** Bằng bản mặc định thì xoá bản sửa (để sau này nhận bản mặc định mới). */
    fun save(text: String, default: String, onDone: () -> Unit) = viewModelScope.launch {
        val override = text.trim().takeUnless { it.isEmpty() || it == default.trim() }
        settings.update { it.copy(systemPromptOverride = override, systemPromptDefaultVersion = SystemPromptSource.DEFAULT_VERSION) }
            .onSuccess {
                undo.post(UiMessage(R.string.s42e_saved))
                onDone()
            }
    }
}

/** S42 trình soạn system prompt (s40 f9): toàn màn, Khôi phục mặc định có Hoàn tác, đóng khi chưa Lưu thì bỏ. */
@Composable
fun SystemPromptRoute(onClose: () -> Unit, vm: SystemPromptViewModel = hiltViewModel()) {
    var default by remember { mutableStateOf<String?>(null) }
    var text by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val (d, c) = vm.load()
        default = d
        if (text == null) text = c
    }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val res = LocalResources.current
    val cs = MaterialTheme.colorScheme
    val d = default ?: return
    val t = text ?: return
    val edited = t.trim() != d.trim()

    Scaffold(containerColor = cs.surface, snackbarHost = { SnackbarHost(snack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, stringResource(R.string.action_close)) }
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.s42e_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface, modifier = Modifier.weight(1f))
                TextButton(onClick = { vm.save(t, d, onClose) }) { Text(stringResource(R.string.action_save), style = MaterialTheme.typography.titleSmall) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(if (edited) R.string.s42e_edited else R.string.s42e_default_chip)) },
                    leadingIcon = { if (edited) Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = if (edited) cs.secondaryContainer else Color.Transparent),
                    border = null,
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    enabled = edited,
                    onClick = {
                        val before = t
                        text = d
                        scope.launch {
                            snack.currentSnackbarData?.dismiss()
                            val r = snack.showSnackbar(res.getString(R.string.s42e_restored), res.getString(R.string.action_undo), duration = SnackbarDuration.Long)
                            if (r == SnackbarResult.ActionPerformed) text = before
                        }
                    },
                ) {
                    Icon(Icons.Rounded.Restore, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text(stringResource(R.string.s42e_restore))
                }
            }
            TextField(
                value = t,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Row(Modifier.fillMaxWidth().background(cs.surfaceContainer).padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
                Icon(Icons.Rounded.Info, null, Modifier.size(18.dp), tint = cs.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s42e_footer), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
    }
}

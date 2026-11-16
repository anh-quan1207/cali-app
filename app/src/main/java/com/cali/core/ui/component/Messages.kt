package com.cali.core.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.cali.R
import com.cali.core.ui.UiMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Thu thông báo từ [messages] và hiện snackbar; Hoàn tác chạy hàm undo kèm theo. */
@Composable
fun CollectMessages(messages: Flow<UiMessage>, host: SnackbarHostState) {
    val ctx = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { m ->
            launch {
                host.currentSnackbarData?.dismiss()
                val text = ctx.getString(m.text, *m.args.toTypedArray())
                val r = host.showSnackbar(
                    message = text,
                    actionLabel = if (m.undo != null) ctx.getString(R.string.action_undo) else null,
                    duration = if (m.undo != null) SnackbarDuration.Long else SnackbarDuration.Short,
                )
                if (r == SnackbarResult.ActionPerformed) m.undo?.invoke()
            }
        }
    }
}

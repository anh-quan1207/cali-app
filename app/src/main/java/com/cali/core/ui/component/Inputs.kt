package com.cali.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing

/** Nút chính 56dp tròn hết cỡ (nút bước, Bắt đầu, Xong). */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}

/** Thanh tiến độ "Bước n/5" + nút quay lại (S01). */
@Composable
fun StepHeader(step: Int, total: Int, onBack: (() -> Unit)?) {
    Box(Modifier.fillMaxWidth().height(64.dp)) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
        }
        Column(Modifier.align(Alignment.Center).width(240.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { step.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(stringResource(R.string.step_n, step), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Thẻ / hàng chọn theo selection grammar của DESIGN.md: viền 1dp outline-var → 2dp primary + secondary-container. */
@Composable
fun SelectCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    Box(
        modifier
            .clip(shape)
            .background(if (selected) cs.secondaryContainer else cs.surfaceContainerLow)
            .border(if (selected) BorderStroke(2.dp, cs.primary) else BorderStroke(1.dp, cs.outlineVariant), shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 48.dp),
    ) { content() }
}

/** Ô biểu tượng 40dp góc 12dp trong thẻ chọn: đang chọn thì primary. */
@Composable
fun IconTile(icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, size: Int = 40) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier.size(size.dp).clip(MaterialTheme.shapes.medium).background(if (selected) cs.primary else cs.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) cs.onPrimary else cs.onSurfaceVariant, modifier = Modifier.size(24.dp))
    }
}

/** Ô nhập số có hậu tố đơn vị, lỗi ở dòng hỗ trợ (S01 bước 2, S41). */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supporting: String? = null,
    decimal: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter { it.isDigit() || (decimal && (it == ',' || it == '.')) }.take(6)) },
        label = { Text(label) },
        suffix = { Text(unit, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        textStyle = MaterialTheme.typography.titleLarge,
        isError = error != null,
        supportingText = when {
            error != null -> {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text(error)
                    }
                }
            }
            supporting != null -> { { Text(supporting) } }
            else -> null
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraSmall,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Hộp hổ phách (điều chỉnh an toàn, BMI thấp). Không chặn, chỉ báo. */
@Composable
fun AmberBox(text: String, modifier: Modifier = Modifier) {
    val cc = CaliTheme.colors
    Row(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cc.warnContainer).padding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(Icons.Rounded.Info, null, tint = cc.onWarnContainer, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = cc.onWarnContainer)
    }
}

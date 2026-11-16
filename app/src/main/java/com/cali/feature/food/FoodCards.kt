package com.cali.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.cali.R
import com.cali.core.calc.FoodValues
import com.cali.core.calc.PortionScaler
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.repo.NewFood
import com.cali.core.ui.component.MacroDots
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import kotlin.math.roundToInt

/** Một món trên thẻ xác nhận (S12) / sửa món (S14): giá trị chuẩn × hệ số. */
@Immutable
data class DraftItem(
    val key: Long,
    val name: String,
    val portion: String?,
    val baseGrams: Double?,
    val base: FoodValues,
    val factor: Double = 1.0,
    val source: FoodSource,
    val myFoodId: Long? = null,
    val bookmark: Boolean = false,
    /** Chỉ món từ AI. */
    val confidence: Double? = null,
    val assumptions: String? = null,
    val flags: Set<com.cali.core.calc.AmberFlag> = emptySet(),
    val aiCallId: Long? = null,
) {
    val values: FoodValues get() = PortionScaler.scale(base, factor)
    val grams: Double? get() = baseGrams?.times(factor)

    /** Khẩu phần lưu vào nhật ký: "1½ × 1 tô vừa" khi đổi hệ số. */
    val portionLabel: String?
        get() = when {
            factor == 1.0 -> portion
            portion.isNullOrBlank() -> "×" + VnFormat.factor(factor)
            else -> "${PortionScaler.label(factor).removePrefix("×")} × $portion"
        }

    fun toNewFood() = NewFood(
        name = name,
        portionDesc = portionLabel?.takeIf { it.isNotBlank() },
        grams = grams,
        values = values,
        source = source,
        myFoodId = myFoodId,
        saveAsMyFood = bookmark && myFoodId == null,
        aiConfidence = confidence,
        aiCallId = aiCallId,
    )
}

/** Thẻ một món: tên, kcal, khẩu phần + macro, hàng hệ số ½ · ×1 · 1½ · 2 · ✎, dấu trang. */
@Composable
fun DraftCard(
    item: DraftItem,
    onFactor: (Double) -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier,
    extra: @Composable ColumnScope.() -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    var gramsDialog by remember { mutableStateOf(false) }
    val v = item.values
    Column(modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.md, bottom = Spacing.xs)) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(end = Spacing.sm)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(Spacing.sm))
            Text(VnFormat.int(v.kcal.roundToInt()), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(" kcal", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        }
        val portion = listOfNotNull(item.portionLabel?.takeIf { it.isNotBlank() }, item.grams?.let { "~" + VnFormat.int(it.roundToInt()) + " g" }).joinToString(" · ")
        MacroDots(v.proteinG, v.carbG, v.fatG, prefix = portion.ifBlank { null }, modifier = Modifier.padding(top = 2.dp, end = Spacing.sm))
        if (item.factor !in PortionScaler.PRESETS) {
            Row(Modifier.padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Calculate, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    stringResource(R.string.s12_factor_custom, VnFormat.factor(item.factor), item.portion ?: "1", VnFormat.int(item.base.kcal.roundToInt())),
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                )
            }
        }
        extra()
        Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            PortionScaler.PRESETS.forEach { f ->
                FactorChip(PortionScaler.label(f), selected = item.factor == f, onClick = { onFactor(f) })
            }
            IconButton(onClick = { gramsDialog = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Edit, stringResource(R.string.s12_grams_title), tint = if (item.factor in PortionScaler.PRESETS) cs.onSurfaceVariant else cs.primary)
            }
            Spacer(Modifier.weight(1f))
            val saved = item.myFoodId != null || item.bookmark
            IconToggleButton(checked = saved, onCheckedChange = { if (item.myFoodId == null) onBookmark() }, modifier = Modifier.size(48.dp)) {
                Icon(
                    if (saved) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                    stringResource(if (saved) R.string.s12_bookmarked else R.string.s12_bookmark),
                    tint = cs.primary,
                )
            }
        }
    }
    if (gramsDialog) {
        GramsDialog(item, onDismiss = { gramsDialog = false }, onDone = { f -> onFactor(f); gramsDialog = false })
    }
}

@Composable
private fun FactorChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cd = stringResource(R.string.s12_factor_cd, label)
    Box(
        Modifier
            .size(width = 52.dp, height = 48.dp)
            .padding(vertical = 6.dp, horizontal = 2.dp)
            .clip(CircleShape)
            .background(if (selected) cs.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { role = Role.RadioButton; this.selected = selected; contentDescription = cd },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium), color = if (selected) cs.onSecondaryContainer else cs.onSurface)
    }
}

/** ✎: nhập gram (khi biết gram chuẩn) hoặc hệ số trực tiếp. */
@Composable
private fun GramsDialog(item: DraftItem, onDismiss: () -> Unit, onDone: (Double) -> Unit) {
    val byGrams = item.baseGrams != null && item.baseGrams > 0
    var text by remember {
        mutableStateOf(if (byGrams) VnFormat.int((item.grams ?: 0.0).roundToInt()) else VnFormat.factor(item.factor))
    }
    val n = FoodFormState.num(text)
    val factor = if (byGrams) n?.let { PortionScaler.factorForGrams(item.baseGrams, it) } else n?.takeIf { it > 0 }
    val ok = factor != null && factor in 0.05..20.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.s12_grams_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { s -> text = s.filter { it.isDigit() || it == ',' || it == '.' }.take(6) },
                    suffix = { Text(if (byGrams) "g" else "×") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.extraSmall,
                )
                if (byGrams) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(stringResource(R.string.s12_grams_base, VnFormat.int(item.baseGrams!!.roundToInt())), style = MaterialTheme.typography.bodySmall)
                }
                if (ok) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "= " + VnFormat.int(PortionScaler.scale(item.base, factor!!).kcal.roundToInt()) + " kcal",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onDone(factor!!) }, enabled = ok) { Text(stringResource(R.string.action_done)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Đầu sheet kiểu S12: tay nắm, nút đóng/quay lại, tiêu đề + dòng phụ, nút phải tuỳ chọn. */
@Composable
fun SheetHeader(
    title: String,
    subtitle: String?,
    navIcon: androidx.compose.ui.graphics.vector.ImageVector,
    navLabel: String,
    onNav: () -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(top = Spacing.sm), contentAlignment = Alignment.Center) {
            Box(Modifier.size(width = 32.dp, height = 4.dp).clip(CircleShape).background(cs.onSurfaceVariant.copy(alpha = 0.4f)))
        }
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNav) { Icon(navIcon, navLabel, tint = cs.onSurface) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, textAlign = TextAlign.Center)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.primary, maxLines = 1)
            }
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { action?.invoke() }
        }
    }
}

/** Thanh dưới cùng: nút chính lớn "✓ Lưu vào bữa Trưa      490 kcal". */
@Composable
fun BottomSaveBar(label: String, trailing: String?, enabled: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(color = cs.surfaceContainer) {
        Column {
            HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.lg, vertical = Spacing.md).heightIn(min = 56.dp),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Check, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (trailing != null) Text(trailing, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

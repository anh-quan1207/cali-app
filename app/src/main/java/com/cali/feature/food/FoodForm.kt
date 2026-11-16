package com.cali.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.AmberCheck
import com.cali.core.calc.AmberFlag
import com.cali.core.calc.FoodValues
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import kotlin.math.abs
import kotlin.math.roundToInt

/** Các ô của Nhập tay (S13), Món mới / Sửa món đã lưu (S34) và phần "Sửa tên, kcal, macro" (S14). */
@Immutable
data class FoodFormState(
    val name: String = "",
    val portion: String = "",
    val grams: String = "",
    val kcal: String = "",
    val protein: String = "",
    val carb: String = "",
    val fat: String = "",
    /** Đã rời ô Béo hoặc bấm Tiếp → mới kiểm tra (03, lỗi hiện khi rời ô). */
    val checked: Boolean = false,
) {
    val kcalValue: Double? get() = num(kcal)
    val nameOk get() = name.isNotBlank()
    val kcalOk get() = kcalValue?.let { it in 0.0..5000.0 } == true
    val valid get() = nameOk && kcalOk

    fun values() = FoodValues(kcalValue ?: 0.0, num(protein) ?: 0.0, num(carb) ?: 0.0, num(fat) ?: 0.0)
    fun gramsValue(): Double? = num(grams)?.takeIf { it > 0 }

    /** Cờ hổ phách theo AmberCheck (03 §5); chỉ khi có nhập macro. */
    fun amber(): Set<AmberFlag> {
        if (!kcalOk) return emptySet()
        val hasMacro = listOf(protein, carb, fat).any { it.isNotBlank() }
        val flags = AmberCheck.check(values()).flags
        return if (hasMacro) flags else flags - AmberFlag.MACRO_MISMATCH
    }

    companion object {
        fun num(s: String): Double? = s.trim().replace(',', '.').takeIf { it.isNotEmpty() }?.toDoubleOrNull()

        fun of(name: String, portion: String?, grams: Double?, v: FoodValues) = FoodFormState(
            name = name,
            portion = portion.orEmpty(),
            grams = grams?.let { VnFormat.kg(it) }.orEmpty(),
            kcal = v.kcal.roundToInt().toString(),
            protein = fmt(v.proteinG),
            carb = fmt(v.carbG),
            fat = fmt(v.fatG),
        )

        private fun fmt(x: Double) = VnFormat.kg(x)
    }
}

@Composable
fun FoodFields(state: FoodFormState, onChange: (FoodFormState) -> Unit, showErrors: Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Field(
            state.name, { onChange(state.copy(name = it.take(80))) }, stringResource(R.string.field_food_name),
            support = stringResource(R.string.field_required),
            error = if (showErrors && !state.nameOk) stringResource(R.string.error_name_required) else null,
            keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Field(state.portion, { onChange(state.copy(portion = it.take(60))) }, stringResource(R.string.field_portion), modifier = Modifier.weight(1.6f),
                keyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
            Field(state.grams, { onChange(state.copy(grams = digits(it))) }, stringResource(R.string.field_grams), unit = "g", modifier = Modifier.weight(1f), numeric = true)
        }
        Field(
            state.kcal, { onChange(state.copy(kcal = digits(it, decimal = false))) }, stringResource(R.string.field_kcal), unit = "kcal", numeric = true,
            support = stringResource(R.string.field_kcal_support),
            error = if (showErrors && !state.kcalOk) stringResource(R.string.error_kcal_required) else null,
            big = true,
        )
        Spacer(Modifier.height(Spacing.xs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(stringResource(R.string.manual_macro), style = MaterialTheme.typography.titleSmall, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.manual_optional), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Field(state.protein, { onChange(state.copy(protein = digits(it))) }, stringResource(R.string.macro_protein), unit = "g", numeric = true, modifier = Modifier.weight(1f))
            Field(state.carb, { onChange(state.copy(carb = digits(it))) }, stringResource(R.string.macro_carb), unit = "g", numeric = true, modifier = Modifier.weight(1f))
            Field(
                state.fat, { onChange(state.copy(fat = digits(it))) }, stringResource(R.string.macro_fat), unit = "g", numeric = true,
                modifier = Modifier.weight(1f).onFocusChanged { if (!it.isFocused && state.fat.isNotBlank() && !state.checked) onChange(state.copy(checked = true)) },
                imeAction = ImeAction.Done,
            )
        }
        if (state.checked || showErrors) AmberNotes(state)
    }
}

@Composable
private fun AmberNotes(state: FoodFormState) {
    val cc = CaliTheme.colors
    val flags = state.amber()
    if (flags.isEmpty()) return
    val v = state.values()
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (AmberFlag.MACRO_MISMATCH in flags) {
            Row(
                Modifier.clip(MaterialTheme.shapes.small).background(cc.warnContainer).padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Warning, null, tint = cc.onWarnContainer, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(stringResource(R.string.amber_mismatch), style = MaterialTheme.typography.labelLarge, color = cc.onWarnContainer)
            }
            val fromMacro = 4 * v.proteinG + 4 * v.carbG + 9 * v.fatG
            val pct = if (fromMacro > 0) (abs(v.kcal - fromMacro) / fromMacro * 100).roundToInt() else 100
            Row {
                Icon(Icons.Rounded.Warning, null, tint = cc.warn, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    stringResource(
                        R.string.amber_mismatch_body, VnFormat.int(fromMacro.roundToInt()), VnFormat.kg(v.proteinG), VnFormat.kg(v.carbG), VnFormat.kg(v.fatG),
                        VnFormat.int(v.kcal.roundToInt()), pct,
                    ),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (AmberFlag.VERY_HIGH in flags) {
            Text(stringResource(R.string.amber_very_high), style = MaterialTheme.typography.bodySmall, color = cc.warn)
        }
    }
}

private fun digits(s: String, decimal: Boolean = true) = s.filter { it.isDigit() || (decimal && (it == ',' || it == '.')) }.take(7)

@Composable
private fun Field(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    support: String? = null,
    error: String? = null,
    numeric: Boolean = false,
    big: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    keyboard: KeyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text, imeAction = imeAction),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label, maxLines = 1) },
        suffix = unit?.let { { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        textStyle = if (big || numeric) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
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
            support != null -> { { Text(support) } }
            else -> null
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraSmall,
        keyboardOptions = keyboard,
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
    )
}

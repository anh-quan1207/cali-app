package com.cali.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.RateLabel
import com.cali.core.calc.RateOption
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing

/** Hàng chọn có ô biểu tượng, tiêu đề, dòng phụ, chữ phải (mức vận động, mục tiêu). */
@Composable
fun IconChoiceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    SelectCard(selected, onClick, modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, selected)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) {
                Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Hàng chọn tốc độ (S01 bước 5 / sheet S41). Bị kẹp → dòng hổ phách kèm tốc độ thực.
 * [weeksUnderRate]: kiểu S41 (số tuần dưới tốc độ, "Đã chỉnh · ~0,52 kg/tuần" bên phải).
 */
@Composable
fun RateRow(option: RateOption, selected: Boolean, onClick: () -> Unit, weeksUnderRate: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    val warn = CaliTheme.colors.warn
    SelectCard(selected, onClick, Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.rate_kg, VnFormat.rate(option.rateKg)), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.width(Spacing.sm))
                    if (option.label == RateLabel.RECOMMENDED) {
                        Text(
                            stringResource(option.label.label()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) cs.onPrimary else cs.onPrimaryContainer,
                            modifier = Modifier.clip(MaterialTheme.shapes.small)
                                .background(if (selected) cs.primary else cs.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    } else {
                        Text(stringResource(option.label.label()), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                }
                if (weeksUnderRate && option.weeks != null) {
                    Text(stringResource(R.string.weeks_approx, option.weeks), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${VnFormat.int(option.targetKcal)} ${stringResource(R.string.unit_kcal)}", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                when {
                    option.clamped -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Info, null, tint = warn, modifier = Modifier.size(14.dp))
                        val rate = VnFormat.decimal2(option.actualRateKg)
                        Text(
                            if (weeksUnderRate) stringResource(R.string.rate_adjusted, rate)
                            else stringResource(R.string.rate_clamped_hint, rate, option.weeks ?: 0),
                            style = MaterialTheme.typography.labelMedium,
                            color = warn,
                        )
                    }
                    !weeksUnderRate && option.weeks != null ->
                        Text(stringResource(R.string.weeks_approx, option.weeks), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}

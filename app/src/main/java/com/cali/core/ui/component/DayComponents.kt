package com.cali.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cali.R
import com.cali.core.calc.Projection
import com.cali.core.calc.RingPart
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import com.cali.core.ui.theme.TimeLabelStyle
import kotlin.math.roundToInt

// ---- Bữa: tông màu, biểu tượng, nhãn ----

@Composable
fun MealType.tone(): Color = when (this) {
    MealType.BREAKFAST -> CaliTheme.colors.mealSang
    MealType.LUNCH -> CaliTheme.colors.mealTrua
    MealType.DINNER -> CaliTheme.colors.mealToi
    MealType.SNACK -> CaliTheme.colors.mealPhu
}

fun MealType.icon(): ImageVector = when (this) {
    MealType.BREAKFAST -> Icons.Rounded.WbTwilight
    MealType.LUNCH -> Icons.Rounded.LightMode
    MealType.DINNER -> Icons.Rounded.DarkMode
    MealType.SNACK -> Icons.Rounded.Cookie
}

fun MealType.label(): Int = when (this) {
    MealType.BREAKFAST -> R.string.meal_breakfast
    MealType.LUNCH -> R.string.meal_lunch
    MealType.DINNER -> R.string.meal_dinner
    MealType.SNACK -> R.string.meal_snack
}

/** Thứ tự hiển thị thẻ bữa trên S10 (mockup): Sáng, Trưa, Tối, Phụ. */
val MEAL_CARD_ORDER = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)

/** Thứ tự trong segmented chọn bữa (mockup s12 khung 4): theo giờ trong ngày. */
val MEAL_PICK_ORDER = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.SNACK, MealType.DINNER)

fun FoodSource.icon(): ImageVector = when (this) {
    FoodSource.AI_PHOTO, FoodSource.AI_CHAT -> Icons.Rounded.AutoAwesome
    FoodSource.MY_FOOD -> Icons.Rounded.Bookmark
    FoodSource.MANUAL -> Icons.Rounded.Edit
}

@Composable
fun ringParts(parts: List<RingPart>): List<RingSegment> = parts.map { RingSegment(it.kcal, it.meal.tone()) }

/** Ô màu bữa 24dp góc 8dp: đầy khi có món, viền đứt khi trống. */
@Composable
fun MealBadge(meal: MealType, filled: Boolean, size: Dp = 24.dp) {
    val cs = MaterialTheme.colorScheme
    val tone = meal.tone()
    val outline = cs.outlineVariant
    Box(
        Modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .then(if (filled) Modifier.background(tone) else Modifier)
            .drawBehind {
                if (!filled) {
                    drawRoundRect(
                        outline, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f))),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(meal.icon(), null, tint = if (filled) cs.surface else cs.outline, modifier = Modifier.size(size * 2 / 3))
    }
}

/** Ô vuông màu bữa 10dp, dùng trong chip giờ · bữa và segmented. */
@Composable
fun MealSquare(meal: MealType) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(meal.tone()))
}

// ---- Macro: Goal-Line Split ----

data class MacroValue(val grams: Double, val target: Int)

/**
 * Ba thanh macro (S10). Thanh dài = 125% mục tiêu, vạch mục tiêu ở 80%.
 * Vượt: phần trong mục tiêu giữ màu, phần dư tách bằng khe 3dp màu error, chữ đỏ.
 */
@Composable
fun MacroBars(protein: MacroValue, carb: MacroValue, fat: MacroValue, modifier: Modifier = Modifier) {
    val cc = CaliTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MacroBar(stringResource(R.string.macro_protein), cc.protein, protein, Modifier.weight(1f))
        MacroBar(stringResource(R.string.macro_carb), cc.carb, carb, Modifier.weight(1f))
        MacroBar(stringResource(R.string.macro_fat), cc.fat, fat, Modifier.weight(1f))
    }
}

@Composable
private fun MacroBar(name: String, color: Color, v: MacroValue, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    val g = v.grams.roundToInt()
    val over = v.target in 1 until g
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = if (over) cs.error else cs.onSurface)) { append(g.toString()) }
                withStyle(MaterialTheme.typography.labelMedium.toSpanStyle().copy(color = if (over) cs.error else cs.onSurfaceVariant)) { append(" / ${v.target} g") }
            },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
        Spacer(Modifier.height(6.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().height(6.dp)) {
            val w = maxWidth
            val track = cs.surfaceContainerHighest
            val ink = cs.onSurface
            val ratio = if (v.target > 0) v.grams / v.target else 0.0
            Box(Modifier.fillMaxWidth().fillMaxHeight().background(track))
            if (!over) {
                Box(Modifier.width(w * (ratio / 1.25).toFloat().coerceIn(0f, 0.8f)).fillMaxHeight().background(color))
            } else {
                Box(Modifier.width(w * 0.8f - 1.5.dp).fillMaxHeight().background(color))
                val ex = (minOf(ratio - 1, 0.25) / 1.25).toFloat()
                Box(Modifier.offset(x = w * 0.8f + 1.5.dp).width((w * ex - 1.5.dp).coerceAtLeast(2.dp)).fillMaxHeight().background(cs.error))
            }
            // vạch mục tiêu 2dp, cao hơn thanh 3dp mỗi phía
            Box(Modifier.offset(x = w * 0.8f - 1.dp, y = (-3).dp).width(2.dp).height(12.dp).background(ink))
        }
        Spacer(Modifier.height(3.dp))
        Text(
            if (over) stringResource(R.string.s10_macro_over, g - v.target) else stringResource(R.string.s10_macro_left, (v.target - g).coerceAtLeast(0)),
            style = MaterialTheme.typography.labelSmall,
            color = if (over) cs.error else cs.onSurfaceVariant,
        )
    }
}

/** Chấm macro "● 33 ● 60 ● 12 g" trên hàng món (S12, S13, S34). */
@Composable
fun MacroDots(p: Double, c: Double, f: Double, prefix: String? = null, modifier: Modifier = Modifier) {
    val cc = CaliTheme.colors
    val cs = MaterialTheme.colorScheme
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (!prefix.isNullOrBlank()) {
            Text("$prefix · ", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        }
        listOf(cc.protein to p, cc.carb to c, cc.fat to f).forEachIndexed { i, (col, v) ->
            Box(Modifier.size(6.dp).clip(CircleShape).background(col))
            Spacer(Modifier.width(3.dp))
            Text(v.roundToInt().toString() + if (i == 2) " g" else "", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1)
            if (i < 2) Spacer(Modifier.width(6.dp))
        }
    }
}

// ---- Thẻ bữa và hàng món (One Food Row) ----

@Composable
fun MealCard(
    meal: MealType,
    kcal: Double,
    count: Int,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    items: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val name = stringResource(meal.label())
    Column(modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(vertical = Spacing.xs)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            MealBadge(meal, filled = count > 0)
            Spacer(Modifier.width(Spacing.md))
            if (count > 0) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.s10_meal_count, count), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(VnFormat.int(kcal.roundToInt()), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(" kcal", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            } else {
                Text(name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.s10_meal_none), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            IconButton(onClick = onAdd, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Add, stringResource(R.string.s10_add_to_meal, name), tint = cs.primary)
            }
        }
        items()
    }
}

/** Một hàng món trong thẻ bữa: giờ · tên + nguồn/khẩu phần · kcal. Chạm = sửa, giữ = menu. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoodRow(
    time: String,
    name: String,
    portion: String?,
    kcal: Double,
    source: FoodSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.background(cs.surfaceContainerLow)) {
        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.55f))
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .heightIn(min = 52.dp)
                .padding(start = Spacing.lg, end = Spacing.gutter, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(time, style = TimeLabelStyle, color = cs.onSurfaceVariant, modifier = Modifier.width(44.dp))
            Spacer(Modifier.width(Spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(source.icon(), null, tint = cs.primary, modifier = Modifier.size(14.dp))
                    if (!portion.isNullOrBlank()) {
                        Spacer(Modifier.width(Spacing.xs))
                        Text(portion, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(VnFormat.int(kcal.roundToInt()), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
        }
    }
}

/** Vuốt trái để xoá: nền error-container với Xoá; thả quá ngưỡng là xoá (không hộp thoại). */
@Composable
fun SwipeToDelete(onDelete: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.4f })
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val cs = MaterialTheme.colorScheme
            Row(
                Modifier.fillMaxWidth().fillMaxHeight().background(cs.errorContainer).padding(end = Spacing.xl),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Delete, null, tint = cs.onErrorContainer)
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.action_delete), style = MaterialTheme.typography.labelLarge, color = cs.onErrorContainer)
            }
        },
    ) { content() }
}

// ---- Làn "Hôm nay sau khi lưu" (S12/S14) ----

/**
 * Làn ngang toàn chiều = mục tiêu (vạch cuối). Đoạn đặc theo bữa cho phần giữ nguyên, đoạn chờ tô nhạt có viền
 * cho phần thêm, nét đứt không tô cho phần bỏ. Phần vượt chạy làn 6dp màu trạng thái bên dưới.
 */
@Composable
fun ProjectionBar(
    title: String,
    projection: Projection,
    targetKcal: Int?,
    baseParts: List<RingPart>,
    pendingMeal: MealType,
    modifier: Modifier = Modifier,
    /** false = không có nền thẻ riêng (đặt trong thẻ chat S20). */
    contained: Boolean = true,
    footer: (@Composable () -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val p = projection
    val stateColor = when (p.statusAfter) {
        com.cali.core.calc.RingStatus.OVER_LIGHT -> cc.warn
        com.cali.core.calc.RingStatus.OVER_MUCH -> cs.error
        else -> cs.onSurface
    }
    Column(if (contained) modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.surfaceContainerLow).padding(Spacing.lg) else modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(
                // Ngày trước khi có mục tiêu: không có "còn / vượt", chỉ nêu tổng sau khi lưu.
                if (targetKcal == null) {
                    stringResource(R.string.proj_total_no_goal, VnFormat.int(p.afterKcal.roundToInt()))
                } else if (p.overAfter > 0) {
                    val res = if (p.statusAfter == com.cali.core.calc.RingStatus.OVER_LIGHT) R.string.s12_over_light else R.string.s12_over_much
                    stringResource(res, p.overPercentAfter, VnFormat.int(p.overAfter))
                } else {
                    stringResource(R.string.s12_left, VnFormat.int(p.remainingAfter))
                },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = stateColor,
            )
        }
        Spacer(Modifier.height(Spacing.md))
        val target = (targetKcal ?: 0).toDouble()
        val pendingTone = pendingMeal.tone()
        BoxWithConstraints(Modifier.fillMaxWidth().height(12.dp)) {
            val w = maxWidth
            Box(Modifier.fillMaxWidth().fillMaxHeight().background(cc.ringTrack))
            if (target > 0) {
                // phần giữ nguyên: cắt baseParts theo baseKcal
                var acc = 0.0
                var x = 0.dp
                val gap = 3.dp
                for (part in baseParts) {
                    val k = (minOf(part.kcal, p.baseKcal - acc)).coerceAtLeast(0.0)
                    acc += part.kcal
                    val shown = minOf(k, (target - (x / w) * target).coerceAtLeast(0.0))
                    if (shown <= 0) continue
                    val len = w * (shown / target).toFloat()
                    Box(Modifier.offset(x = x).width((len - gap).coerceAtLeast(2.dp)).fillMaxHeight().background(part.meal.tone()))
                    x += len
                }
                val addLen = w * (minOf(p.addedKcal, (target - p.baseKcal).coerceAtLeast(0.0)) / target).toFloat()
                if (addLen > 0.dp) {
                    Box(
                        Modifier.offset(x = x).width((addLen - gap).coerceAtLeast(2.dp)).fillMaxHeight()
                            .background(pendingTone.copy(alpha = 0.35f)).border(1.5.dp, pendingTone),
                    )
                    x += addLen
                }
                val remLen = w * (minOf(p.removedKcal, (target - p.baseKcal).coerceAtLeast(0.0)) / target).toFloat()
                if (remLen > 0.dp) {
                    val outline = cs.outline
                    Box(
                        Modifier.offset(x = x).width((remLen - gap).coerceAtLeast(2.dp)).fillMaxHeight().drawBehind {
                            drawRect(outline, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
                        },
                    )
                }
                // vạch mục tiêu ở cuối làn
                Box(Modifier.offset(x = w - 2.dp, y = (-4).dp).width(2.dp).height(20.dp).background(cs.onSurface))
            }
        }
        if (p.overAfter > 0 && target > 0) {
            Spacer(Modifier.height(6.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().height(6.dp)) {
                val len = maxWidth * (minOf(p.overAfter.toDouble(), target) / target).toFloat()
                Box(Modifier.fillMaxWidth().fillMaxHeight().background(stateColor.copy(alpha = 0.18f)))
                Box(Modifier.width(len).fillMaxHeight().clip(CircleShape).background(stateColor))
            }
        }
        if (footer != null) {
            Spacer(Modifier.height(Spacing.sm))
            footer()
        }
    }
}

/** Chữ có phần khớp tìm kiếm tô primary đậm, gạch chân (S13). */
@Composable
fun HighlightedName(name: String, range: IntRange?, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Text(
        buildAnnotatedString {
            if (range == null) {
                append(name)
            } else {
                append(name.substring(0, range.first))
                withStyle(SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append(name.substring(range.first, range.last + 1))
                }
                append(name.substring(range.last + 1))
            }
        },
        style = MaterialTheme.typography.bodyLarge,
        color = cs.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Nhãn khả năng truy cập cho cả hàng. */
fun Modifier.describe(text: String) = semantics(mergeDescendants = true) { contentDescription = text }

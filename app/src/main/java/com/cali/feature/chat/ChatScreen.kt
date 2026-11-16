package com.cali.feature.chat

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiFoodLogJson
import com.cali.core.ai.AiReview
import com.cali.core.ai.DecodedFoodLog
import com.cali.core.calc.Projection
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.ChatRole
import com.cali.core.data.db.entity.FoodLogStatus
import com.cali.core.data.repo.ChatRepository
import com.cali.core.ui.component.MacroDots
import com.cali.core.ui.component.MealSquare
import com.cali.core.ui.component.ProjectionBar
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import com.cali.feature.food.aiErrorText
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** S20 Chat AI. */
@Composable
fun ChatRoute(
    onOpenCard: (Long) -> Unit,
    onOpenAiSettings: () -> Unit,
    onOpenToday: () -> Unit,
    vm: ChatViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val res = LocalResources.current
    LaunchedEffect(Unit) {
        vm.eventFlow.collect { e ->
            launch {
                when (e) {
                    is ChatEvent.Moved -> {
                        val r = snack.showSnackbar(res.getString(R.string.s20_moved, VnFormat.weekdayDayMonth(e.date)), res.getString(R.string.s20_moved_view), duration = SnackbarDuration.Long)
                        if (r == SnackbarResult.ActionPerformed) vm.setDate(e.date)
                    }
                    is ChatEvent.Discarded -> {
                        val r = snack.showSnackbar(res.getString(R.string.s20_skipped), res.getString(R.string.action_undo), duration = SnackbarDuration.Long)
                        if (r == SnackbarResult.ActionPerformed) vm.undoDiscard(e.id)
                    }
                    ChatEvent.OpenAiSettings -> onOpenAiSettings()
                }
            }
        }
    }
    val s = ui ?: return
    var datePicker by remember { mutableStateOf(false) }
    var attachSheet by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            withContext(Dispatchers.IO) { runCatching { ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() }?.let(vm::attach)
        }
    }
    val captureFile = remember { File(ctx.cacheDir, "capture/chat.jpg") }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) scope.launch {
            val bytes = withContext(Dispatchers.IO) { runCatching { captureFile.readBytes() }.getOrNull().also { captureFile.delete() } }
            bytes?.let(vm::attach)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Header(s, onPrev = { vm.shiftDay(-1) }, onNext = { vm.shiftDay(1) }, onPick = { datePicker = true })
            val list = rememberLazyListState()
            val unanswered = if (s.sending) null else ChatRepository.unansweredId(s.messages)
            LaunchedEffect(s.messages.size, s.sending) {
                val n = s.messages.size + (if (s.sending) 1 else 0)
                if (n > 0) list.animateScrollToItem(n)
            }
            if (s.messages.isEmpty() && !s.sending) {
                Empty(s.date == s.today, onPick = vm::setInput, Modifier.weight(1f))
            } else {
                LazyColumn(Modifier.weight(1f), state = list, contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item(key = "day") {
                        Text(
                            if (s.date == s.today) stringResource(R.string.s20_today) else VnFormat.weekdayDayMonth(s.date),
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                    items(s.messages, key = { it.id }) { m ->
                        when {
                            m.role == ChatRole.USER -> UserBubble(m, s, vm, unsent = m.id == unanswered, onRetry = { vm.retry(m.id) }, onOpenAiSettings = onOpenAiSettings)
                            m.foodLogJson != null -> AiFoodCard(m, s, vm, onOpenCard, onOpenToday)
                            else -> AiTextBubble(m, s, vm)
                        }
                    }
                    if (s.sending) item(key = "typing") { Typing() }
                }
            }
            InputBar(s, vm, onAttach = { attachSheet = true })
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
    }

    if (attachSheet) {
        AttachSheet(
            onDismiss = { attachSheet = false },
            onCamera = {
                attachSheet = false
                captureFile.parentFile?.mkdirs()
                camera.launch(FileProvider.getUriForFile(ctx, ctx.packageName + ".files", captureFile))
            },
            onGallery = {
                attachSheet = false
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        )
    }
    if (datePicker) DayPicker(s.date, s.today, onDismiss = { datePicker = false }, onPick = { vm.setDate(it); datePicker = false })
}

@Composable
private fun Header(s: ChatUi, onPrev: () -> Unit, onNext: () -> Unit, onPick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    Column(Modifier.fillMaxWidth().padding(top = Spacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.s20_title), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.s20_prev_day)) }
            Row(Modifier.weight(1f).clip(CircleShape).clickable(onClick = onPick).padding(vertical = Spacing.sm), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(VnFormat.weekdayDayMonth(s.date), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                val sum = s.day?.summary
                if (sum != null && s.day.goal != null) {
                    Text(" · ", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                    if (sum.overKcal > 0) {
                        Text(stringResource(R.string.s20_over, VnFormat.int(sum.overKcal)), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = if (sum.overPercent <= 10) cc.warn else cs.error)
                    } else {
                        Text(stringResource(R.string.s20_left, VnFormat.int(sum.remainingKcal)), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = cs.primary)
                    }
                }
            }
            IconButton(onClick = onNext, enabled = s.date < s.today) { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.s20_next_day)) }
        }
    }
}

@Composable
private fun Empty(today: Boolean, onPick: (String) -> Unit, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth().padding(horizontal = Spacing.md), verticalArrangement = Arrangement.Bottom) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = Spacing.lg)) {
            Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.medium).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = cs.onPrimaryContainer)
            }
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(stringResource(if (today) R.string.s20_empty_title else R.string.s20_empty_title_day), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(stringResource(R.string.s20_empty_sub), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
        val s1 = stringResource(R.string.s20_sugg_1)
        val s2 = stringResource(R.string.s20_sugg_2)
        val s3 = stringResource(R.string.s20_sugg_3)
        listOf(Icons.Rounded.Restaurant to s1, Icons.AutoMirrored.Rounded.HelpOutline to s2, Icons.Rounded.FitnessCenter to s3).forEach { (icon, text) ->
            OutlinedButton(
                onClick = { onPick(text) },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(bottom = Spacing.sm),
                contentPadding = PaddingValues(horizontal = Spacing.lg),
            ) {
                Icon(icon, null, Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.md))
                Text(text, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }
}

private fun timeLabel(s: ChatUi, vm: ChatViewModel, m: ChatMessageEntity): String {
    val t = vm.localTime(m.createdAt)
    val hm = "%02d:%02d".format(t.hour, t.minute)
    return if (t.toLocalDate() != s.date) VnFormat.dayMonth(t.toLocalDate()) + " · " + hm else hm
}

@Composable
private fun UserBubble(m: ChatMessageEntity, s: ChatUi, vm: ChatViewModel, unsent: Boolean, onRetry: () -> Unit, onOpenAiSettings: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Column(
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)).background(cs.secondaryContainer).padding(horizontal = Spacing.lg, vertical = Spacing.md),
        ) {
            m.thumbnailPath?.let { path ->
                val bmp = remember(path) { runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull() }
                if (bmp != null) {
                    Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.size(160.dp).clip(MaterialTheme.shapes.medium))
                    if (!m.text.isNullOrBlank()) Spacer(Modifier.height(Spacing.sm))
                }
            }
            Text(m.text ?: stringResource(R.string.s20_image_only), style = MaterialTheme.typography.bodyLarge, color = cs.onSecondaryContainer)
        }
        Row(Modifier.padding(top = 2.dp, end = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            if (unsent) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = cs.error, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(stringResource(R.string.s20_unsent) + " · ", style = MaterialTheme.typography.labelSmall, color = cs.error)
            }
            Text(timeLabel(s, vm, m), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
    }
    if (unsent) {
        val type = s.errors[m.id] ?: AiErrorType.UNKNOWN
        Column(Modifier.fillMaxWidth().padding(end = Spacing.xl).clip(MaterialTheme.shapes.large).background(cs.errorContainer).padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(if (type == AiErrorType.NO_NETWORK) Icons.Rounded.CloudOff else Icons.Rounded.ErrorOutline, null, tint = cs.onErrorContainer, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(aiErrorText(type)) + ". " + stringResource(R.string.s20_err_suffix), style = MaterialTheme.typography.bodyMedium, color = cs.onErrorContainer)
            }
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.s20_retry), color = cs.onErrorContainer) }
                if (type == AiErrorType.INVALID_KEY || type == AiErrorType.NO_KEY) {
                    OutlinedButton(onClick = onOpenAiSettings, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.Rounded.Settings, null, Modifier.size(18.dp), tint = cs.onErrorContainer)
                        Spacer(Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.s11_open_ai), color = cs.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiStamp(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = Spacing.xs, top = 2.dp)) {
        Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(Spacing.xs))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AiTextBubble(m: ChatMessageEntity, s: ChatUi, vm: ChatViewModel) {
    val cs = MaterialTheme.colorScheme
    Column {
        Text(
            m.text.orEmpty(),
            style = MaterialTheme.typography.bodyLarge, color = cs.onSurface,
            modifier = Modifier.padding(end = Spacing.xl).clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)).background(cs.surfaceContainerHigh).padding(horizontal = Spacing.lg, vertical = Spacing.md),
        )
        AiStamp(timeLabel(s, vm, m))
    }
}

/** Thẻ ghi món thu gọn rộng cố định 304dp; trạng thái chờ / đã lưu / bỏ qua đổi tại chỗ. */
@Composable
private fun AiFoodCard(m: ChatMessageEntity, s: ChatUi, vm: ChatViewModel, onOpenCard: (Long) -> Unit, onOpenToday: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val cc = CaliTheme.colors
    val d: DecodedFoodLog = remember(m.foodLogJson) { AiFoodLogJson.decode(m.foodLogJson) } ?: return
    val status = m.foodLogStatus ?: FoodLogStatus.PENDING
    val total = d.log.items.sumOf { it.kcal }
    val mealName = stringResource(d.timing.mealType.label())
    val at = d.timing.eatenAt
    val dayPart = when (at.toLocalDate()) {
        s.today -> ""
        s.today.minusDays(1) -> " · " + stringResource(R.string.s20_yesterday_part)
        else -> " · " + VnFormat.dayMonth(at.toLocalDate())
    }
    Column {
        Column(
            Modifier.width(304.dp).clip(MaterialTheme.shapes.extraLarge).background(cs.surfaceContainerLow)
                .border(1.dp, cs.outlineVariant, MaterialTheme.shapes.extraLarge).padding(Spacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clip(MaterialTheme.shapes.small).background(cs.secondaryContainer).padding(horizontal = Spacing.sm, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    MealSquare(d.timing.mealType)
                    Spacer(Modifier.width(6.dp))
                    Text("$mealName$dayPart · %02d:%02d".format(at.hour, at.minute), style = MaterialTheme.typography.labelLarge, color = cs.onSecondaryContainer)
                }
                Spacer(Modifier.weight(1f))
                Text(VnFormat.int(total.roundToInt()), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = cs.onSurface)
                Text(" kcal", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            Spacer(Modifier.height(Spacing.md))
            AiReview.reviewOrder(d.log.items).forEach { i ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text(i.name, style = MaterialTheme.typography.titleSmall, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Text(" " + i.portionDesc, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (AiReview.needsCheck(i)) Icon(Icons.AutoMirrored.Rounded.HelpOutline, null, tint = cc.warn, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Spacing.xs))
                    Text(VnFormat.int(i.kcal.roundToInt()), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                }
            }
            MacroDots(d.log.items.sumOf { it.proteinG }, d.log.items.sumOf { it.carbG }, d.log.items.sumOf { it.fatG }, modifier = Modifier.padding(top = Spacing.xs))
            if (status == FoodLogStatus.PENDING && !d.log.clarifyingQuestion.isNullOrBlank()) {
                Spacer(Modifier.height(Spacing.md))
                Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(cc.warnContainer).padding(Spacing.md), verticalAlignment = Alignment.Top) {
                    Icon(Icons.AutoMirrored.Rounded.HelpOutline, null, tint = cc.onWarnContainer, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(d.log.clarifyingQuestion, style = MaterialTheme.typography.bodySmall, color = cc.onWarnContainer)
                }
            }
            val day = s.day
            if (status == FoodLogStatus.PENDING && day?.goal != null && day.date == at.toLocalDate()) {
                Spacer(Modifier.height(Spacing.md))
                ProjectionBar(
                    title = stringResource(if (day.date == s.today) R.string.s20_after_save else R.string.s20_after_save_day),
                    projection = Projection.of(day.summary.totalKcal, 0.0, total, day.goal.targetKcal),
                    targetKcal = day.goal.targetKcal,
                    baseParts = day.summary.segments,
                    pendingMeal = d.timing.mealType,
                    contained = false,
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (status) {
                    FoodLogStatus.PENDING -> {
                        FilledTonalButton(onClick = { onOpenCard(m.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.s20_view_save), style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        TextButton(onClick = { vm.discard(m.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.s20_skip), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        }
                    }
                    FoodLogStatus.SAVED -> {
                        Icon(Icons.Rounded.CheckCircle, null, tint = cs.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s20_saved_to, mealName), style = MaterialTheme.typography.titleSmall, color = cs.onSurface, modifier = Modifier.weight(1f))
                        TextButton(onClick = onOpenToday, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.s20_review)) }
                    }
                    FoodLogStatus.DISCARDED -> {
                        Icon(Icons.Rounded.RemoveCircleOutline, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.s20_skipped), style = MaterialTheme.typography.titleSmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.undoDiscard(m.id) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.action_undo)) }
                    }
                }
            }
        }
        if (!m.text.isNullOrBlank()) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                m.text,
                style = MaterialTheme.typography.bodyLarge, color = cs.onSurface,
                modifier = Modifier.padding(end = Spacing.xl).clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)).background(cs.surfaceContainerHigh).padding(horizontal = Spacing.lg, vertical = Spacing.md),
            )
        }
        AiStamp(timeLabel(s, vm, m))
    }
}

@Composable
private fun Typing() {
    val cs = MaterialTheme.colorScheme
    val cd = stringResource(R.string.s20_typing_cd)
    val reduce = com.cali.core.ui.component.rememberReducedMotion()
    val t = rememberInfiniteTransition(label = "typing")
    Row(
        Modifier.clip(CircleShape).background(cs.surfaceContainerHigh).padding(horizontal = Spacing.lg, vertical = Spacing.md).semantics { contentDescription = cd },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(3) { i ->
            val a = if (reduce) null else t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, delayMillis = i * 150), RepeatMode.Reverse), label = "dot$i")
            Box(Modifier.size(8.dp).clip(CircleShape).background(cs.onSurfaceVariant.copy(alpha = a?.value ?: 0.6f)))
        }
    }
}

@Composable
private fun InputBar(s: ChatUi, vm: ChatViewModel, onAttach: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(cs.surface)) {
        s.attachment?.let { a ->
            val bmp = remember(a) { runCatching { BitmapFactory.decodeByteArray(a.thumbnailJpeg, 0, a.thumbnailJpeg.size)?.asImageBitmap() }.getOrNull() }
            Box(Modifier.padding(start = Spacing.lg, top = Spacing.sm)) {
                if (bmp != null) Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.medium))
                IconButton(onClick = vm::clearAttachment, modifier = Modifier.align(Alignment.TopEnd).size(32.dp)) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.s20_remove_attach_cd), tint = Color.White, modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).padding(2.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAttach) { Icon(Icons.Rounded.AddPhotoAlternate, stringResource(R.string.s20_attach_cd)) }
            TextField(
                value = s.input,
                onValueChange = vm::setInput,
                placeholder = { Text(stringResource(R.string.s20_input_hint)) },
                maxLines = 4,
                shape = CircleShape,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = cs.surfaceContainerHigh, unfocusedContainerColor = cs.surfaceContainerHigh,
                ),
                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            val can = (s.input.isNotBlank() || s.attachment != null) && !s.sending
            FilledTonalIconButton(
                onClick = vm::send, enabled = can, modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
            ) { Icon(Icons.AutoMirrored.Rounded.Send, stringResource(R.string.s20_send_cd)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = Spacing.xl)) {
            SheetRow(Icons.Rounded.PhotoCamera, stringResource(R.string.s20_attach_camera), stringResource(R.string.s20_attach_camera_sub), onCamera)
            SheetRow(Icons.Rounded.PhotoLibrary, stringResource(R.string.s20_attach_gallery), stringResource(R.string.s20_attach_gallery_sub), onGallery)
        }
    }
}

@Composable
private fun SheetRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 72.dp).padding(horizontal = Spacing.xl), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(cs.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = cs.onSecondaryContainer)
        }
        Spacer(Modifier.width(Spacing.lg))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPicker(current: LocalDate, today: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val todayMs = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = current.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: onDismiss()
            }) { Text(stringResource(R.string.action_done)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) { DatePicker(state) }
}

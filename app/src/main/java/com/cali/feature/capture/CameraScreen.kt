package com.cali.feature.capture

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RamenDining
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings as SettingsIcon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.ai.AiErrorType
import com.cali.core.ui.component.MealTimeChip
import com.cali.core.ui.component.MealTimeSheet
import com.cali.core.ui.theme.CaliTheme
import com.cali.core.ui.theme.Spacing
import com.cali.feature.food.aiErrorText
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** S11 Camera — luôn tối (DESIGN: camera always dark). */
@Composable
fun CaptureRoute(
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onManual: () -> Unit,
    onMyFoods: () -> Unit,
    vm: CaptureViewModel = hiltViewModel(),
) {
    val s by vm.ui.collectAsStateWithLifecycle()
    val nav by vm.nav.collectAsStateWithLifecycle()
    LaunchedEffect(nav) {
        when (nav) {
            CaptureNav.ToConfirm -> onConfirm()
            CaptureNav.ToAiSettings -> onOpenAiSettings()
            null -> Unit
        }
        if (nav != null) vm.consumeNav()
    }
    var timeSheet by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            val bytes = withContext(Dispatchers.IO) { runCatching { ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() }
            if (bytes != null) vm.onPicked(bytes) else vm.dismissBadImage()
        }
    }
    val openGallery = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    BackHandler {
        when (s.stage) {
            CaptureStage.CAMERA -> onClose()
            CaptureStage.REVIEW, CaptureStage.ERROR -> vm.retake()
            CaptureStage.ANALYZING -> vm.cancel()
        }
    }

    CaliTheme(darkTheme = true) {
        Surface(Modifier.fillMaxSize(), color = if (s.stage == CaptureStage.CAMERA) Color.Black else MaterialTheme.colorScheme.surface) {
            when (s.stage) {
                CaptureStage.CAMERA -> CameraStage(s, vm, onClose, onGallery = openGallery, onTime = { timeSheet = true })
                CaptureStage.REVIEW -> ReviewStage(s, vm, onTime = { timeSheet = true })
                CaptureStage.ANALYZING -> AnalyzingStage(s, vm)
                CaptureStage.ERROR -> ErrorStage(s, vm, onClose, onManual, onMyFoods, onOpenAiSettings)
            }
            if (timeSheet) {
                MealTimeSheet(s.mealTime, vm.resolver, s.today, onDismiss = { timeSheet = false }, onDone = { vm.setMealTime(it); timeSheet = false })
            }
            if (s.badImage) {
                AlertDialog(
                    onDismissRequest = vm::dismissBadImage,
                    text = { Text(stringResource(R.string.s11_bad_image)) },
                    confirmButton = { TextButton(onClick = vm::dismissBadImage) { Text(stringResource(R.string.action_done)) } },
                )
            }
        }
    }
}

private fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

private enum class Flash(val mode: Int, val icon: ImageVector, val label: Int) {
    AUTO(ImageCapture.FLASH_MODE_AUTO, Icons.Rounded.FlashAuto, R.string.s11_flash_auto),
    ON(ImageCapture.FLASH_MODE_ON, Icons.Rounded.FlashOn, R.string.s11_flash_on),
    OFF(ImageCapture.FLASH_MODE_OFF, Icons.Rounded.FlashOff, R.string.s11_flash_off),
}

@Composable
private fun CameraStage(s: CaptureUi, vm: CaptureViewModel, onClose: () -> Unit, onGallery: () -> Unit, onTime: () -> Unit) {
    val ctx = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        permanentlyDenied = !ok && ctx.activity()?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false
    }
    if (!granted) {
        PermissionScreen(permanentlyDenied, onClose, onAllow = { perm.launch(Manifest.permission.CAMERA) }, onGallery = onGallery)
        return
    }

    var flash by remember { mutableStateOf(Flash.AUTO) }
    var back by remember { mutableStateOf(true) }
    var lowLight by remember { mutableStateOf(false) }
    var noteDialog by remember { mutableStateOf(false) }
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    LaunchedEffect(flash) { capture.flashMode = flash.mode }
    val owner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val mainExecutor = remember { ContextCompat.getMainExecutor(ctx) }

    LaunchedEffect(back) {
        val provider = ProcessCameraProvider.awaitInstance(ctx)
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        // Thiếu sáng (gợi ý trung tính, không phải hổ phách): độ sáng trung bình kênh Y.
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { a ->
            a.setAnalyzer(mainExecutor) { img ->
                val buf = img.planes[0].buffer
                var sum = 0L
                var n = 0
                var i = 0
                while (i < buf.remaining()) {
                    sum += buf.get(i).toInt() and 0xFF
                    n++
                    i += 64
                }
                lowLight = n > 0 && sum / n < LOW_LIGHT_LUMA
                img.close()
            }
        }
        val selector = if (back) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
        runCatching {
            provider.unbindAll()
            provider.bindToLifecycle(owner, selector, preview, capture, analysis)
        }
    }
    DisposableEffect(Unit) {
        onDispose { runCatching { ProcessCameraProvider.getInstance(ctx).get().unbindAll() } }
    }

    val shutterCd = stringResource(R.string.s11_shutter_cd)
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView({ previewView }, Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, stringResource(R.string.action_close), tint = Color.White) }
                Spacer(Modifier.weight(1f))
                val fl = stringResource(flash.label)
                TextButton(onClick = { flash = Flash.entries[(flash.ordinal + 1) % Flash.entries.size] }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(flash.icon, stringResource(R.string.s11_flash_cd, fl), tint = Color.White)
                    Spacer(Modifier.width(Spacing.xs))
                    Text(fl, color = Color.White, style = MaterialTheme.typography.titleSmall)
                }
                IconButton(onClick = { back = !back }) { Icon(Icons.Rounded.FlipCameraAndroid, stringResource(R.string.s11_switch_cd), tint = Color.White) }
            }
            Hint(Icons.Rounded.Info, stringResource(R.string.s11_hint_top), Modifier.align(Alignment.CenterHorizontally))
            if (lowLight) {
                Spacer(Modifier.height(Spacing.sm))
                Hint(Icons.Rounded.BrightnessLow, stringResource(R.string.s11_low_light), Modifier.align(Alignment.CenterHorizontally).padding(horizontal = Spacing.xl))
            }
            Spacer(Modifier.weight(1f))
            MealTimeChip(s.mealTime, s.today, onTime, Modifier.align(Alignment.CenterHorizontally).padding(bottom = Spacing.lg), showSource = false)
            Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                SideButton(Icons.Rounded.PhotoLibrary, stringResource(R.string.s11_gallery), onGallery)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(80.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).padding(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f))
                        .semantics { contentDescription = shutterCd; role = Role.Button }
                        .clickable {
                            val file = File(ctx.cacheDir, "capture.jpg")
                            capture.takePicture(
                                ImageCapture.OutputFileOptions.Builder(file).build(), mainExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        val bytes = runCatching { file.readBytes() }.getOrNull()
                                        file.delete()
                                        if (bytes != null) vm.onCaptured(bytes)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        file.delete()
                                    }
                                },
                            )
                        },
                ) {}
                Spacer(Modifier.weight(1f))
                SideButton(Icons.Rounded.EditNote, stringResource(R.string.s11_note)) { noteDialog = true }
            }
        }
    }
    if (noteDialog) {
        var text by remember { mutableStateOf(s.note) }
        AlertDialog(
            onDismissRequest = { noteDialog = false },
            title = { Text(stringResource(R.string.s11_note_label)) },
            text = {
                OutlinedTextField(text, { text = it.take(CaptureViewModel.NOTE_MAX) }, placeholder = { Text(stringResource(R.string.s11_note_placeholder)) })
            },
            confirmButton = { TextButton(onClick = { vm.setNote(text); noteDialog = false }) { Text(stringResource(R.string.action_done)) } },
            dismissButton = { TextButton(onClick = { noteDialog = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

private const val LOW_LIGHT_LUMA = 45

@Composable
private fun Hint(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(MaterialTheme.shapes.small).background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(Spacing.xs))
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun SideButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick).padding(Spacing.xs)) {
        Box(Modifier.size(48.dp).clip(MaterialTheme.shapes.medium).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White)
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

@Composable
private fun PermissionScreen(permanentlyDenied: Boolean, onClose: () -> Unit, onAllow: () -> Unit, onGallery: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().background(cs.surface).statusBarsPadding().navigationBarsPadding().padding(horizontal = Spacing.gutter)) {
        Box(Modifier.height(64.dp), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onClose, modifier = Modifier.offset(x = (-12).dp)) { Icon(Icons.Rounded.Close, stringResource(R.string.action_close)) }
        }
        Spacer(Modifier.height(Spacing.lg))
        Box(Modifier.size(72.dp).clip(MaterialTheme.shapes.large).background(cs.surfaceContainerHigh), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.NoPhotography, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(Spacing.xl))
        Text(stringResource(R.string.s11_perm_title), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
        Spacer(Modifier.height(Spacing.md))
        Text(stringResource(if (permanentlyDenied) R.string.s11_perm_denied_body else R.string.s11_perm_body), style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
        Spacer(Modifier.height(Spacing.xl))
        Text(stringResource(R.string.s11_perm_gallery_hint), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                if (permanentlyDenied) {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null)))
                } else {
                    onAllow()
                }
            },
            shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Icon(if (permanentlyDenied) Icons.Rounded.SettingsIcon else Icons.Rounded.PhotoCamera, null, Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(if (permanentlyDenied) R.string.s11_perm_settings else R.string.s11_perm_allow), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(Spacing.sm))
        OutlinedButton(onClick = onGallery, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Icon(Icons.Rounded.PhotoLibrary, null, Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.s11_pick_gallery), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(Spacing.lg))
    }
}

@Composable
private fun PhotoBox(jpeg: ByteArray?, modifier: Modifier = Modifier, overlay: @Composable () -> Unit = {}) {
    val cs = MaterialTheme.colorScheme
    val bmp = remember(jpeg) { jpeg?.let { runCatching { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }.getOrNull() } }
    Box(modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(MaterialTheme.shapes.extraLarge).background(cs.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        if (bmp != null) Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Icon(Icons.Rounded.RamenDining, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(56.dp))
        overlay()
    }
}

@Composable
private fun TopTitle(icon: ImageVector, cd: String, title: String?, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClick) { Icon(icon, cd) }
        if (title != null) {
            Spacer(Modifier.width(Spacing.xs))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ReviewStage(s: CaptureUi, vm: CaptureViewModel, onTime: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().imePadding()) {
        TopTitle(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back), stringResource(R.string.s11_review_title), vm::retake)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.md)) {
            PhotoBox(s.image?.jpeg)
            Spacer(Modifier.height(Spacing.md))
            MealTimeChip(s.mealTime, s.today, onTime)
            Spacer(Modifier.height(Spacing.lg))
            Text(stringResource(R.string.s11_note_label), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = Spacing.xs))
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = s.note,
                onValueChange = vm::setNote,
                placeholder = { Text(stringResource(R.string.s11_note_placeholder)) },
                leadingIcon = { Icon(Icons.Rounded.EditNote, null) },
                shape = CircleShape,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.md))
            Info(Icons.Rounded.Info, stringResource(R.string.s11_note_help))
            Spacer(Modifier.height(Spacing.sm))
            Info(Icons.Rounded.Lock, stringResource(R.string.s11_privacy))
            Spacer(Modifier.height(Spacing.lg))
        }
        Row(
            Modifier.fillMaxWidth().background(cs.surfaceContainer).navigationBarsPadding().padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            FilledTonalButton(onClick = vm::retake, shape = CircleShape, modifier = Modifier.weight(0.42f).heightIn(min = 56.dp)) {
                Icon(Icons.Rounded.PhotoCamera, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s11_retake), style = MaterialTheme.typography.titleMedium)
            }
            Button(onClick = vm::analyze, shape = CircleShape, modifier = Modifier.weight(0.58f).heightIn(min = 56.dp)) {
                Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s11_analyze), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun Info(icon: ImageVector, text: String) {
    Row(Modifier.padding(horizontal = Spacing.xs)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AnalyzingStage(s: CaptureUi, vm: CaptureViewModel) {
    val cs = MaterialTheme.colorScheme
    var seconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(s.startedAtMs) {
        while (true) {
            seconds = vm.elapsedSeconds().coerceAtLeast(0)
            delay(1000)
        }
    }
    val reduceMotion = com.cali.core.ui.component.rememberReducedMotion()
    val sweep = if (reduceMotion) null else rememberInfiniteTransition(label = "scan").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "scanY",
    )
    Column(Modifier.fillMaxSize()) {
        TopTitle(Icons.Rounded.Close, stringResource(R.string.action_cancel), null, vm::cancel)
        Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
            BoxWithConstraints {
                val h = maxWidth * 3f / 4f
                PhotoBox(s.image?.jpeg, Modifier.background(Color.Transparent)) {
                    Box(Modifier.fillMaxSize().background(cs.scrim.copy(alpha = 0.35f)))
                    if (sweep != null) {
                        Box(Modifier.fillMaxWidth().offset(y = h * sweep.value - h / 2).height(2.dp).background(cs.primary))
                    }
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(stringResource(R.string.s11_analyzing), style = MaterialTheme.typography.headlineSmall, color = cs.onSurface, modifier = Modifier.padding(horizontal = Spacing.xs))
            Text(
                stringResource(R.string.s11_waited, "%d:%02d".format(seconds / 60, seconds % 60)),
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = Spacing.xs),
            )
            Spacer(Modifier.height(Spacing.lg))
            Step(done = true, n = 1, stringResource(R.string.s11_step1), stringResource(R.string.s11_step1_sub))
            Spacer(Modifier.height(Spacing.md))
            Step(done = false, n = 2, stringResource(R.string.s11_step2), stringResource(R.string.s11_step2_sub))
            Spacer(Modifier.height(Spacing.lg))
            Info(Icons.Rounded.Schedule, stringResource(R.string.s11_wait_note))
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(Spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedButton(onClick = vm::cancel, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(stringResource(R.string.s11_cancel_note), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun Step(done: Boolean, n: Int, title: String, sub: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(if (done) cs.primaryContainer else Color.Transparent)
                .border(BorderStroke(2.dp, if (done) cs.primaryContainer else cs.primary), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Rounded.Check, null, tint = cs.onPrimaryContainer, modifier = Modifier.size(18.dp))
            else Text(n.toString(), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = cs.primary)
        }
        Spacer(Modifier.width(Spacing.md))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorStage(
    s: CaptureUi,
    vm: CaptureViewModel,
    onClose: () -> Unit,
    onManual: () -> Unit,
    onMyFoods: () -> Unit,
    onOpenAiSettings: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val type = s.error ?: AiErrorType.UNKNOWN
    val keyIssue = type == AiErrorType.INVALID_KEY || type == AiErrorType.NO_KEY
    Column(Modifier.fillMaxSize()) {
        TopTitle(Icons.Rounded.Close, stringResource(R.string.action_close), stringResource(R.string.s11_error_title), onClose)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.md)) {
            PhotoBox(s.image?.jpeg)
            if (s.note.isNotBlank()) {
                Spacer(Modifier.height(Spacing.md))
                Row(Modifier.padding(horizontal = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.EditNote, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text("“${s.note.trim()}”", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = cs.onSurface)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(cs.errorContainer).padding(Spacing.lg), verticalAlignment = Alignment.Top) {
                Icon(
                    when (type) {
                        AiErrorType.NO_NETWORK -> Icons.Rounded.CloudOff
                        AiErrorType.TIMEOUT -> Icons.Rounded.HourglassBottom
                        AiErrorType.INVALID_KEY, AiErrorType.NO_KEY -> Icons.Rounded.Key
                        else -> Icons.Rounded.ErrorOutline
                    },
                    null, tint = cs.onErrorContainer,
                )
                Spacer(Modifier.width(Spacing.md))
                Column {
                    Text(stringResource(aiErrorText(type)), style = MaterialTheme.typography.titleMedium, color = cs.onErrorContainer)
                    Text(
                        stringResource(
                            when (type) {
                                AiErrorType.NO_NETWORK -> R.string.s11_err_network_body
                                AiErrorType.TIMEOUT -> R.string.s11_err_timeout_body
                                AiErrorType.INVALID_KEY, AiErrorType.NO_KEY -> R.string.s11_err_key_body
                                else -> R.string.s11_err_other_body
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium, color = cs.onErrorContainer,
                    )
                }
            }
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            if (keyIssue) {
                Button(onClick = onOpenAiSettings, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Icon(Icons.Rounded.SettingsIcon, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.s11_open_ai), style = MaterialTheme.typography.titleMedium)
                }
            }
            // Lỗi key: nút chính là Mở Cài đặt AI, Thử lại lùi xuống nút phụ (một nút chính mỗi màn).
            val retry: @Composable () -> Unit = {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                Text(stringResource(R.string.s11_retry), style = MaterialTheme.typography.titleMedium)
            }
            if (keyIssue) {
                FilledTonalButton(onClick = vm::analyze, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { retry() }
            } else {
                Button(onClick = vm::analyze, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { retry() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = onManual, shape = CircleShape, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Icon(Icons.Rounded.EditNote, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.s13_manual), style = MaterialTheme.typography.titleSmall)
                }
                OutlinedButton(onClick = onMyFoods, shape = CircleShape, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(20.dp)); Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.s34_title), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
